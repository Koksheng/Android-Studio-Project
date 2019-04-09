package com.koksheng.procleanservices.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koksheng.procleanservices.Interface.ItemClickListener;
import com.koksheng.procleanservices.R;

public class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtServiceName;
    public ImageView imageView;

    private ItemClickListener itemClickListener;

    public ServiceViewHolder(View itemView){
        super(itemView);

        txtServiceName = (TextView)itemView.findViewById(R.id.service_name);
        imageView = (ImageView)itemView.findViewById(R.id.service_image);

        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view){
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

}
