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

import com.laowuren.levelup.others.MyMessage;
import com.laowuren.levelup.thread.SocketThread;

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
                if (sThread != null && sThread.isAlive()){
                    sThread.stop = true;
                }
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
                if (msg.what == SocketThread.DISCONNECTED) {
                    Log.d("CreateActivity", "disconnected");
                    Toast.makeText(CreateActivity.this, "与服务器断开连接", Toast.LENGTH_LONG).show();
                    CreateActivity.this.finish();
                    sThread.stop = true;
                } else if (msg.what == SocketThread.MYMESSAGE) {
                    MyMessage message = (MyMessage) msg.obj;
                    switch (message.getWhat()) {
                        case MyMessage.TEXT:
                            Log.d("CreateActivity", message.getText());
                            if ("player2".equals(message.getText())){
                                player2Status.setText("玩家2: 就绪");
                            }
                            else if ("player3".equals(message.getText())){
                                player3Status.setText("玩家3: 就绪");
                            }
                            else if ("player4".equals(message.getText())){
                                player4Status.setText("玩家4: 就绪");
                                playButton.setEnabled(true);
                            }
                            else {
                                try {
                                    int roomId = Integer.parseInt(message.getText());
                                    roomIdText.setText("房间号: " + roomId);
                                } catch (Exception e) {
                                    Log.d("CreateActivity", "input exception");
                                }
                            }
                    }
                }
            }
        };
        sThread.start();
        sThread.send(new MyMessage(MyMessage.TEXT, "create room", null, 0));
    }
}
