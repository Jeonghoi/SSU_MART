package com.jeonghoi.ssumart;

import android.content.Context;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

import static com.jeonghoi.ssumart.MainActivity.comm;

class SimpleServer extends WebSocketServer {

    public Context context;
    public static MyDataBase db;
    //WebSocket conn;

    public SimpleServer(InetSocketAddress address) {
        super(address);
        this.context=context;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        comm = conn;
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        comm=conn;
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        //conn.send(message);
        comm = conn;
        switch (message){
            case "LED_ON":
                if(MyDataBase.led==0){
                    db.setLed(1);
                    //conn.send("LED_ON_DONE");
                    //Db.setledoon();
                }break;
            case "LED_OFF":
                if(MyDataBase.led==1){
                    //conn.send("LED_OFF_DONE");
                    db.setLed(0);
                    //Db.setledoon();
                }break;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        comm = conn;
        System.err.println("an error occured on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
    }

    @Override
    public void onStart() {

    }

    /*public void sendMessage(String msg){
        conn.send(msg);
    }*/


}