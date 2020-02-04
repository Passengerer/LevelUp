package com.laowuren.levelup.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.laowuren.levelup.socket.MySocket;
import com.laowuren.levelup.utils.CodeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2020/1/26/026.
 */

public class SocketThread extends Thread {

    public Handler handler;
    public volatile boolean stop = false;

    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public void send(final byte code){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (out == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Log.d("CreateActivity", "interrupted exception");
                        ie.printStackTrace();
                    }
                }
                try{
                    out.write(code);
                    out.flush();
                }catch (Exception e){
                    Log.d("SocketThread", "write exception");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void run() {
        try {
            socket = MySocket.getSocket();
            out = MySocket.getOS();
            in = MySocket.getIS();
            Log.d("SocketThread", "init");
        } catch (IOException ioe) {
            Log.d("SocketThread", "io exception");
            ioe.printStackTrace();
        }
        while (!stop && socket != null && !socket.isClosed()) {
            byte b;
            try {
                if ((b = (byte)in.read()) != -1) {
                    Log.d("accept", "code " + (int)b);
                    Message message = new Message();
                    message.obj = b;
                    handler.sendMessage(message);
                    if (b == CodeUtil.READY)
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (stop){
            try{
                Thread.sleep(1500);
                if (!socket.isClosed()){
                    socket.close();
                }
                if (in != null){
                    in.close();
                }
                if (out != null){
                    out.close();
                }
            }catch (Exception e){}
        }
        Log.d("SocketThread", "thread over");
    }
}
