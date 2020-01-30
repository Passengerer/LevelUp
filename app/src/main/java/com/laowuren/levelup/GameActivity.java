package com.laowuren.levelup;

import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.MyImageView;
import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.CodeUtil;
import com.laowuren.levelup.utils.ResourceUtil;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private SocketThread sThread;
    private ArrayList<Card> handCards;

    private TextView scoreText;
    private LinearLayout handCardsLayout;

    private int imageWidth;
    private int imageHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("GameActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        scoreText = (TextView)findViewById(R.id.text_score);
        handCardsLayout = (LinearLayout)findViewById(R.id.hand_cards_region);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        handCards = new ArrayList<>();
        imageHeight = dm.heightPixels / 9;
        imageWidth = dm.widthPixels / 8;
        //imageHeight = 200;
        //imageWidth = 140;
        //Log.d("height", "" + dm.heightPixels);
        //Log.d("width", "" + dm.widthPixels);
        setUI();
        initSocket();
    }

    protected void initSocket() {
        try {
            sThread = new SocketThread();
        } catch (Exception e) {
            Log.d("GameActivity", "socket exception");
            e.printStackTrace();
            Toast.makeText(GameActivity.this, "连接服务器失败", Toast.LENGTH_LONG).show();
            GameActivity.this.finish();
        }

        sThread.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte code = (byte) msg.obj;
                Card card = CodeUtil.getCardFromCode(code);
                addCard(card);
                Log.d("GameActivity", card.toString());
            }
        };
        sThread.start();
        Log.d("GameActivity", "socket thread start");
    }

    protected void addCard(Card card){
        MyImageView imageView = new MyImageView(GameActivity.this);
        imageView.card = card;
        handCards.add(card);

        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(imageWidth, imageHeight);
        if (handCards.size() > 1){
            params.leftMargin = imageWidth / 3 * 2 * -1;
        }
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(card.toString().toLowerCase())));

        handCardsLayout.addView(imageView);
    }

    protected void setUI(){
        Log.d("GameActivity", "setUI");
        // 隐藏标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        // 隐藏状态栏和虚拟按键
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        // 设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
