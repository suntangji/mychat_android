package com.example.suntangji.mychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public  class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private Context mContext;
    private List<Room> mFruitList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView fruitImage;
        TextView fruitName;
        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            fruitImage = (ImageView) view.findViewById((R.id.fruit_image));
            fruitName = (TextView) view.findViewById(R.id.fruit_name);
        }
    }
    public RoomAdapter(List<Room> fruitList) {
        mFruitList = fruitList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.room_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Room fruit = mFruitList.get(position);
                Intent intent = new Intent(mContext,ChatActivity.class);

//                Intent intent = new Intent(mContext, RoomActivity.class);
//                intent.putExtra(RoomActivity.FRUIT_NAME, fruit.getName());
//                intent.putExtra(RoomActivity.FRUIT_IMAGE_ID, fruit.getImageId());
                mContext.startActivities(new Intent[]{intent});


            }
        });
        return holder;
//        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room fruit = mFruitList.get(position);
        holder.fruitName.setText(fruit.getName());
        Glide.with(mContext).load(fruit.getImageId()).into(holder.fruitImage);
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }
}