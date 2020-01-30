package com.laowuren.levelup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.laowuren.levelup.others.MyMessage;
import com.laowuren.levelup.thread.SocketThread;

public class JoinActivity extends AppCompatActivity {

    private SocketThread sThread;

    private EditText roomIdEdit;
    private Button joinButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        roomIdEdit = (EditText)findViewById(R.id.edit_roomId);
        joinButton = (Button)findViewById(R.id.button_sure);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(roomIdEdit.getText())){
                    Toast.makeText(JoinActivity.this, "请输入房间号", Toast.LENGTH_SHORT).show();
                    return;
                }
                //sThread.send(roomIdEdit.getText().toString());
            }
        });
        joinRoom();
    }

    protected void joinRoom() {
        try {
            sThread = new SocketThread();
        } catch (Exception e) {
            Log.d("JoinActivity", "socket exception");
            e.printStackTrace();
            Toast.makeText(JoinActivity.this, "连接服务器失败", Toast.LENGTH_LONG).show();
            JoinActivity.this.finish();
        }

        sThread.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                MyMessage message = (MyMessage) msg.obj;
                switch (message.getWhat()) {
                    case MyMessage.TEXT:
                        if ("ready".equals(message.getText())) {
                            if (progressBar.getVisibility() == View.VISIBLE) {
                                progressBar.setVisibility(View.GONE);
                            }
                            Toast.makeText(JoinActivity.this, "游戏开始", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(JoinActivity.this, GameActivity.class);
                            startActivity(intent);
                        } else if ("input error".equals(message.getText())) {
                            Toast.makeText(JoinActivity.this, "房间不存在", Toast.LENGTH_SHORT).show();
                        } else if ("full".equals(message.getText())) {
                            Toast.makeText(JoinActivity.this, "房间人数已满", Toast.LENGTH_SHORT).show();
                        } else if ("success".equals(message.getText())) {
                            Toast.makeText(JoinActivity.this, "等待游戏开始", Toast.LENGTH_LONG).show();
                            if (progressBar.getVisibility() == View.GONE)
                                progressBar.setVisibility(View.VISIBLE);
                        }
                }
            }
        };
        sThread.start();
    }
}
