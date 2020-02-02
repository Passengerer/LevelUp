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
import com.laowuren.levelup.utils.CodeUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class JoinActivity extends AppCompatActivity {

    private SocketThread sThread;

    private EditText roomIdEdit;
    private Button joinButton;
    private ProgressBar progressBar;

    private String fullStr;
    private String noRoomIdStr;
    private String successStr;

    private String inputErrorStr;
    private String noInputStr;

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
                    try{
                        String str = URLEncoder.encode("请输入房间号", "GBK");
                        noInputStr = URLDecoder.decode(str, "UTF-8");
                    }catch (UnsupportedEncodingException e){
                        noInputStr = "please input room id";
                    }finally {
                        Toast.makeText(JoinActivity.this, noInputStr, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                try{
                    roomId = Integer.parseInt(roomIdEdit.getText().toString());
                }catch (Exception e){
                    try{
                        String str = URLEncoder.encode("输入错误", "GBK");
                        inputErrorStr = URLDecoder.decode(str, "UTF-8");
                    }catch (UnsupportedEncodingException ex){
                        inputErrorStr = "input error";
                    }finally {
                        Toast.makeText(JoinActivity.this, inputErrorStr, Toast.LENGTH_SHORT).show();
                    }
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
                    try{
                        String str = URLEncoder.encode("房间人数已满", "GBK");
                        fullStr = URLDecoder.decode(str, "UTF-8");
                    }catch (UnsupportedEncodingException e){
                        fullStr = "full";
                    }finally {
                        Toast.makeText(JoinActivity.this, fullStr, Toast.LENGTH_SHORT).show();
                    }
                }
                else if (code == CodeUtil.FAILED3){
                    try{
                        String str = URLEncoder.encode("无该房间", "GBK");
                        noRoomIdStr = URLDecoder.decode(str, "UTF-8");
                    }catch (UnsupportedEncodingException e){
                        noRoomIdStr = "no such room";
                    }finally {
                        Toast.makeText(JoinActivity.this, noRoomIdStr, Toast.LENGTH_SHORT).show();
                    }
                }
                else if (CodeUtil.getHeader(code) == CodeUtil.ROOMID) {
                    playerId = CodeUtil.getTail(code) & 0x03;
                    try {
                        String str = URLEncoder.encode("加入成功，等待其他玩家", "GBK");
                        successStr = URLDecoder.decode(str, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        successStr = "success";
                    } finally {
                        Toast.makeText(JoinActivity.this, successStr, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.VISIBLE);
                        joinButton.setEnabled(false);
                    }
                }
            }
        };
        sThread.start();
    }
}
