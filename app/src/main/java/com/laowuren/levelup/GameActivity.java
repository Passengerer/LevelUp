package com.laowuren.levelup;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.CardComparator;
import com.laowuren.levelup.others.MyImageView;
import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.CodeUtil;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private SocketThread sThread;

    private ArrayList<Byte> handCards;
    private ArrayList<Byte> playCards;
    private byte zhu = 0x15;
    private CardComparator com;

    private TextView scoreText;
    private LinearLayout handCardsLayout;
    private Button showHeart;
    private Button showClub;
    private Button showDiamond;
    private Button showSpade;

    private int imageWidth;
    private int imageHeight;
    private int leftMargin;
    private int topMargin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("GameActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        scoreText = (TextView)findViewById(R.id.text_score);
        showHeart = (Button)findViewById(R.id.show_heart);
        showClub = (Button)findViewById(R.id.show_club);
        showDiamond = (Button)findViewById(R.id.show_diamond);
        showSpade = (Button)findViewById(R.id.show_spade);
        handCardsLayout = (LinearLayout)findViewById(R.id.hand_cards_region);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        handCards = new ArrayList<>();
        playCards = new ArrayList<>();
        imageHeight = dm.heightPixels / 9;
        imageWidth = dm.widthPixels / 8;
        leftMargin = imageWidth / 3 * 2 * -1;
        topMargin = imageHeight / 4 * 1;

        com = new CardComparator();
        com.setZhu(CodeUtil.getCardFromCode(zhu));

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
                addCard(code);
            }
        };
        sThread.start();
        Log.d("GameActivity", "socket thread start");
    }

    protected void addCard(byte code){
        Card card = CodeUtil.getCardFromCode(code);
        Log.d("GameActivity", card.toString());
        // 有当前等级牌可以亮花色
        if (CodeUtil.getTail(code) == CodeUtil.getTail(zhu)){
            Log.d("test", "" + code);
            switch (CodeUtil.getHigher(code)){
                case 0:
                    showHeart.setEnabled(true);
                    break;
                case 1:
                    showClub.setEnabled(true);
                    break;
                case 2:
                    showDiamond.setEnabled(true);
                    break;
                case 3:
                    showSpade.setEnabled(true);
                    break;
            }
        }
        int index = getIndex(code);

        MyImageView imageView = new MyImageView(GameActivity.this);
        imageView.setImg(code);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.play == false){
                    playCards.add(imageView.code);
                    imageView.offsetTopAndBottom(-topMargin);
                    imageView.play = true;
                }else{
                    playCards.remove((Object)imageView.code);
                    imageView.offsetTopAndBottom(topMargin);
                    imageView.play = false;
                }
            }
        });

        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(imageWidth, imageHeight);
        if (index > 0){
            params.leftMargin = leftMargin;
            imageView.setLayoutParams(params);
            handCardsLayout.addView(imageView, index);
        }else if(index == 0) {
            params.leftMargin = 0;
            imageView.setLayoutParams(params);
            handCardsLayout.addView(imageView, index);

            if (!handCards.isEmpty()) {
                MyImageView secondCard = (MyImageView) handCardsLayout.getChildAt(1);
                params.leftMargin = leftMargin;
                secondCard.setLayoutParams(params);
            }
        }
        handCards.add(index, code);
    }

    protected int getIndex(byte code){
        int index = 0;
        if (handCards.isEmpty()){
            return 0;
        }
        for (byte b : handCards){
            Card c1 = CodeUtil.getCardFromCode(code);
            Card c2 = CodeUtil.getCardFromCode(b);
            if (com.compare(c1, c2) < 0)
                ++index;
            else break;
        }
        return index;
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
