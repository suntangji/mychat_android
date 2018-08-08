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
    private List<Room> mRoomList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView roomImage;
        TextView roomName;
        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            roomImage = (ImageView) view.findViewById((R.id.room_image));
            roomName = (TextView) view.findViewById(R.id.room_name);
        }
    }
    public RoomAdapter(List<Room> roomList) {
        mRoomList = roomList;
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
                Room room = mRoomList.get(position);
                Intent intent = new Intent(mContext,RoomActivity.class);

//                Intent intent = new Intent(mContext, SingleChatActivity.class);
//                intent.putExtra(SingleChatActivity.FRUIT_NAME, room.getName());
                intent.putExtra("ROOM_ID", room.getRoomId());
                mContext.startActivities(new Intent[]{intent});


            }
        });
        return holder;
//        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room room = mRoomList.get(position);
        holder.roomName.setText(room.getName());
        Glide.with(mContext).load(room.getImageId()).into(holder.roomImage);
    }

    @Override
    public int getItemCount() {
        return mRoomList.size();
    }
}
