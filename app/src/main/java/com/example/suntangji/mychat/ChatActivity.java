package com.example.suntangji.mychat;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "DEBUG";

    // 连接服务器

    private Socket socket;
    private BufferedReader in;
    private OutputStream out;
    private List<Msg> msgList = new ArrayList<>();
    private Deque<Msg> msgDeque = new ArrayDeque<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private static ReadWriteLock rwl = new ReentrantReadWriteLock();

    private static final int UPDATE = 1;
    private static final int ERROR = 0;
    private Handler mHandler;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        HandlerThread thread = new HandlerThread("MyHandlerThread");
        thread.start();//创建一个HandlerThread并启动它
        mHandler = new Handler(thread.getLooper());//使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
        mHandler.post(connectThread);//将线程post到Handler中


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
            mHandler.removeCallbacks(sendThread);
            mHandler.removeCallbacks(recvThread);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finish();
                break;
            case R.id.quit:
                Toast.makeText(ChatActivity.this,"quit",Toast.LENGTH_SHORT ).show();
                break;
        }
        return true;

    }

    private Runnable recvThread = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run: recv");
            while (true) {
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
                    Log.e(TAG, "run: exception");
                    e.printStackTrace();
                }
                Log.e(TAG, recvMsg );

                Message msg = new Message();
                msg.what = UPDATE;
                msg.arg1 = Msg.TYPE_RECEIVED;
                msg.obj = recvMsg;
                handler.sendMessage(msg);

            }
        }
    };

    private Runnable sendThread = new Runnable() {
        @Override
        public void run() {
            Json default_json = new Json();
            default_json.setCmd("1");
            default_json.setName("test2");
            default_json.setTo("test");
            default_json.setContent("大家好，我是" + Global.username);
            Gson default_gson = new Gson();
            String default_jsonMsg = default_gson.toJson(default_json);

            try {
                out.write(default_jsonMsg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }


            while (true) {
                synchronized (msgDeque) {
                    while (msgDeque.size() == 0) {
                        try {
                            msgDeque.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            msgDeque.notify();
                        }
                    }

                    Msg msg = msgDeque.poll();
                    Json json = new Json();
                    json.setCmd("1");
                    json.setName("test2");
                    json.setTo("test");
                    json.setContent(msg.getContent());
                    Gson gson = new Gson();
                    String jsonMeg = gson.toJson(json);
                    Log.e("json", jsonMeg);
                    try {
                        out.write(jsonMeg.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };

    private void initClient() {
        Log.w(TAG, "initClient: ");
        mHandler.post(sendThread);
        mHandler.post(recvThread);
    }

    private Runnable connectThread = new Runnable() {
        @Override
        public void run() {
            try {
                Log.w(TAG, "run: ");
//                    socket = new Socket("47.93.204.99",5000);
//                    socket.setSoTimeout(10000);
//                    if (socket.isConnected() && !socket.isClosed()) {
//                       in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                       out = socket.getOutputStream();
//                        Log.w(TAG, "run: before init");
//                        initClient();
//                   }

                socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress("suntangji.me", 5000);
                socket.connect(socketAddress, 5*1000);//over 5sec error
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = socket.getOutputStream();
                initClient();

            } catch (Exception e) {
                Log.d(TAG, "run: NetworkException");
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
//                            Toast.makeText(ChatActivity.this, "点击了取消按钮", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(ChatActivity.this, "点击了确定的按钮", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        dialog.show();
    }

}
