package com.example.suntangji.mychat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by suntangji on 2018/7/29.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Msg> mMsgList;
    private Context mContext;
    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView otherName;
        CircleImageView otherIcon;
        public ViewHolder (View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
            otherName = (TextView) view.findViewById(R.id.other_name);
            otherIcon = (CircleImageView) view.findViewById(R.id.other_icon);
        }
    }
    public MsgAdapter(List<Msg> msgList) {
        mMsgList = msgList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        if (mContext == null) {
            mContext = parent.getContext();
        }
        final ViewHolder holder = new ViewHolder(view);
        holder.otherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
//                    .setIcon(R.mipmap.icon)//设置标题的图片
                        .setTitle("提示")//设置对话框的标题
                        .setMessage("你想要和他私聊吗")//设置对话框的内容
                        //设置对话框的按钮
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(RoomActivity.this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(RoomActivity.this, "点击了确定的按钮", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Intent intent = new Intent(mContext,SingleChatActivity.class);
                                intent.putExtra("USERNAME", holder.otherName.getText().toString());
                                intent.putExtra("ROOM_ID", RoomActivity.roomId);
                                Log.e("OtherName", holder.otherName.getText().toString() );
                                mContext.startActivities(new Intent[]{intent});

                                //finish();
                            }
                        }).create();
                dialog.show();
            }
        });

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Msg msg = mMsgList.get(position);
        if (msg.getType() == Msg.TYPE_RECEIVED) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            Gson gson=new Gson();
            Json json = gson.fromJson(msg.getContent(), Json.class);
            holder.leftMsg.setText(json.getContent());
            holder.otherName.setText(json.getName());
            Log.d("LeftMsg", "onBindViewHolder: " + holder.leftMsg.getText());

        } else if (msg.getType() == Msg.TYPE_SEND) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }
}
