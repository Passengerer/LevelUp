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

import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.BitmapManager;
import com.laowuren.levelup.utils.CodeUtil;

public class JoinActivity extends AppCompatActivity {

    private SocketThread sThread;

    private final String inputId = "请输入房间号";
    private final String inputError = "输入错误";
    private final String fullPeople = "房间人数已满";
    private final String noSuchRoom = "没有该房间";
    private final String successStr = "加入成功，等待其他玩家";

    private EditText roomIdEdit;
    private Button joinButton;
    private ProgressBar progressBar;

    private int playerId;

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
                int roomId = -1;
                if (TextUtils.isEmpty(roomIdEdit.getText())){
                    Toast.makeText(JoinActivity.this, inputId, Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    roomId = Integer.parseInt(roomIdEdit.getText().toString());
                }catch (Exception e){
                    Toast.makeText(JoinActivity.this, inputError, Toast.LENGTH_SHORT).show();
                    return;
                }
                sThread.send((byte)(CodeUtil.ROOMID | (byte)roomId));
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
            Toast.makeText(JoinActivity.this, "disconnected", Toast.LENGTH_LONG).show();
            JoinActivity.this.finish();
        }

        sThread.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte code = (byte)msg.obj;
                if (code == CodeUtil.READY){
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(JoinActivity.this, GameActivity.class);
                    intent.putExtra("playerId", playerId);
                    startActivity(intent);
                }
                else if (code == CodeUtil.FAILED2){
                    Toast.makeText(JoinActivity.this, fullPeople, Toast.LENGTH_SHORT).show();
                }
                else if (code == CodeUtil.FAILED3){
                    Toast.makeText(JoinActivity.this, noSuchRoom, Toast.LENGTH_SHORT).show();
                }
                else if (CodeUtil.getHeader(code) == CodeUtil.ROOMID) {
                    playerId = CodeUtil.getTail(code) & 0x03;
                    if (playerId != 3)
                        Toast.makeText(JoinActivity.this, successStr, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.VISIBLE);
                    joinButton.setEnabled(false);
                }
            }
        };
        sThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sThread.stop = true;
    }
}
