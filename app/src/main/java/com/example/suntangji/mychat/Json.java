package com.example.suntangji.mychat;


/**
 * Created by suntangji on 2018/7/30.
 */

public class Json {
    private String name;
    private String content;
    private int cmd;
    private String to;
    private int room_id;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getCmd() {
        return cmd;
    }
    public void setCmd(int cmd) {
        this.cmd = cmd;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public int getRoom_id() {
        return room_id;
    }
    public  void setRoom_id(int room_id) {
        this.room_id = room_id;
    }
}