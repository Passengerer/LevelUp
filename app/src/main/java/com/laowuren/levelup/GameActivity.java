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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.CardComparator;
import com.laowuren.levelup.others.MyImageView;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.CodeUtil;
import com.laowuren.levelup.utils.ResourceUtil;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketThread sThread;

    private int playerId;     // 玩家id
    private ArrayList<Byte> handCards;
    private ArrayList<Byte> duizi;      // 对子
    private int zhuCount = 0;
    private int heartCount = 0;
    private int clubCount = 0;
    private int diamondCount = 0;
    private int spadeCount = 0;
    private ArrayList<Byte> playCards;
    private byte zhu = 0x00;
    private byte suit = 0x10;
    private boolean selfFan = false;
    private boolean selfTurn = false;
    private boolean dapaiStat = false;
    private boolean maipaiStat = false;
    private boolean fapaiStat = true;
    private boolean isZhuangJia = false;

    private boolean hasSetZhu = false;
    private boolean hasDing = false;
    private boolean hasFanWang = false;
    private boolean hasFanHeart = false;
    private boolean hasFanClub = false;
    private boolean hasFanDiamond = false;
    private boolean hasFanSpade = false;

    private CardComparator com;

    private TextView scoreText;
    private LinearLayout handCardsLayout;
    private ArrayList<ImageView> playImages;
    private Button showHeart;
    private Button showClub;
    private Button showDiamond;
    private Button showSpade;
    private Button showJokerRed;
    private Button showJokerBlack;
    private ImageView zhuImage;
    private Button noZhuFan;
    private Button maipaiButton;
    private Button chupaiButton;

    private DisplayMetrics dm;
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
        showHeart.setOnClickListener(this);
        showClub = (Button)findViewById(R.id.show_club);
        showClub.setOnClickListener(this);
        showDiamond = (Button)findViewById(R.id.show_diamond);
        showDiamond.setOnClickListener(this);
        showSpade = (Button)findViewById(R.id.show_spade);
        showSpade.setOnClickListener(this);
        showJokerRed = (Button)findViewById(R.id.show_joker_red);
        showJokerRed.setOnClickListener(this);
        showJokerBlack = (Button)findViewById(R.id.show_joker_black);
        showJokerBlack.setOnClickListener(this);
        zhuImage = (ImageView)findViewById(R.id.image_zhu);
        noZhuFan = (Button)findViewById(R.id.no_fan);
        noZhuFan.setOnClickListener(this);
        maipaiButton = (Button)findViewById(R.id.button_maipai);
        maipaiButton.setOnClickListener(this);
        chupaiButton = (Button)findViewById(R.id.button_chupai);
        chupaiButton.setOnClickListener(this);
        handCardsLayout = (LinearLayout)findViewById(R.id.hand_cards_region);
        playImages = new ArrayList<>();
        playerId = getIntent().getIntExtra("playerId", -1);
        Log.d("playerId", playerId + "");
        handCards = new ArrayList<>();
        duizi = new ArrayList<>();
        playCards = new ArrayList<>();
        dm = getResources().getDisplayMetrics();
        imageHeight = dm.heightPixels / 9;
        imageWidth = (int)(dm.widthPixels / 8.5);
        leftMargin = imageWidth / 4 * 3 * -1;
        topMargin = imageHeight / 4;

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
                if (CodeUtil.getHeader(code) == CodeUtil.ZHUSUIT){
                    Log.d("zhusuit", "zhusuit");
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)(suit << 4 | zhu);
                    // 设置主的图片
                    setZhuImage(zhu);
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    if (showSuitId == playerId){
                        selfFan = true;
                        Log.d("self", "true");
                        // 自己亮的花色，不能再亮，有对可以定
                        setShowButtonsFalse();
                        if (duizi.contains(zhu)){
                            switch (suit){
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
                    }else{ // 别人亮的花色，如果有对可以反，没有则不能亮
                        selfFan = false;
                        Log.d("self", "false");
                        setShowButtonsFalse();
                        if (duizi.contains((byte)(0x00 << 4) | (byte)zhu)){
                            showHeart.setEnabled(true);
                        }
                        if (duizi.contains((byte)(0x01 << 4) | (byte)zhu)){
                            showClub.setEnabled(true);
                        }
                        if (duizi.contains((byte)(0x02 << 4) | (byte)zhu)){
                            showDiamond.setEnabled(true);
                        }
                        if (duizi.contains((byte)(0x03 << 4) | (byte)zhu)){
                            showSpade.setEnabled(true);
                        }
                        if (duizi.contains(0x4d)){
                            showJokerBlack.setEnabled(true);
                        }
                        if (duizi.contains(0x4e)){
                            showJokerRed.setEnabled(true);
                        }
                    }
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.FANSUIT){
                    Log.d("fansuit", "fansuit");
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)(suit << 4 | zhu);
                    setZhuImage(zhu);
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    if (showSuitId == playerId){
                        selfFan = true;
                        Log.d("self", "true");
                        // 自己反的花色，不能再反
                        setShowButtonsFalse();
                        showJokerBlack.setEnabled(false);
                        showJokerRed.setEnabled(false);
                    }else{ // 别人反的花色，如果有对可以再反
                        selfFan = false;
                        Log.d("self", "false");
                        setShowButtonsFalse();
                        if (duizi.contains((byte)(0x00 << 4) | (byte)zhu)){
                            if (!hasFanHeart)
                                showHeart.setEnabled(true);
                        }
                        if (duizi.contains((byte)(0x01 << 4) | (byte)zhu)){
                            if (!hasFanClub)
                                showClub.setEnabled(true);
                        }
                        if (duizi.contains((byte)(0x02 << 4) | (byte)zhu)){
                            if (!hasFanDiamond)
                                showDiamond.setEnabled(true);
                        }
                        if (duizi.contains((byte)(0x03 << 4) | (byte)zhu)){
                            if (!hasFanSpade)
                                showSpade.setEnabled(true);
                        }
                        if (duizi.contains(0x4d)){
                            showJokerBlack.setEnabled(true);
                        }
                        if (duizi.contains(0x4e)){
                            showJokerRed.setEnabled(true);
                        }
                    }
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.DINGSUIT){
                    Log.d("dingsuit", "dingsuit");
                    hasDing = true;
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)(suit << 4 | zhu);
                    setZhuImage(zhu);
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    if (showSuitId == playerId){
                        selfFan = true;
                        Log.d("self", "true");
                        setShowButtonsFalse();
                    }else{ // 别人定的花色，如果有对王可以反
                        selfFan = false;
                        Log.d("self", "false");
                        setShowButtonsFalse();
                        if (duizi.contains(0x4d)){
                            showJokerBlack.setEnabled(true);
                        }
                        if (duizi.contains(0x4e)){
                            showJokerRed.setEnabled(true);
                        }
                    }
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.FANWANG){
                    Log.d("fanwang", "fanwang");
                    hasFanWang = true;
                    int wang = (CodeUtil.getTail(code) >> 2);
                    suit = (byte)(wang == 0 ? 0x04 : 0x05);
                    if (wang == 0) {
                        setZhuImage((byte)0x4d);
                    }else {
                        setZhuImage((byte)0x4e);
                    }
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    if (showSuitId == playerId){
                        selfFan = true;
                        Log.d("self", "true");
                        setShowButtonsFalse();
                        showJokerBlack.setEnabled(false);
                        showJokerRed.setEnabled(false);
                    }else{ // 别人反小王，大王可以反
                        selfFan = false;
                        Log.d("self", "false");
                        setShowButtonsFalse();
                        showJokerBlack.setEnabled(false);
                        if (wang == 0 && duizi.contains(0x4e)){
                            showJokerRed.setEnabled(true);
                        }
                    }
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.BIZHUANG){
                    Log.d("bizhuang", "bizhuang");
                    if (!isZhuangJia){
                        setShowButtonsFalse();
                        showJokerBlack.setEnabled(false);
                        showJokerRed.setEnabled(false);
                    }
                    else {
                        boolean canFan = false;
                        if (hasCard((byte)((0x00 << 4) | (byte)zhu))){
                            canFan = true;
                        }
                        if (hasCard((byte)((0x01 << 4) | (byte)zhu))){
                            canFan = true;
                        }
                        if (hasCard((byte)((0x02 << 4) | (byte)zhu))){
                            canFan = true;
                        }
                        if (hasCard((byte)((0x03 << 4) | (byte)zhu))){
                            canFan = true;
                        }
                        if (!canFan){
                            noZhuFan.setVisibility(View.VISIBLE);
                        }
                    }
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.STARTTURN){
                    Log.d("start turn", "start turn");
                    fapaiStat = false;
                    maipaiStat = true;
                    dapaiStat = false;
                    if (CodeUtil.getTail(code) == playerId){
                        selfTurn = true;
                        setShowButtonsFalse();
                        showJokerBlack.setEnabled(false);
                        showJokerRed.setEnabled(false);
                        maipaiButton.setVisibility(View.VISIBLE);
                    }else{
                        selfTurn = false;
                        maipaiButton.setVisibility(View.GONE);
                        chupaiButton.setVisibility(View.GONE);
                        updateImages();
                    }
                    return;
                }
                addCard(code);
            }
        };
        sThread.start();
        Log.d("GameActivity", "socket thread start");
    }

    protected void updateImages(){
        imageWidth = dm.widthPixels / 8;
        leftMargin = imageWidth / 3 * 2 * -1;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
        params.leftMargin = leftMargin;
        for (int i = 0; i < handCardsLayout.getChildCount(); ++i){
            ImageView view = (ImageView) handCardsLayout.getChildAt(i);
            view.setLayoutParams(params);
        }
    }

    protected void setShowButtonsFalse(){
        showHeart.setEnabled(false);
        showClub.setEnabled(false);
        showDiamond.setEnabled(false);
        showSpade.setEnabled(false);
        showJokerBlack.setEnabled(false);
        showJokerRed.setEnabled(false);
    }

    protected void setShowButtonsGone(){
        showHeart.setVisibility(View.GONE);
        showClub.setVisibility(View.GONE);
        showDiamond.setVisibility(View.GONE);
        showSpade.setVisibility(View.GONE);
        showJokerBlack.setVisibility(View.GONE);
        showJokerRed.setVisibility(View.GONE);
    }

    protected void addCard(byte code){
        Card card = CodeUtil.getCardFromCode(code);
        Log.d("GameActivity add card", card.toString());
        Rank _rank = card.getRank();
        /*
        if (_rank == Rank.Joker_red || _rank == Rank.Joker_black || _rank == Rank.Deuce ||
                CodeUtil.getTail(code) == CodeUtil.getTail(zhu)){
            ++zhuCount;
        }else if (CodeUtil.getHigher(code) == 0){
            ++heartCount;
        }else if (CodeUtil.getHigher(code) == 1){
            ++clubCount;
        }else if (CodeUtil.getHigher(code) == 2){
            ++diamondCount;
        }else if (CodeUtil.getHigher(code) == 3){
            ++spadeCount;
        }
        */
        // 有当前等级牌可以亮花色
        if (CodeUtil.getTail(code) == CodeUtil.getTail(zhu)){
            if (selfFan == false) {
                switch (CodeUtil.getHigher(code)) {
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
        }
        int index = getIndex(code);

        MyImageView imageView = new MyImageView(GameActivity.this);
        imageView.setImg(code);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fapaiStat) return;
                if (imageView.play == false){
                    playCards.add(imageView.code);
                    playImages.add(imageView);
                    imageView.offsetTopAndBottom(-topMargin);
                    imageView.play = true;
                }else{
                    playCards.remove((Object)imageView.code);
                    playImages.remove(imageView);
                    imageView.offsetTopAndBottom(topMargin);
                    imageView.play = false;
                }
                if (maipaiStat){
                    if (playCards.size() == 8)
                        maipaiButton.setEnabled(true);
                    else
                        maipaiButton.setEnabled(false);
                }
                if (dapaiStat){
                    if (playCards.isEmpty())
                        chupaiButton.setEnabled(false);
                    else
                        chupaiButton.setEnabled(true);
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
        if (handCards.contains(code)){
            duizi.add(code);
            if (hasDuizi((byte)((0x00 << 4) | (byte)zhu))){
                if (!hasFanHeart)
                    showHeart.setEnabled(true);
            }
            if (hasDuizi((byte)((0x01 << 4) | (byte)zhu))){
                if (!hasFanClub)
                    showClub.setEnabled(true);
            }
            if (hasDuizi((byte)((0x02 << 4) | (byte)zhu))){
                if (!hasFanDiamond)
                    showDiamond.setEnabled(true);
            }
            if (hasDuizi((byte)((0x03 << 4) | (byte)zhu))){
                if (!hasFanSpade)
                    showSpade.setEnabled(true);
            }
        }
        handCards.add(index, code);
    }

    protected boolean hasCard(byte code){
        return handCards.contains(code);
    }

    protected boolean hasDuizi(byte code){
        return duizi.contains(code);
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

    @Override
    public void onClick(View v) {
        byte header;
        if (hasSetZhu == false){
            header = CodeUtil.ZHUSUIT;
        }else{
            header = CodeUtil.FANSUIT;
        }
        switch (v.getId()){
            case R.id.show_heart:
                sThread.send((byte)(header | (byte)0x00));
                if (header == CodeUtil.FANSUIT) {
                    hasFanHeart = true;
                    findViewById(R.id.show_heart).setEnabled(false);
                }
                break;
            case R.id.show_club:
                sThread.send((byte)(header | (byte)0x01));
                if (header == CodeUtil.FANSUIT) {
                    hasFanClub = true;
                    findViewById(R.id.show_club).setEnabled(false);
                }
                break;
            case R.id.show_diamond:
                sThread.send((byte)(header | (byte)0x02));
                if (header == CodeUtil.FANSUIT) {
                    hasFanDiamond = true;
                    findViewById(R.id.show_diamond).setEnabled(false);
                }
                break;
            case R.id.show_spade:
                sThread.send((byte)(header | (byte)0x03));
                if (header == CodeUtil.FANSUIT) {
                    hasFanSpade = true;
                    findViewById(R.id.show_spade).setEnabled(false);
                }
                break;
            case R.id.show_joker_black:
                sThread.send((byte)(header | (byte)0x04));
                break;
            case R.id.show_joker_red:
                sThread.send((byte)(header | (byte)0x05));
                break;
            case R.id.no_fan:
                sThread.send(CodeUtil.BIZHUANG);
                noZhuFan.setEnabled(false);
                break;
            case R.id.button_maipai:
                for (byte b : playCards){
                    Log.d("send ", "" + b);
                    Log.d("send card", CodeUtil.getCardFromCode(b).toString());
                    sThread.send(b);
                }
                handCards.removeAll(playCards);
                for (int i = 0; i < playImages.size(); ++i) {
                    handCardsLayout.removeView(playImages.get(i));
                }
                setSelfTurnFalse();
                updateImages();
                break;
            case R.id.button_chupai:
                break;
        }
    }

    protected void setSelfTurnFalse(){
        selfTurn = false;
        maipaiButton.setVisibility(View.GONE);
        chupaiButton.setVisibility(View.GONE);
    }

    protected void setZhuImage(byte code){
        String zhuName = CodeUtil.getCardFromCode(code).toString().toLowerCase();
        zhuImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(zhuName)));
    }
}
