package com.koksheng.procleanservices;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.koksheng.procleanservices.Common.Common;
import com.koksheng.procleanservices.Database.Database;
import com.koksheng.procleanservices.Model.Cleaning;
import com.koksheng.procleanservices.Model.Order;
import com.koksheng.procleanservices.Model.Rating;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

public class CleaningDetail extends AppCompatActivity implements RatingDialogListener {

    TextView cleaning_name, cleaning_price, cleaning_description;
    ImageView cleaning_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String cleaningId="";

    FirebaseDatabase database;
    DatabaseReference cleanings;
    DatabaseReference ratingTbl;

    Cleaning currentCleaning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaning_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        cleanings = database.getReference("Cleanings");
        ratingTbl = database.getReference("Rating");

        //Init View
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (CounterFab) findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        cleaningId,
                        currentCleaning.getName(),
                        numberButton.getNumber(),
                        currentCleaning.getPrice(),
                        currentCleaning.getDiscount()
                ));
                Toast.makeText(CleaningDetail.this,"Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });
        btnCart.setCount(new Database(this).getCountCart());

        cleaning_description = (TextView)findViewById(R.id.cleaning_description);
        cleaning_name = (TextView)findViewById(R.id.cleaning_name);
        cleaning_price = (TextView)findViewById(R.id.cleaning_price);
        cleaning_image = (ImageView)findViewById(R.id.img_cleaning);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Cleaning ID from intent
        if(getIntent() != null)
            cleaningId = getIntent().getStringExtra("cleaningId");
        if(!cleaningId.isEmpty())
        {
            if (Common.isConnectedToInterner(getBaseContext()))
            {
                getDetailCleaning(cleaningId);
                getRatingCleaning(cleaningId);
            }
            else
            {
                Toast.makeText(CleaningDetail.this,"Please check your connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getRatingCleaning(String cleaningId) {
        Query cleaningRating = ratingTbl.orderByChild("cleaningId").equalTo(cleaningId);

        cleaningRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0)
                {
                    float average = sum/count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this Service")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(R.color.colorPrimary)
                .setCommentBackgroundColor(android.R.color.white)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(CleaningDetail.this)
                .show();

    }

    private void getDetailCleaning(String cleaningId) {
        cleanings.child(cleaningId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentCleaning = dataSnapshot.getValue(Cleaning.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentCleaning.getImage())
                        .into(cleaning_image);

                collapsingToolbarLayout.setTitle(currentCleaning.getName());

                cleaning_price.setText(currentCleaning.getPrice());

                cleaning_name.setText(currentCleaning.getName());

                cleaning_description.setText(currentCleaning.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //Get Rating and upload to Firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                cleaningId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //Remove old value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                else
                {
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(CleaningDetail.this,"Thanks for submited rating !",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
