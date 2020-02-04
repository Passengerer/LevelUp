package com.laowuren.levelup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.CodeUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CreateActivity extends AppCompatActivity {

    private SocketThread sThread = null;

    private TextView roomIdText;
    private ProgressBar progressBar;

    private String waitStr;
    private String errorStr;

    private int playerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CreateActivity", "onCreate");
        setContentView(R.layout.activity_create);

        roomIdText = (TextView)findViewById(R.id.text_roomId);
        progressBar = (ProgressBar)findViewById(R.id.create_progress);
        createRoom();
    }

    protected void createRoom() {
        try {
            sThread = new SocketThread();
        } catch (Exception e) {
            Log.d("CreateActivity", "socket exception");
            e.printStackTrace();
            Toast.makeText(CreateActivity.this, "disconnected", Toast.LENGTH_LONG).show();
            CreateActivity.this.finish();
        }

        sThread.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte instruct = (byte)msg.obj;
                if (instruct == CodeUtil.READY) {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(CreateActivity.this, GameActivity.class);
                    intent.putExtra("playerId", playerId);
                    startActivity(intent);
                }
                else if (CodeUtil.getHeader(instruct) == CodeUtil.ROOMID) {
                    int roomId = CodeUtil.getTail(instruct) >> 2;
                    playerId = CodeUtil.getTail(instruct) & 0x03;
                    roomIdText.append("" + roomId);
                    try{
                        String str = URLEncoder.encode("等待其他玩家", "GBK");
                        waitStr = URLDecoder.decode(str, "UTF-8");
                    }catch (UnsupportedEncodingException e){
                        waitStr = "waiting";
                    }finally {
                        Toast.makeText(CreateActivity.this, waitStr, Toast.LENGTH_SHORT).show();
                    }
                } else if (instruct == CodeUtil.FAILED1){
                    try{
                        String str = URLEncoder.encode("服务器已满", "GBK");
                        errorStr = URLDecoder.decode(str, "UTF-8");
                    }catch (UnsupportedEncodingException e){
                        errorStr = "no room else";
                    }finally {
                        Toast.makeText(CreateActivity.this, errorStr, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        sThread.start();
        sThread.send(CodeUtil.CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sThread.send(CodeUtil.EXIT);
        sThread.stop = true;
    }
}
