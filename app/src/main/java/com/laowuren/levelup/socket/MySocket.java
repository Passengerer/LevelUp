package com.laowuren.levelup.socket;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2020/1/27/027.
 * 参考文章: https://blog.csdn.net/lhp15575865420/article/details/75136649
 */

public class MySocket extends Socket {

    private static final String host = "10.0.2.2";
    private static final int port = 9990;
    private static ObjectInputStream ois = null;
    private static ObjectOutputStream oos = null;
    /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
    private static MySocket socket = null;

    private MySocket(String host, int port) throws UnknownHostException, IOException {
        super(host, port);
        Log.d("MySocket", "init");
    }

    public static MySocket getSocket() throws IOException {
        if (socket == null) {
            socket = new MySocket(host, port);
        }
        return socket;
    }

    public static ObjectInputStream getOIS() throws IOException {
        if (ois == null) {
            ois = new ObjectInputStream(socket.getInputStream());
        }
        return ois;
    }

    public static ObjectOutputStream getOOS() throws IOException {
        if (oos == null) {
            oos = new ObjectOutputStream(socket.getOutputStream());
        }
        return oos;
    }
}
