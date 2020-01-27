package com.laowuren.levelup.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.laowuren.levelup.others.MyMessage;
import com.laowuren.levelup.socket.MySocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2020/1/26/026.
 */

public class SocketThread extends Thread {

    public final static int DISCONNECTED = 1;
    public final static int MYMESSAGE = 2;

    public Handler handler;
    public volatile boolean stop = false;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public void send(final MyMessage message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (oos == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Log.d("CreateActivity", "interrupted exception");
                        ie.printStackTrace();
                    }
                }
                try{
                    oos.writeObject(message);
                    oos.flush();
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
            oos = MySocket.getOOS();
            ois = MySocket.getOIS();
            Log.d("SocketThread", "init");
        } catch (IOException ioe) {
            Log.d("SocketThread", "io exception");
            ioe.printStackTrace();
        }
        while (!stop && socket != null && !socket.isClosed()) {
            try {
                MyMessage message = (MyMessage) ois.readObject();
                if (message != null) {
                    Message msg = new Message();
                    msg.what = MYMESSAGE;
                    msg.obj = message;
                    handler.sendMessage(msg);
                }
            } catch (IOException ioe) {
                Log.d("SocketThread", "read exception");
                ioe.printStackTrace();
                try {
                    Message msg = new Message();
                    msg.what = DISCONNECTED;
                    handler.sendMessage(msg);
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (Exception ex) {
                    Log.d("SocketThread", "close socket exception");
                }
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
}
