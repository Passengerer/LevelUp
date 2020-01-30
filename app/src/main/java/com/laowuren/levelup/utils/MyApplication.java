package com.laowuren.levelup.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2020/1/27/027.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
