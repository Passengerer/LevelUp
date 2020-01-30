package com.laowuren.levelup.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Administrator on 2020/1/27/027.
 */

public class ResourceUtil {

    public static int getIDByName(String resourceName) {
        Context context = MyApplication.getContext();
        Resources res = context.getResources();
        int picid = res.getIdentifier(resourceName, "drawable", context.getPackageName());
        return picid;
    }

}
