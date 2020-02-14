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
import com.laowuren.levelup.others.CodeComparator;
import com.laowuren.levelup.others.MyImageView;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;
import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.CodeUtil;
import com.laowuren.levelup.utils.PlayRuler;
import com.laowuren.levelup.utils.ResourceUtil;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketThread sThread;
    private final String strBreakRules = "出牌犯规";

    private int playerId;     // 玩家id
    private int whoPlay;      // 当前出牌玩家id
    private int firstPlayerId;  // 每轮第一个出牌的玩家id
    private ArrayList<Byte> handCards;
    private ArrayList<Byte> duizi;      // 对子
    private ArrayList<Byte> firstCards;
    private int firstCardsCount;
    private int acceptCardsCount;
    private ArrayList<Byte> playCards;
    private byte zhu = -1;        // 有小主时存牌(花色和等级)，无小主时存等级
    private byte suit = 0x10;       // 存花色，4代表小王，5代表大王
    private PlayRuler ruler;
    private boolean selfFan = false;
    private boolean selfTurn = false;
    private GAMESTATE stat = GAMESTATE.INIT;
    private int zhuangJia = -1;
    private boolean isZhuangJia = false;
    private boolean isFirstGame = true;

    private boolean hasSetZhu = false;
    private boolean hasDing = false;
    private boolean hasFanWang = false;
    private boolean[] hasFanColor;
    private boolean bizhuang = false;
    private boolean selfMaipai = false;

    private CardComparator com;
    private CodeComparator codeCom;

    private TextView scoreText;
    private TextView ourLevelText;
    private TextView othersLevelText;
    private LinearLayout handCardsLayout;
    private LinearLayout[] playCardsLayouts;
    private ArrayList<ImageView> playImages;
    private LinearLayout.LayoutParams params;
    private LinearLayout.LayoutParams smallParams;
    private Button[] showButtons;
    private Button showBuFan;
    private ImageView zhuImage;
    private Button noZhuFan;
    private Button maipaiButton;
    private Button chupaiButton;

    private ImageView[] showImages;
    private ImageView[] turnImage;

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
        ourLevelText = (TextView)findViewById(R.id.text_level_self);
        othersLevelText = (TextView)findViewById(R.id.text_level_others);
        showButtons = new Button[6];
        showButtons[0] = (Button)findViewById(R.id.show_heart);
        showButtons[0].setOnClickListener(this);
        showButtons[1] = (Button)findViewById(R.id.show_club);
        showButtons[1].setOnClickListener(this);
        showButtons[2] = (Button)findViewById(R.id.show_diamond);
        showButtons[2].setOnClickListener(this);
        showButtons[3] = (Button)findViewById(R.id.show_spade);
        showButtons[3].setOnClickListener(this);
        showButtons[4] = (Button)findViewById(R.id.show_joker_black);
        showButtons[4].setOnClickListener(this);
        showButtons[5] = (Button)findViewById(R.id.show_joker_red);
        showButtons[5].setOnClickListener(this);
        showBuFan = (Button)findViewById(R.id.show_bufan);
        showBuFan.setOnClickListener(this);
        hasFanColor = new boolean[4];
        zhuImage = (ImageView)findViewById(R.id.image_zhu);
        noZhuFan = (Button)findViewById(R.id.no_fan);
        noZhuFan.setOnClickListener(this);
        maipaiButton = (Button)findViewById(R.id.button_maipai);
        maipaiButton.setOnClickListener(this);
        chupaiButton = (Button)findViewById(R.id.button_chupai);
        chupaiButton.setOnClickListener(this);
        handCardsLayout = (LinearLayout)findViewById(R.id.hand_cards_region);
        playCardsLayouts = new LinearLayout[4];
        playCardsLayouts[0] = (LinearLayout)findViewById(R.id.self_play_region);
        playCardsLayouts[1] = (LinearLayout)findViewById(R.id.right_play_region);
        playCardsLayouts[2] = (LinearLayout)findViewById(R.id.top_play_region);
        playCardsLayouts[3] = (LinearLayout)findViewById(R.id.left_play_region);
        playImages = new ArrayList<>();
        playerId = getIntent().getIntExtra("playerId", -1);
        Log.d("playerId", playerId + "");
        handCards = new ArrayList<>();
        duizi = new ArrayList<>();
        playCards = new ArrayList<>();
        firstCards = new ArrayList<>();
        dm = getResources().getDisplayMetrics();
        imageWidth = (int)(dm.heightPixels / 11.64);
        imageHeight = (int)(imageWidth / 2 * 3);
        leftMargin = imageWidth / 3 * 2 * -1;
        topMargin = imageHeight / 4;
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)handCardsLayout.getLayoutParams();
        p.height = imageHeight / 3 * 4;
        handCardsLayout.setLayoutParams(p);
        params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
        params.leftMargin = leftMargin;
        smallParams = new LinearLayout.LayoutParams(imageWidth / 5 * 4, imageHeight / 5 * 4);
        smallParams.leftMargin = smallParams.width / 3 * 2 * -1;

        com = new CardComparator();
        ruler = new PlayRuler();
        codeCom = new CodeComparator();

        showImages = new ImageView[8];
        showImages[0] = (ImageView)findViewById(R.id.image_show00);
        showImages[1] = (ImageView)findViewById(R.id.image_show01);
        showImages[2] = (ImageView)findViewById(R.id.image_show10);
        showImages[3] = (ImageView)findViewById(R.id.image_show11);
        showImages[4] = (ImageView)findViewById(R.id.image_show20);
        showImages[5] = (ImageView)findViewById(R.id.image_show21);
        showImages[6] = (ImageView)findViewById(R.id.image_show30);
        showImages[7] = (ImageView)findViewById(R.id.image_show31);
        turnImage = new ImageView[3];
        turnImage[0] = (ImageView)findViewById(R.id.turn_right);
        turnImage[1] = (ImageView)findViewById(R.id.turn_top);
        turnImage[2] = (ImageView)findViewById(R.id.turn_left);

        setUI();
        initSocket();
    }

    protected void clearHandCardsRegion(){
        for (int i = handCardsLayout.getChildCount() - 1; i > 0; --i){
            handCardsLayout.removeViewAt(i);
        }
    }

    protected void initGame(){
        stat = GAMESTATE.INIT;
        Log.d("init game", "init");
        scoreText.setText("分数: 0");
        clearHandCardsRegion();
        clearPlayRegion();
        whoPlay = -1;
        firstPlayerId = -1;
        handCards.clear();
        duizi.clear();      // 对子
        firstCards.clear();
        firstCardsCount = 0;
        acceptCardsCount = 0;
        playCards.clear();
        suit = 0x10;
        selfFan = false;
        selfTurn = false;
        zhuangJia = -1;
        isZhuangJia = false;
        hasSetZhu = false;
        hasDing = false;
        hasFanWang = false;
        for (int i = 0; i < hasFanColor.length; ++i){
            hasFanColor[i] = false;
        }
        bizhuang = false;
        selfMaipai = false;
    }

    protected void gameOver(){
        isFirstGame = false;
        clearPlayRegion();
        setAllTurnImageInvisible();
    }

    protected void initSocket() {
        try {
            sThread = new SocketThread();
        } catch (Exception e) {
            Log.d("GameActivity", "socket exception");
            e.printStackTrace();
            Toast.makeText(GameActivity.this, "disconnected", Toast.LENGTH_LONG).show();
            GameActivity.this.finish();
        }

        sThread.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte code = (byte) msg.obj;
                if (code == CodeUtil.HEARTBEAT){
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.ROOMID){
                    zhuangJia = CodeUtil.getTail(code);
                    Log.d("zhuang", zhuangJia + "" );
                    if (zhuangJia == playerId){
                        isZhuangJia = true;
                    }else{
                        isZhuangJia = false;
                    }
                    return;
                }
                if (code == CodeUtil.GAMESTART){
                    initGame();
                    return;
                }
                if (code == CodeUtil.GAMEOVER){
                    stat = GAMESTATE.OVER;
                    gameOver();
                    return;
                }
                if (stat == GAMESTATE.OVER){
                    addCardToPlayRegion(code, playerId);
                    return;
                }
                if (code == CodeUtil.EXIT){
                    Log.d("exit", "exit");
                    Toast.makeText(GameActivity.this, "某玩家已退出", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.ZHUSUIT){
                    hasSetZhu = true;
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)((suit << 4) | (zhu & 0x0f));
                    Log.d("liang", "" + suit);
                    // 亮牌者id
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    setZhuImage(zhu, showSuitId, false);
                    if (showSuitId == playerId) {
                        selfFan = true;
                    }else {
                        selfFan = false;
                    }
                    updateShowButtons();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.FANSUIT){
                    if (stat == GAMESTATE.INIT){
                        zhu = CodeUtil.getTail(code);
                        Log.d("zhu", "" + zhu);
                        com.setZhu(CodeUtil.getCardFromCode(zhu));
                        stat = GAMESTATE.DEAL;
                        if (!isFirstGame){
                            setLevelText();
                        }
                        return;
                    }
                    if (stat == GAMESTATE.PLAY){
                        int score = code & 0x3f;
                        scoreText.setText("分数: " + score * 5);
                        return;
                    }
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)((suit << 4) | (zhu & 0x0f));
                    Log.d("fan", "" + zhu);
                    Log.d("suit", "" + suit);
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    setZhuImage(zhu, showSuitId, true);
                    if (showSuitId == playerId){
                        selfFan = true;
                    }else {
                        selfFan = false;
                    }
                    updateShowButtons();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.DINGSUIT){
                    Log.d("ding", "has ding");
                    hasDing = true;
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)((suit << 4) | (zhu & 0x0f));
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    setZhuImage(zhu, showSuitId, true);
                    if (showSuitId == playerId){
                        selfFan = true;
                    }else {
                        selfFan = false;
                    }
                    updateShowButtons();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.FANWANG){
                    Log.d("fan", "has fan wang");
                    hasFanWang = true;
                    int wang = (CodeUtil.getTail(code) >> 2);
                    suit = (byte)(wang == 0 ? 0x04 : 0x05);
                    zhu = (byte)(zhu & 0x0f);
                    int showSuitId = CodeUtil.getTail(code) & 0x03;
                    if (wang == 0) {
                        setZhuImage((byte)0x4d, showSuitId, true);
                    }else {
                        setZhuImage((byte)0x4e, showSuitId, true);
                    }
                    if (showSuitId == playerId) {
                        selfFan = true;
                    }else {
                        selfFan = false;
                    }
                    updateShowButtons();
                    return;
                }
                if (code == CodeUtil.SUCCESS){
                    stat = GAMESTATE.MAIPAI;
                    Log.d("stat", stat.toString());
                    updateShowButtons();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.BIZHUANG){
                    bizhuang = true;
                    updateShowButtons();
                    updateTurnImage(CodeUtil.getTail(code));
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.MAIPAITURN){
                    int maipairen = CodeUtil.getTail(code) >> 2;
                    if (maipairen == playerId){
                        selfMaipai = true;
                    }else {
                        selfMaipai = false;
                    }
                    int turnID = CodeUtil.getTail(code) & 0x03;
                    if (turnID == playerId){
                        selfTurn = true;
                    }else{
                        selfTurn = false;
                    }
                    updateShowButtons();
                    updateTurnImage(turnID);
                    updateMaipaiButton();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.FIRSTPLAY){
                    Log.d("firstplay", "");
                    clearPlayRegion();
                    firstPlayerId = CodeUtil.getTail(code);
                    return;
                }
                if (code == CodeUtil.SHUAIFAIL){
                    clearPlayRegion();
                    Toast.makeText(GameActivity.this, "甩牌犯规", Toast.LENGTH_SHORT).show();
                }
                if (CodeUtil.getHeader(code) == CodeUtil.PLAYTURN){
                    if (stat != GAMESTATE.PLAY) {
                        stat = GAMESTATE.PLAY;
                        setRuler();
                        setShowButtonsGone();
                        setShowImages(View.GONE);
                        Log.d("stat", stat.toString());
                    }
                    int playId = CodeUtil.getTail(code);
                    if (playId == playerId){
                        selfTurn = true;
                    }else{
                        selfTurn = false;
                    }
                    whoPlay = playId;
                    updateChupaiButton();
                    updateTurnImage(playId);
                    return;
                }
                if (stat != GAMESTATE.PLAY) {
                    addCard(code, false);
                }
                if (stat == GAMESTATE.PLAY && (code & 0xe0) == 0xe0){
                    acceptCardsCount = 0;
                    firstCardsCount = (code & 0x1f);
                    Log.d("firstCardsCount", firstCardsCount + "");
                    return;
                }
                if (stat == GAMESTATE.PLAY){
                    ++acceptCardsCount;
                    if (whoPlay == firstPlayerId){
                        if (firstCards.size() < firstCardsCount)
                            firstCards.add(code);
                        if (firstCards.size() == firstCardsCount){
                            Log.d("first cards", firstCards.toString());
                        }
                    }
                    if (acceptCardsCount <= firstCardsCount) {
                        addCardToPlayRegion(code, whoPlay);
                    }else {
                        addCard(code, true);
                        Log.d("shuaipai failed", code + "");
                    }
                }
            }
        };
        sThread.start();
        Log.d("GameActivity", "socket thread start");
    }

    protected void setLevelText(){
        String level = "";
        switch (zhu & 0x0f){
            case 0: level = "3"; break;
            case 1: level = "4"; break;
            case 2: level = "5"; break;
            case 3: level = "6"; break;
            case 4: level = "7"; break;
            case 5: level = "8"; break;
            case 6: level = "9"; break;
            case 7: level = "10"; break;
            case 8: level = "J"; break;
            case 9: level = "Q"; break;
            case 10: level = "K"; break;
            case 11: level = "A"; break;
        }
        if ((zhuangJia + playerId) % 2 == 0){
            ourLevelText.setText("我方: " + level);
        }else{
            othersLevelText.setText("对方: " + level);
        }
    }

    protected void setRuler(){
        if (suit < 4){
            ruler.setZhu(CodeUtil.getCardFromCode(zhu));
            com.setZhu(CodeUtil.getCardFromCode(zhu));
        }else{
            ruler.setZhu(new Card(null, Rank.values()[zhu]));
            com.setZhu(new Card(Suit.Heart, Rank.values()[zhu]));
        }
        codeCom.setCardComparator(com);
        ruler.setCom(codeCom);
    }

    protected void clearPlayRegion(){
        firstCards.clear();
        firstCardsCount = 0;
        acceptCardsCount = 0;
        for (int i = 0; i < playCardsLayouts.length; ++i){
            for (int j = playCardsLayouts[i].getChildCount() - 1; j >= 2; --j) {
                playCardsLayouts[i].removeViewAt(j);
            }
        }
    }

    protected void updateChupaiButton(){
        if (selfTurn){
            // 自己出牌
            if (chupaiButton.getVisibility() != View.VISIBLE) {
                chupaiButton.setVisibility(View.VISIBLE);
            }
            if (firstPlayerId == playerId){     // 是本轮第一个出牌
                if (!playCards.isEmpty()) {     // 有牌出列
                    if (!chupaiButton.isEnabled()){
                        chupaiButton.setEnabled(true);
                    }
                }else{
                    if (chupaiButton.isEnabled()){
                        chupaiButton.setEnabled(false);
                    }
                }
            }else{
                // 跟牌，数量必须与第一个一致
                if (playCards.size() == firstCardsCount){
                    if (!chupaiButton.isEnabled()){
                        chupaiButton.setEnabled(true);
                    }
                }else{
                    if (chupaiButton.isEnabled()){
                        chupaiButton.setEnabled(false);
                    }
                }
            }
        }else{
            if (chupaiButton.getVisibility() != View.INVISIBLE){
                chupaiButton.setVisibility(View.INVISIBLE);
            }
            if (chupaiButton.isEnabled()){
                chupaiButton.setEnabled(false);
            }
        }
    }

    protected void updateMaipaiButton(){
        if (selfTurn && selfMaipai){
            if (maipaiButton.getVisibility() != View.VISIBLE) {
                maipaiButton.setVisibility(View.VISIBLE);
            }
            if (playCards.size() == 8){
                if (!maipaiButton.isEnabled()) {
                    maipaiButton.setEnabled(true);
                }
            }else{
                if (maipaiButton.isEnabled()){
                    maipaiButton.setEnabled(false);
                }
            }
        }else{
            if (maipaiButton.getVisibility() != View.INVISIBLE) {
                maipaiButton.setVisibility(View.INVISIBLE);
            }
            if (maipaiButton.isEnabled()){
                maipaiButton.setEnabled(false);
            }
        }
    }

    protected boolean checkShowButtonsVisible(){
        boolean ret = false;
        for (int i = 0; i < 6; ++i){
            if (showButtons[i].getVisibility() == View.VISIBLE) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    protected void setShowButtonsInvisible(int i){
        if (showButtons[i].getVisibility() != View.INVISIBLE) {
            showButtons[i].setEnabled(false);
            showButtons[i].setVisibility(View.INVISIBLE);
        }
    }

    protected void setAllShowButtonsInvisible() {
        for (int i = 0; i < 6; ++i) {
            setShowButtonsInvisible(i);
        }
        if (showBuFan.getVisibility() != View.INVISIBLE) {
            showBuFan.setEnabled(false);
            showBuFan.setVisibility(View.INVISIBLE);
        }
    }

    protected void setShowButtonsVisible(int i){
        if (showButtons[i].getVisibility() != View.VISIBLE) {
            showButtons[i].setEnabled(true);
            showButtons[i].setVisibility(View.VISIBLE);
        }
    }

    protected void setShowButtonsGone() {
        for (int i = 0; i < showButtons.length; ++i){
            if (showButtons[i].getVisibility() != View.GONE)
                showButtons[i].setVisibility(View.GONE);
        }
    }

    protected void updateShowButtons() {
        if (stat == GAMESTATE.PLAY){
            return;
        }
        if (bizhuang){
            if (isZhuangJia) {
                for (int i = 0; i < 4; ++i) {
                    if (hasCard((byte) ((i << 4) | (byte) zhu))) {
                        setShowButtonsVisible(i);
                    }
                }
                if (!checkShowButtonsVisible()){
                    // 无牌可翻
                    noZhuFan.setVisibility(View.VISIBLE);
                }
            }else{
                setAllShowButtonsInvisible();
            }
            return;
        }
        if (stat == GAMESTATE.MAIPAI && selfMaipai){
            setAllShowButtonsInvisible();
            return;
        }
        if (stat == GAMESTATE.MAIPAI && !selfTurn){
            setAllShowButtonsInvisible();
            return;
        }
        if (stat == GAMESTATE.MAIPAI && !selfMaipai && selfTurn){
            setAllShowButtonsInvisible();
            showBuFan.setEnabled(true);
            showBuFan.setVisibility(View.VISIBLE);
            if (hasDing){
                if (hasDuizi((byte) 0x4d)) {
                    setShowButtonsVisible(4);
                }
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
            }
            if (!hasFanWang && !hasDing){
                if (hasDuizi((byte) 0x4d)) {
                    setShowButtonsVisible(4);
                }
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
                for (int i = 0; i < 4; ++i){
                    if (hasDuizi((byte)((i << 4) | (zhu & 0x0f))) && !hasFanColor[i]){
                        setShowButtonsVisible(i);
                    }
                }
            }
            if (hasFanWang){
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
            }
            return;
        }
        if (!hasSetZhu) {
            for (int i = 0; i < 4; ++i) {
                if (hasCard((byte)((i << 4) | (zhu & 0x0f)))) {
                    setShowButtonsVisible(i);
                }
            }
        }
        if (hasSetZhu && !hasFanWang && selfFan && !hasDing) {
            setAllShowButtonsInvisible();
            if (hasDuizi((byte) zhu)) {
                setShowButtonsVisible(zhu >> 4);
            }
        }
        if (hasSetZhu && !hasFanWang && selfFan && hasDing) {
            setAllShowButtonsInvisible();
        }
        if (hasSetZhu && !hasFanWang && !selfFan && !hasDing) {
            setAllShowButtonsInvisible();
            if (!isFirstGame) {
                if (hasDuizi((byte) 0x4d)) {
                    setShowButtonsVisible(4);
                }
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
            }
            for (int i = 0; i < 4; ++i){
                if (hasDuizi((byte)((i << 4) | (zhu & 0x0f))) && !hasFanColor[i]){
                    setShowButtonsVisible(i);
                }
            }
        }
        if (hasSetZhu && !hasFanWang && !selfFan && hasDing){
            setAllShowButtonsInvisible();
            if (!isFirstGame){
                if (hasDuizi((byte) 0x4d)) {
                    setShowButtonsVisible(4);
                }
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
            }
        }
        if (hasSetZhu && selfFan && hasFanWang){
            setAllShowButtonsInvisible();
        }
        if (hasSetZhu && !selfFan && hasFanWang){
            setAllShowButtonsInvisible();
            if (hasDuizi((byte) 0x4e)) {
                setShowButtonsVisible(5);
            }
        }
    }

    protected void addCardToPlayRegion(byte card, int id){
        int loc = id - playerId;
        ImageView view = new ImageView(GameActivity.this);
        view.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(CodeUtil.getCardFromCode(card).toString().toLowerCase())));
        view.setLayoutParams(smallParams);
        loc = loc < 0 ? loc + 4 : loc;
        playCardsLayouts[loc].addView(view);
    }

    protected void addCard(byte code, boolean callOnClick){
        Card card = CodeUtil.getCardFromCode(code);
        MyImageView imageView = new MyImageView(GameActivity.this, topMargin);
        imageView.setImg(code);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stat == GAMESTATE.DEAL) return;
                if (imageView.play == false){
                    playCards.add(imageView.code);
                    playImages.add(imageView);
                    imageView.play = true;
                    imageView.offsetTopAndBottom(-topMargin);
                }else{
                    playCards.remove((Object)imageView.code);
                    playImages.remove(imageView);
                    imageView.play = false;
                    imageView.offsetTopAndBottom(topMargin);
                }
                if (stat == GAMESTATE.MAIPAI)
                    updateMaipaiButton();
                if (stat == GAMESTATE.PLAY)
                    updateChupaiButton();
            }
        });

        int index = getIndex(code);
        if (index > 0) {
            imageView.setLayoutParams(params);
            handCardsLayout.addView(imageView, index);
            if (stat == GAMESTATE.MAIPAI || callOnClick){
                imageView.callOnClick();
            }
        }
        if (handCards.contains(code)) {
            duizi.add(code);
        }
        handCards.add(index - 1, code);
        updateShowButtons();
    }

    protected boolean hasCard(byte code){
        return handCards.contains(code);
    }

    protected boolean hasDuizi(byte code){
        return duizi.contains(code);
    }

    protected int getIndex(byte code){
        int index = 1;
        if (handCards.isEmpty()){
            return 1;
        }
        Card c1 = CodeUtil.getCardFromCode(code);
        for (byte b : handCards){
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
                    hasFanColor[0] = true;
                    findViewById(R.id.show_heart).setEnabled(false);
                }
                break;
            case R.id.show_club:
                sThread.send((byte)(header | (byte)0x01));
                if (header == CodeUtil.FANSUIT) {
                    hasFanColor[1] = true;
                    findViewById(R.id.show_club).setEnabled(false);
                }
                break;
            case R.id.show_diamond:
                sThread.send((byte)(header | (byte)0x02));
                if (header == CodeUtil.FANSUIT) {
                    hasFanColor[2] = true;
                    findViewById(R.id.show_diamond).setEnabled(false);
                }
                break;
            case R.id.show_spade:
                sThread.send((byte)(header | (byte)0x03));
                if (header == CodeUtil.FANSUIT) {
                    hasFanColor[3] = true;
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
                noZhuFan.setVisibility(View.GONE);
                break;
            case R.id.button_maipai:
                sThread.sendCards(new ArrayList<Byte>(playCards), false);
                Log.d("maipai array", playCards.toString());
                for (byte b : playCards)
                    handCards.remove((Object)b);
                playCards.clear();
                for (int i = 0; i < playImages.size(); ++i) {
                    handCardsLayout.removeView(playImages.get(i));
                }
                setSelfTurnFalse();
                break;
            case R.id.show_bufan:
                setAllShowButtonsInvisible();
                sThread.send(CodeUtil.BUFAN);
                break;
            case R.id.button_chupai:
                if (!checkPlay()){
                    chupaiButton.setEnabled(false);
                    Toast.makeText(GameActivity.this, strBreakRules, Toast.LENGTH_SHORT).show();
                    return;
                }
                sThread.sendCards(new ArrayList<Byte>(playCards), true);
                Log.d("chupai array", playCards.toString());
                for (byte b : playCards)
                    handCards.remove((Object)b);
                for (int i = 0; i < playImages.size(); ++i) {
                    handCardsLayout.removeView(playImages.get(i));
                }
                setSelfTurnFalse();
                playCards.clear();
                break;
        }
    }

    protected boolean checkPlay() {
        // 如果自己先出，只检查花色是否统一
        if (firstPlayerId == playerId) {
            return ruler.checkSuit(playCards);
        } else {
            // 别人先出牌，检查规则
            return ruler.checkRules(firstCards, handCards, playCards);
        }
    }


     protected void setSelfTurnFalse() {
         selfTurn = false;
         maipaiButton.setVisibility(View.GONE);
         chupaiButton.setVisibility(View.GONE);
     }

    protected void setShowImages(int visibility){
        for (int i = 0; i < showImages.length; ++i){
            if (showImages[i].getVisibility() != visibility){
                showImages[i].setVisibility(visibility);
            }
        }
    }

    protected void setZhuImage(byte code, int id, boolean dui){
        String zhuName = CodeUtil.getCardFromCode(code).toString().toLowerCase();
        Log.d("setZhuImage", zhuName);
        setShowImages(View.INVISIBLE);
        int loc = id - playerId;
        loc = loc < 0 ? loc + 4 : loc;
        Log.d("show loc", "" + loc);
        showImages[loc * 2].setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(zhuName)));
        showImages[loc * 2].setVisibility(View.VISIBLE);
        if (dui){
            showImages[loc * 2 + 1].setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    ResourceUtil.getIDByName(zhuName)));
            showImages[loc * 2 + 1].setVisibility(View.VISIBLE);
        }
        zhuImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(zhuName)));
    }

    protected void updateTurnImage(int turn){
        setAllTurnImageInvisible();
        int loc = turn- playerId;
        loc = loc < 0 ? loc + 4 : loc;
        if (loc != 0){
            turnImage[loc - 1].setVisibility(View.VISIBLE);
        }
    }

    protected void setAllTurnImageInvisible(){
        for (int i = 0; i < 3; ++i){
            if (turnImage[i].getVisibility() != View.INVISIBLE){
                turnImage[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("GameActivity", "destroy");
        sThread.stop = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 闅愯棌鐘舵?佹爮鍜岃櫄鎷熸寜閿?
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
    }

    private enum  GAMESTATE{
        INIT,
        DEAL,
        MAIPAI,
        PLAY,
        OVER
    }
}
