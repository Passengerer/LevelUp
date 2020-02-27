package com.laowuren.levelup;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.laowuren.levelup.others.Card;
import com.laowuren.levelup.others.CardComparator;
import com.laowuren.levelup.others.CodeComparator;
import com.laowuren.levelup.others.ImageViewsComparator;
import com.laowuren.levelup.others.MyImageView;
import com.laowuren.levelup.others.Rank;
import com.laowuren.levelup.others.Suit;
import com.laowuren.levelup.thread.SocketThread;
import com.laowuren.levelup.utils.BitmapManager;
import com.laowuren.levelup.utils.CardsParser;
import com.laowuren.levelup.utils.CodeUtil;
import com.laowuren.levelup.utils.PlayRuler;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private SocketThread sThread;
    private final String strBreakRules = "���Ʒ���";

    private int playerId;     // ���id
    private int whoPlay;      // ��ǰ�������id
    private int firstPlayerId;  // ÿ�ֵ�һ�����Ƶ����id
    private ArrayList<Byte> handCards;
    private ArrayList<Byte> firstCards;
    private int firstCardsCount;
    private int acceptCardsCount;
    private ArrayList<Byte> playCards;
    private byte zhu = -1;        // ��С��ʱ����(��ɫ�͵ȼ�)����С��ʱ��ȼ�
    private byte suit = 0x10;       // �滨ɫ��4����С����5�������
    private PlayRuler ruler;
    private boolean selfFan = false;
    private boolean selfTurn = false;
    private GAMESTATE stat = GAMESTATE.INIT;
    private int zhuangJia = -1;
    private int score = 0;
    private boolean isZhuangJia = false;
    private boolean isFirstGame = true;

    private boolean hasSetZhu = false;
    private boolean hasDing = false;
    private boolean hasFanWang = false;
    private boolean[] hasFanColor;
    private boolean bizhuang = false;
    private boolean selfMaipai = false;
    private int showSuitId = -1;

    private CardComparator com;
    private CodeComparator codeCom;
    private ImageViewsComparator imgCom;

    private TextView scoreText;
    private TextView ourLevelText;
    private TextView othersLevelText;
    private LinearLayout handCardsLayout;
    private LinearLayout[] playCardsLayouts;
    private LinearLayout[] playedLayouts;
    private ArrayList<ImageView> playImages;
    private LinearLayout.LayoutParams params;
    private LinearLayout.LayoutParams smallParams;
    private LinearLayout.LayoutParams spaceParams;
    private Button[] showButtons;
    private Button showBuFan;
    private ImageView zhuImage;
    private Button noZhuFan;
    private Button maipaiButton;
    private Button chupaiButton;
    private Button[] chapaiButtons;

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
        chapaiButtons = new Button[4];
        chapaiButtons[0] = (Button)findViewById(R.id.button_chapai_self);
        chapaiButtons[0].setOnTouchListener(this);
        chapaiButtons[1] = (Button)findViewById(R.id.button_chapai_right);
        chapaiButtons[1].setOnTouchListener(this);
        chapaiButtons[2] = (Button)findViewById(R.id.button_chapai_top);
        chapaiButtons[2].setOnTouchListener(this);
        chapaiButtons[3] = (Button)findViewById(R.id.button_chapai_left);
        chapaiButtons[3].setOnTouchListener(this);
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
        playedLayouts = new LinearLayout[4];
        playedLayouts[0] = (LinearLayout)findViewById(R.id.self_played);
        playedLayouts[1] = (LinearLayout)findViewById(R.id.right_played);
        playedLayouts[2] = (LinearLayout)findViewById(R.id.top_played);
        playedLayouts[3] = (LinearLayout)findViewById(R.id.left_played);
        playImages = new ArrayList<>();
        playerId = getIntent().getIntExtra("playerId", -1);
        Log.d("playerId", playerId + "");
        handCards = new ArrayList<>();
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
        spaceParams = new LinearLayout.LayoutParams(imageWidth / 5 * 4, imageHeight / 5 * 4);
        spaceParams.leftMargin = smallParams.width / 6 * 5 * -1;

        com = new CardComparator();
        ruler = new PlayRuler();
        codeCom = new CodeComparator();
        imgCom = new ImageViewsComparator();

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

        TextView indexSelf = (TextView)findViewById(R.id.index_self);
        indexSelf.setText(playerId % 4 + 1 + "");
        TextView indexRight = (TextView)findViewById(R.id.index_right);
        indexRight.setText((playerId + 1) % 4 + 1 + "");
        TextView indexTop = (TextView)findViewById(R.id.index_top);
        indexTop.setText((playerId + 2) % 4 + 1 + "");
        TextView indexLeft = (TextView)findViewById(R.id.index_left);
        indexLeft.setText((playerId + 3) % 4 + 1 + "");

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
        clearHandCardsRegion();
        clearPlayRegion(false);
        clearPlayedRegion();
        zhuImage.setImageBitmap(BitmapManager.bitmapHashMap.get((byte)0xff));
        scoreText.setText("����: 0");
        score = 0;
        showSuitId = -1;
        whoPlay = -1;
        firstPlayerId = -1;
        handCards.clear();
        firstCards.clear();
        firstCardsCount = 0;
        acceptCardsCount = 0;
        playCards.clear();
        suit = 0x10;
        selfFan = false;
        selfTurn = false;
        isZhuangJia = false;
        hasSetZhu = false;
        hasDing = false;
        hasFanWang = false;
        for (int i = 0; i < hasFanColor.length; ++i){
            hasFanColor[i] = false;
        }
        bizhuang = false;
        selfMaipai = false;
        setAllShowButtonsInvisible();
    }

    protected void gameOver(){
        isFirstGame = false;
        clearPlayRegion(false);
        setAllTurnImageInvisible();
        setChapaiButtonsVisibility(View.INVISIBLE);
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
                    Toast.makeText(GameActivity.this, "ĳ������˳�", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (CodeUtil.getHeader(code) == CodeUtil.ZHUSUIT){
                    hasSetZhu = true;
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)((suit << 4) | (zhu & 0x0f));
                    Log.d("liang", "" + suit);
                    // ������id
                    showSuitId = CodeUtil.getTail(code) & 0x03;
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
                        int turnScoreCode = code & 0x0f;
                        score = score + turnScoreCode * 5;
                        scoreText.setText("����: " + score);
                        return;
                    }
                    suit = (byte)(CodeUtil.getTail(code) >> 2);
                    zhu = (byte)((suit << 4) | (zhu & 0x0f));
                    Log.d("fan", "" + zhu);
                    Log.d("suit", "" + suit);
                    showSuitId = CodeUtil.getTail(code) & 0x03;
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
                    showSuitId = CodeUtil.getTail(code) & 0x03;
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
                    showSuitId = CodeUtil.getTail(code) & 0x03;
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
                    clearPlayRegion(true);
                    firstPlayerId = CodeUtil.getTail(code);
                    return;
                }
                if (code == CodeUtil.SHUAIFAIL){
                    clearPlayRegion(false);
                    Toast.makeText(GameActivity.this, "˦�Ʒ���", Toast.LENGTH_SHORT).show();
                }
                if (CodeUtil.getHeader(code) == CodeUtil.PLAYTURN){
                    if (stat != GAMESTATE.PLAY) {
                        stat = GAMESTATE.PLAY;
                        setRuler();
                        setShowButtonsGone();
                        setShowImages(View.GONE);
                        setChapaiButtonsVisibility(View.VISIBLE);
                        sortHandCardsLayout();
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
                    if (whoPlay == firstPlayerId)
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

    protected void sortHandCardsLayout(){
        if (Build.VERSION.SDK_INT >= 24)
            handCards.sort(codeCom);
        ArrayList<MyImageView> imageViews = new ArrayList<>();
        for (int i = handCardsLayout.getChildCount() - 1; i > 0; --i){
            imageViews.add((MyImageView)handCardsLayout.getChildAt(i));
            handCardsLayout.removeViewAt(i);
        }
        if (Build.VERSION.SDK_INT >= 24)
            imageViews.sort(imgCom);
        for (int i = 0; i < imageViews.size(); ++i){
            MyImageView imageView = imageViews.get(i);
            boolean isZhu;
            if (ruler.getSuit(CodeUtil.getCardFromCode(imageView.code)) == null) {
                isZhu = true;
            } else {
                isZhu = false;
            }
            imageViews.get(i).setImg(imageView.code, isZhu);
            handCardsLayout.addView(imageViews.get(i));
        }
    }

    protected void setChapaiButtonsVisibility(int visibility){
        for (int i = 0; i < chapaiButtons.length; ++i){
            if (chapaiButtons[i].getVisibility() != visibility){
                chapaiButtons[i].setVisibility(visibility);
            }
        }
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
        Log.d("zhuangjia", "" + zhuangJia);
        if ((zhuangJia + playerId) % 2 == 0){
            ourLevelText.setText("�ҷ�: " + level);
            ourLevelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            othersLevelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }else{
            othersLevelText.setText("�Է�: " + level);
            othersLevelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            ourLevelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
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
        imgCom.setCodeCom(codeCom);
    }

    protected void clearPlayedRegion(){
        for (int i = 0; i < playedLayouts.length; ++i){
            for (int j = playedLayouts[i].getChildCount() - 1; j > 0; --j){
                playedLayouts[i].removeViewAt(j);
            }
        }
    }

    protected void clearPlayRegion(boolean addToPlayedRegion){
        firstCards.clear();
        firstCardsCount = 0;
        acceptCardsCount = 0;
        for (int i = 0; i < playCardsLayouts.length; ++i){
            for (int j = playCardsLayouts[i].getChildCount() - 1; j >= 2; --j) {
                View view = playCardsLayouts[i].getChildAt(j);
                playCardsLayouts[i].removeViewAt(j);
                if (addToPlayedRegion) {
                    playedLayouts[i].addView(view);
                }
            }
            // ���һ����ͼƬ
            ImageView marginView = new ImageView(GameActivity.this);
            marginView.setLayoutParams(spaceParams);
            playedLayouts[i].addView(marginView);
        }
    }

    protected void updateChupaiButton(){
        if (selfTurn){
            // �Լ�����
            if (chupaiButton.getVisibility() != View.VISIBLE) {
                chupaiButton.setVisibility(View.VISIBLE);
            }
            if (firstPlayerId == playerId){     // �Ǳ��ֵ�һ������
                if (!playCards.isEmpty()) {     // ���Ƴ���
                    if (!chupaiButton.isEnabled()){
                        chupaiButton.setEnabled(true);
                    }
                }else{
                    if (chupaiButton.isEnabled()){
                        chupaiButton.setEnabled(false);
                    }
                }
            }else{
                // ���ƣ������������һ��һ��
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
        Log.d("self fan", selfFan + "");
        if (stat == GAMESTATE.PLAY){
            return;
        }
        if (bizhuang){
            bizhuang = false;
            if (isZhuangJia) {
                for (int i = 0; i < 4; ++i) {
                    if (hasCard((byte) ((i << 4) | (byte) zhu))) {
                        setShowButtonsVisible(i);
                    }
                }
                if (!checkShowButtonsVisible()){
                    // ���ƿɷ�
                    noZhuFan.setVisibility(View.VISIBLE);
                }
            }else{
                setAllShowButtonsInvisible();
            }
            return;
        }
        if (stat == GAMESTATE.MAIPAI && selfMaipai){
            Log.d("maipai", "0");
            setAllShowButtonsInvisible();
            return;
        }
        if (stat == GAMESTATE.MAIPAI && !selfTurn){
            Log.d("maipai", "1");
            setAllShowButtonsInvisible();
            return;
        }
        if (stat == GAMESTATE.MAIPAI && !selfMaipai && selfTurn){
            Log.d("maipai", "2");
            setAllShowButtonsInvisible();
            showBuFan.setEnabled(true);
            showBuFan.setVisibility(View.VISIBLE);
            if (selfFan){
                return;
            }
            if (hasDing){
                Log.d("maipai", "3");
                if (hasDuizi((byte) 0x4d)) {
                    setShowButtonsVisible(4);
                }
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
            }
            if (!hasFanWang && !hasDing){
                Log.d("maipai", "4");
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
                Log.d("maipai", "5");
                if (hasDuizi((byte) 0x4e)) {
                    setShowButtonsVisible(5);
                }
            }
            return;
        }
        if (!hasSetZhu) {
            Log.d("maipai", "6");
            for (int i = 0; i < 4; ++i) {
                if (hasCard((byte)((i << 4) | (zhu & 0x0f)))) {
                    setShowButtonsVisible(i);
                }
            }
        }
        if (hasSetZhu && !hasFanWang && selfFan && !hasDing) {
            Log.d("maipai", "7");
            setAllShowButtonsInvisible();
            if (hasDuizi((byte) zhu)) {
                setShowButtonsVisible(zhu >> 4);
            }
        }
        if (hasSetZhu && !hasFanWang && selfFan && hasDing) {
            Log.d("maipai", "8");
            setAllShowButtonsInvisible();
        }
        if (hasSetZhu && !hasFanWang && !selfFan && !hasDing) {
            Log.d("maipai", "9");
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
            Log.d("maipai", "10");
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
            Log.d("maipai", "11");
            setAllShowButtonsInvisible();
        }
        if (hasSetZhu && !selfFan && hasFanWang){
            Log.d("maipai", "12");
            setAllShowButtonsInvisible();
            if (hasDuizi((byte) 0x4e)) {
                setShowButtonsVisible(5);
            }
        }
    }

    protected void addCardToPlayRegion(byte card, int id){
        int loc = id - playerId;
        ImageView view = new ImageView(GameActivity.this);
        if (ruler.getSuit(CodeUtil.getCardFromCode(card)) == null) {
            view.setImageBitmap(BitmapManager.bitmapHashMap.get((byte)(card | 0x40)));
        }else {
            view.setImageBitmap(BitmapManager.bitmapHashMap.get(card));
        }
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.setLayoutParams(smallParams);
        loc = loc < 0 ? loc + 4 : loc;
        playCardsLayouts[loc].addView(view);
    }

    protected void addCard(byte code, boolean callOnClick){
        //Card card = CodeUtil.getCardFromCode(code);
        MyImageView imageView = new MyImageView(GameActivity.this, topMargin);
        imageView.setImg(code, false);
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
        handCards.add(index - 1, code);
        updateShowButtons();
    }

    protected boolean hasCard(byte code){
        return handCards.contains(code);
    }

    protected boolean hasDuizi(byte code){
        ArrayList<Byte> dui = CardsParser.getDui(handCards);
        return dui.contains(code);
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
        // ���ر�����
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        // ����״̬�������ⰴ��
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
        // ���ú���
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

    protected void setPlayedLayoutInvisible(){
        for (int i = 0; i < playedLayouts.length; ++i){
            if (playedLayouts[i].getVisibility() != View.INVISIBLE){
                playedLayouts[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                setPlayedLayoutInvisible();
                switch (v.getId()){
                    case R.id.button_chapai_self:
                        playedLayouts[0].setVisibility(View.VISIBLE);
                        break;
                    case R.id.button_chapai_right:
                        playedLayouts[1].setVisibility(View.VISIBLE);
                        break;
                    case R.id.button_chapai_top:
                        playedLayouts[2].setVisibility(View.VISIBLE);
                        break;
                    case R.id.button_chapai_left:
                        playedLayouts[3].setVisibility(View.VISIBLE);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (v.getId()){
                    case R.id.button_chapai_self:
                        playedLayouts[0].setVisibility(View.INVISIBLE);
                        break;
                    case R.id.button_chapai_right:
                        playedLayouts[1].setVisibility(View.INVISIBLE);
                        break;
                    case R.id.button_chapai_top:
                        playedLayouts[2].setVisibility(View.INVISIBLE);
                        break;
                    case R.id.button_chapai_left:
                        playedLayouts[3].setVisibility(View.INVISIBLE);
                        break;
                }
                break;
        }
        return true;
    }

    protected boolean checkPlay() {
        // ����Լ��ȳ���ֻ��黨ɫ�Ƿ�ͳһ
        if (firstPlayerId == playerId) {
            return ruler.checkSuit(playCards);
        } else {
            // �����ȳ��ƣ�������
            return ruler.checkRules(firstCards, handCards, playCards);
        }
    }


     protected void setSelfTurnFalse() {
         selfTurn = false;
         maipaiButton.setEnabled(false);
         maipaiButton.setVisibility(View.INVISIBLE);
         chupaiButton.setEnabled(false);
         chupaiButton.setVisibility(View.INVISIBLE);
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
        showImages[loc * 2].setImageBitmap(BitmapManager.bitmapHashMap.get(code));
        showImages[loc * 2].setVisibility(View.VISIBLE);
        if (dui){
            showImages[loc * 2 + 1].setImageBitmap(BitmapManager.bitmapHashMap.get(code));
            showImages[loc * 2 + 1].setVisibility(View.VISIBLE);
        }
        zhuImage.setImageBitmap(BitmapManager.bitmapHashMap.get(code));
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
    protected void onPause() {
        super.onPause();
        Log.d("GameActivity", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("GameActivity", "onResume");
        // 隐藏状�?�栏和虚拟按�?
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
