package com.laowuren.levelup.others;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.laowuren.levelup.utils.BitmapManager;
import com.laowuren.levelup.utils.CodeUtil;

/**
 * Created by Administrator on 2020/1/30/030.
 */

public class MyImageView extends ImageView {

    public byte code;
    public boolean play = false;
    public int topMargin;

    public MyImageView(Context context, int topMargin){
        super(context);
        this.topMargin = topMargin;
    }

    public void setImg(byte code, boolean isZhu){
        this.code = code;
        if (isZhu && CodeUtil.getCardFromCode(code).getSuit() != null) {
            setImageBitmap(BitmapManager.bitmapHashMap.get((byte)(code | 0x40)));
        } else {
            setImageBitmap(BitmapManager.bitmapHashMap.get(code));
        }
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
