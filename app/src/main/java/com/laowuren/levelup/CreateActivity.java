package com.laowuren.levelup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.CodeUtil;

public class CreateActivity extends AppCompatActivity {

    private SocketThread sThread = null;

    private TextView roomIdText;
    private TextView player2Status;
    private TextView player3Status;
    private TextView player4Status;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CreateActivity", "onCreate");
        setContentView(R.layout.activity_create);

        roomIdText = (TextView)findViewById(R.id.text_roomId);
        playButton = (Button)findViewById(R.id.button_play);
        player2Status = (TextView)findViewById(R.id.text_player2);
        player3Status = (TextView)findViewById(R.id.text_player3);
        player4Status = (TextView)findViewById(R.id.text_player4);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });
        createRoom();
    }

    protected void createRoom() {
        try {
            sThread = new SocketThread();
        } catch (Exception e) {
            Log.d("CreateActivity", "socket exception");
            e.printStackTrace();
            Toast.makeText(CreateActivity.this, "连接服务器失败", Toast.LENGTH_LONG).show();
            CreateActivity.this.finish();
        }

        sThread.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte instruct = (byte)msg.obj;
                if (instruct == CodeUtil.READY) {
                    Intent intent = new Intent(CreateActivity.this, GameActivity.class);
                    startActivity(intent);
                } else if (CodeUtil.getHeader(instruct) == CodeUtil.ROOMID) {
                    roomIdText.setText("房间号: " + (CodeUtil.getTail(instruct)));
                }
            }
        };
        sThread.start();
        sThread.send(CodeUtil.CREATE);
    }
}
