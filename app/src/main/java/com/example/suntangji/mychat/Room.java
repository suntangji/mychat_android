package com.example.suntangji.mychat;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by suntangji on 2018/7/28.
 */

public class Room {
    private String name;
    private int imageId;
    private int roomId;
    public Room(String name, int imageId, int roomId) {
        this.name = name;
        this.imageId = imageId;
        this.roomId = roomId;
    }
    public String getName() {
        return name;
    }
    public int getImageId() {
        return imageId;
    }
    public int getRoomId() {
        return roomId;
    }
}

