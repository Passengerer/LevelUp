package com.laowuren.levelup.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2020/1/27/027.
 * 参考文章: https://blog.csdn.net/lhp15575865420/article/details/75136649
 */

public class MySocket extends Socket {

    private static final String host = "10.0.2.2";
    private static final int port = 9990;
    private static InputStream in = null;
    private static OutputStream out = null;
    /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
    private static MySocket socket = null;

    private MySocket(String host, int port) throws IOException {
        super(host, port);
    }

    public static MySocket getSocket() throws IOException {
        if (socket == null) {
            socket = new MySocket(host, port);
            Log.d("getSocket", "init");
        }
        return socket;
    }

    public static InputStream getIS() throws IOException {
        if (in == null) {
            in = socket.getInputStream();
            Log.d("getIS", "init");
        }
        return in;
    }

    public static OutputStream getOS() throws IOException {
        if (out == null) {
            out = socket.getOutputStream();
            Log.d("getOS", "init");
        }
        return out;
    }
}
