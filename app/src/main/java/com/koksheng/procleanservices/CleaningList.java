package com.koksheng.procleanservices;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.koksheng.procleanservices.Common.Common;
import com.koksheng.procleanservices.Database.Database;
import com.koksheng.procleanservices.Interface.ItemClickListener;
import com.koksheng.procleanservices.Model.Cleaning;
import com.koksheng.procleanservices.Model.Order;
import com.koksheng.procleanservices.ViewHolder.CleaningViewHolder;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class CleaningList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference cleaningList;

    String categoryId="";

    FirebaseRecyclerAdapter<Cleaning,CleaningViewHolder> adapter;

    //Search Functionality
    FirebaseRecyclerAdapter<Cleaning,CleaningViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from Bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaning_list);


        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);



        //Firebase
        database = FirebaseDatabase.getInstance();
        cleaningList = database.getReference("Cleanings");

        //Local DB
        localDB = new Database(this);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get Intent here
                if(getIntent()!=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null)
                {
                    if (Common.isConnectedToInterner(getBaseContext()))
                        loadListCleaning(categoryId);
                    else
                    {
                        Toast.makeText(CleaningList.this,"Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //get Intent here
                if(getIntent()!=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId != null)
                {
                    if (Common.isConnectedToInterner(getBaseContext()))
                        loadListCleaning(categoryId);
                    else
                    {
                        Toast.makeText(CleaningList.this,"Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_cleaning);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your service");
        //materialSearchBar.setSpeechMode(false); No need, because we already define it at xml
        loadSuggest(); //Write function to load Suggest from firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //when user type their text, we will change suggest list

                List<String> suggest = new ArrayList<String>();
                for (String search:suggestList) //loop in suggest List
                {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search Bar is close
                //Restore original  adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish
                //Show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


    }

    private void startSearch(CharSequence text) {
        //Create query by name
        Query searchByName = cleaningList.orderByChild("Name").equalTo(text.toString());
        //create options  with query
        FirebaseRecyclerOptions<Cleaning> cleaningOptions = new FirebaseRecyclerOptions.Builder<Cleaning>()
                .setQuery(searchByName,Cleaning.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Cleaning, CleaningViewHolder>(cleaningOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CleaningViewHolder viewHolder, int position, @NonNull Cleaning model) {
                viewHolder.cleaning_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.cleaning_image);
                final Cleaning local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent cleaningDetail = new Intent(CleaningList.this,CleaningDetail.class);
                        cleaningDetail.putExtra("cleaningId",searchAdapter.getRef(position).getKey()); //Send Cleaning ID to new activity
                        startActivity(cleaningDetail);
                    }
                });
            }

            @Override
            public CleaningViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cleaning_item, parent, false);
                return new CleaningViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); //Set adapter for Recycler View is Search result
    }

    private void loadSuggest() {
        cleaningList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Cleaning item = postSnapshot.getValue(Cleaning.class);
                            suggestList.add(item.getName()); // Add name of food to suggest list
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadListCleaning(String categoryId) {
        //Create query by category id
        Query searchByName = cleaningList.orderByChild("menuId").equalTo(categoryId);
        //create options  with query
        FirebaseRecyclerOptions<Cleaning> cleaningOptions = new FirebaseRecyclerOptions.Builder<Cleaning>()
                .setQuery(searchByName,Cleaning.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Cleaning, CleaningViewHolder>(cleaningOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final CleaningViewHolder viewHolder, final int position, @NonNull final Cleaning model) {
                viewHolder.cleaning_name.setText(model.getName());
                viewHolder.cleaning_price.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.cleaning_image);

                //Quick cart
                 viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         new Database(getBaseContext()).addToCart(new Order(
                                 adapter.getRef(position).getKey(),
                                 model.getName(),
                                 "1",
                                 model.getPrice(),
                                 model.getDiscount()
                         ));
                         Toast.makeText(CleaningList.this,"Added to Cart", Toast.LENGTH_SHORT).show();


                     }
                 });

                //Add Favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click to Share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });


                //Click to change state of Favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDB.isFavorites(adapter.getRef(position).getKey())) {
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(CleaningList.this, "" + model.getName() + " was added to Favourites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(CleaningList.this, "" + model.getName() + " was removed from Favourites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Cleaning local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent cleaningDetail = new Intent(CleaningList.this, CleaningDetail.class);
                        cleaningDetail.putExtra("cleaningId", adapter.getRef(position).getKey()); //Send Cleaning ID to new activity
                        startActivity(cleaningDetail);
                    }
                });

            }

            @Override
            public CleaningViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cleaning_item, parent, false);
                return new CleaningViewHolder(itemView);
            }
        };
        adapter.startListening();

        //Set Adapter
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter!=null)
            adapter.stopListening();
//            searchAdapter.stopListening();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadListCleaning(categoryId);
        //fix click back on cleaningdetail and get no item in cleaning list
        if (adapter!=null)
            adapter.startListening();
    }
}
