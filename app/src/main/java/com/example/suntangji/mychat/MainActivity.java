package com.example.suntangji.mychat;

import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Room[] rooms = {new Room("聊天室一", R.drawable.chat_room),
                                new Room("聊天室二", R.drawable.chat_room),
                                new Room("聊天室三", R.drawable.chat_room),
                                new Room("聊天室四", R.drawable.chat_room),
                                new Room("聊天室五", R.drawable.chat_room)};
    private List<Room> roomList = new ArrayList<>();
    private RoomAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navView = (NavigationView) findViewById(R.id.nav_menu);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        // navView.setCheckedItem(R.id.nav_call);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "这个没什么用", Toast.LENGTH_SHORT).show();
            }
        });

        // 卡片话
        initRooms();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RoomAdapter(roomList);
        recyclerView.setAdapter(adapter);
        // 刷新操作
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshRooms();
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.modify:
                        setName(navView);
                }
                return true;
            }
        });


        // username

        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        Global.username = pref.getString("name", "");
        if (Global.username.equals("")) {
            setName(navView);
            Log.e("user", Global.username );
        }

        View headerView = navView.getHeaderView(0);
        TextView textview = (TextView) headerView.findViewById(R.id.user);
        textview.setText(Global.username);

    }


    private void initRooms() {
        roomList.clear();
        for (int i = 0; i < 5; i++) {
            roomList.add(rooms[i]);
        }
    }
    // refresh 模拟
    private void refreshRooms() {
        Log.d("Main", "refreshRooms: run");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initRooms();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Toast.makeText(this, "努力开发中。。。", Toast.LENGTH_SHORT).show();
                break;
            case R.id.room_manage:
                Toast.makeText(this, "努力开发中。。。", Toast.LENGTH_SHORT).show();
                break;
            case R.id.exit:
//                Toast.makeText(this, "努力开发中。。。", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
            default:
        }
        return true;
    }

    private void setName(final NavigationView navView) {
        View view = getLayoutInflater().inflate(R.layout.input_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.input_dialog);
        final String[] ret = new String[1];
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("请输入昵称")//设置对话框的标题
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog.dismiss();
                        Toast.makeText(MainActivity.this, "必须设置昵称", Toast.LENGTH_SHORT).show();
                        setName(navView);
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = editText.getText().toString();
                        Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                        editor.putString("name", content);
                        editor.apply();
                        Global.username = content;
                        View headerView = navView.getHeaderView(0);
                        TextView textview = (TextView) headerView.findViewById(R.id.user);
                        textview.setText(Global.username);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }


}
