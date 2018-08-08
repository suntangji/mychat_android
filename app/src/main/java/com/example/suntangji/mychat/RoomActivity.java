package com.example.suntangji.mychat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class RoomActivity extends AppCompatActivity {
    private static final String TAG = "DEBUG";

    // 连接服务器

    private Socket socket;
    private Thread sendThread;
    private Thread recvThread;
    private BufferedReader in;
    private OutputStream out;
    private List<Msg> msgList = new ArrayList<>();
    private Deque<Msg> msgDeque = new ArrayDeque<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    public static int roomId;
    private boolean isConnect = true;
    private static final int UPDATE = 1;
    private static final int ERROR = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE:
                    // g更新 UI
                    Msg m = new Msg(msg.obj.toString(), msg.arg1);
                    Log.e(TAG, "handleMessage: " + m.getContent() );
                    msgList.add(m);
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);

                    break;
                case ERROR:
                    showError();
                    break;
                    default:
                        break;
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        roomId = intent.getIntExtra("ROOM_ID", -1);
        Log.e(TAG, roomId +"");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        connectThread.start();

        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SEND);
                    msgList.add(msg);

                    synchronized (msgDeque) {
                        msgDeque.offer(msg);
                        msgDeque.notify();
                    }


                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    inputText.setText("");
                }

            }
        });

    }

    public void Destory() {
        if (isConnect) {
            sendThread.interrupt();
            recvThread.interrupt();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Destory();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Destory();
                break;
            case R.id.quit:
                Toast.makeText(RoomActivity.this,"quit",Toast.LENGTH_SHORT ).show();
                Destory();
                break;
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Destory();
    }

    private void initClient() {
        Log.w(TAG, "initClient: ");
        recvThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Log.e(TAG, "run: recv");
                while (!isInterrupted()) {
                    String recvMsg = "";
                    String line;
                    try {
                        while ((line = in.readLine())!= null){
                            recvMsg += line;
                            if (line.equals("}")) {
                                break;
                            }
                        }
                    }catch (IOException e) {
                        Log.e(TAG, "run: recv exception");
                        e.printStackTrace();
                        return;
                    }
                    Log.e(TAG, recvMsg );
                    Gson gson=new Gson();
                    Json json = gson.fromJson(recvMsg, Json.class);
                    if (roomId == json.getRoom_id()) {
                        Message msg = new Message();
                        msg.what = UPDATE;
                        msg.arg1 = Msg.TYPE_RECEIVED;
                        msg.obj = recvMsg;
                        handler.sendMessage(msg);
                    }


                }
            }
        };

        sendThread = new Thread() {
            @Override
            public void run() {
                Log.e(TAG, "run: snedThread");
                Json default_json = new Json();
                default_json.setCmd(1);
                default_json.setName(Global.username);
                default_json.setRoom_id(roomId);
                default_json.setTo("everyone");
                default_json.setContent("大家好，我是" + Global.username);
                Gson default_gson = new Gson();
                String default_jsonMsg = default_gson.toJson(default_json);

                try {
                    out.write(default_jsonMsg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                while (!isInterrupted()) {
                    synchronized (msgDeque) {
                        while (msgDeque.size() == 0) {
                            try {
                                msgDeque.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Log.e(TAG, "run: send exception");
                                return;
                                // msgDeque.notify();
                            }
                        }

                        Msg msg = msgDeque.poll();
                        Json json = new Json();
                        json.setCmd(1);
                        json.setName(Global.username);
                        json.setRoom_id(roomId);
                        json.setTo("everyone");
                        json.setContent(msg.getContent());
                        Gson gson = new Gson();
                        String jsonMeg = gson.toJson(json);
                        Log.e("json", jsonMeg);
                        try {
                            out.write(jsonMeg.getBytes());
                        } catch (IOException e) {
                            Log.e(TAG, "run: send write exception");
                            e.printStackTrace();
                            return;
                        }

                    }
                }
            }
        };
        sendThread.start();
        recvThread.start();
    }

    private Thread connectThread = new Thread() {
        @Override
        public void run() {
            try {
                Log.w(TAG, "run: ");

                socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress("suntangji.me", 5000);
                socket.connect(socketAddress, 5*1000);//over 5sec error
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = socket.getOutputStream();
                initClient();

            } catch (Exception e) {
                Log.d(TAG, "run: NetworkException");
                isConnect = false;
                Message msg = new Message();
                msg.what = ERROR;
                handler.sendMessage(msg);
            } finally {
                Log.e(TAG, "run: final");
            }
        }

    };
    private void showError() {
        Log.w(TAG, "showError: ");
        AlertDialog dialog = new AlertDialog.Builder(this)
//                    .setIcon(R.mipmap.icon)//设置标题的图片
                .setTitle("提示")//设置对话框的标题
                .setMessage("无法连接服务器")//设置对话框的内容
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
                        finish();
                    }
                }).create();
        dialog.show();
    }

}
