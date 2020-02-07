package com.laowuren.levelup.others;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.laowuren.levelup.utils.CodeUtil;
import com.laowuren.levelup.utils.ResourceUtil;

/**
 * Created by Administrator on 2020/1/30/030.
 */

public class MyImageView extends ImageView {

    public byte code;
    public boolean play = false;
    public int topMargin;

    private static boolean touch = false;

    public MyImageView(Context context, int topMargin){
        super(context);
        this.topMargin = topMargin;
    }

    public void setImg(byte code){
        this.code = code;
        setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(CodeUtil.getCardFromCode(code).toString().toLowerCase())));
        setScaleType(ScaleType.FIT_XY);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (play == true){
            offsetTopAndBottom(-topMargin);
        }
    }
}
