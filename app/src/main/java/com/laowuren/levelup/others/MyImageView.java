package com.laowuren.levelup.others;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.laowuren.levelup.utils.CodeUtil;
import com.laowuren.levelup.utils.ResourceUtil;

/**
 * Created by Administrator on 2020/1/30/030.
 */

public class MyImageView extends ImageView {

    public byte code;
    public boolean play = false;

    public MyImageView(Context context){
        super(context);
    }

    public void setImg(byte code){
        this.code = code;
        setImageBitmap(BitmapFactory.decodeResource(getResources(),
                ResourceUtil.getIDByName(CodeUtil.getCardFromCode(code).toString().toLowerCase())));
    }
}
