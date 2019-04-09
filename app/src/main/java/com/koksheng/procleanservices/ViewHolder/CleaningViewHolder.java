package com.koksheng.procleanservices.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koksheng.procleanservices.Interface.ItemClickListener;
import com.koksheng.procleanservices.R;

public class CleaningViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView cleaning_name, cleaning_price;
    public ImageView cleaning_image, fav_image, share_image, quick_cart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CleaningViewHolder(@NonNull View itemView) {
        super(itemView);

        cleaning_name = (TextView)itemView.findViewById(R.id.cleaning_name);
        cleaning_image = (ImageView)itemView.findViewById(R.id.cleaning_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        share_image = (ImageView)itemView.findViewById(R.id.btnShare);
        cleaning_price = (TextView)itemView.findViewById(R.id.cleaning_price);
        quick_cart = (ImageView)itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
