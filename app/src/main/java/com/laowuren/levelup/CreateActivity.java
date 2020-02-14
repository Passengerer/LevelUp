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

public class CreateActivity extends AppCompatActivity {

    private SocketThread sThread = null;

    private TextView roomIdText;
    private ProgressBar progressBar;

    private final String waitStr = "等待其他玩家";
    private final String errorStr = "服务器已满";

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
                    Toast.makeText(CreateActivity.this, waitStr, Toast.LENGTH_SHORT).show();
                } else if (instruct == CodeUtil.FAILED1){
                    Toast.makeText(CreateActivity.this, errorStr, Toast.LENGTH_SHORT).show();
                }
            }
        };
        sThread.start();
        sThread.send(CodeUtil.CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sThread.stop = true;
    }
}
