package com.laowuren.levelup.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;

/**
 * Created by Administrator on 2020/2/14/014.
 */

public class BitmapManager {

    public static HashMap<Byte, Bitmap> bitmapHashMap;

    static{
        bitmapHashMap = new HashMap<>();
        Context context = MyApplication.getContext();
        byte code = 0;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 13; ++j) {
                code = (byte) ((i << 4) | (byte) j);
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                        ResourceUtil.getIDByName(CodeUtil.getCardFromCode(code).toString().toLowerCase()));
                bitmapHashMap.put(code, bitmap);
            }
        }
        Bitmap jokerBlack = BitmapFactory.decodeResource(context.getResources(),
                ResourceUtil.getIDByName(CodeUtil.getCardFromCode((byte)0x4d).toString().toLowerCase()));
        bitmapHashMap.put((byte)0x4d, jokerBlack);
        Bitmap jokerRed = BitmapFactory.decodeResource(context.getResources(),
                ResourceUtil.getIDByName(CodeUtil.getCardFromCode((byte)0x4e).toString().toLowerCase()));
        bitmapHashMap.put((byte)0x4e, jokerRed);
    }

    public BitmapManager(){}

}
