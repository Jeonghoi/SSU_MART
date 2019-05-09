package com.jeonghoi.ssumart;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;


class NanoHttp {
    public Context context;
    public WebServer server;
    public static final String TAG = "MYSERVER";
    public static final int PORT = 8080;

    public static String ipAddress;


    public NanoHttp(Context context){
        this.context=context;
        //TextView text = (TextView) findViewById(R.id.ipaddr);
        ipAddress = getLocalIpAddress();

        if (ipAddress != null) {
          //  text.setText("Please Access:" + "http://" + ipAddress + ":" + PORT);
            File root = Environment.getExternalStorageDirectory();
        } else {
          //  text.setText("Wi-Fi Network Not Available");
        }

        server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
    }

    public void finalize()
    {
        //super.finalize();
    //    super.onDestroy();
        if (server != null)
            server.stop();
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddr = inetAddress.getHostAddress();
                        return ipAddr;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.d(TAG, ex.toString());
        }
        return null;
    }


    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(PORT);
        }

        @Override
        public Response serve(String uri, Method method,
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {
            String answer = "";
            try {
                // Open file from SD Card
                File root = Environment.getExternalStorageDirectory();
                FileReader index = new FileReader(root.getAbsolutePath() +
                        "/www/index.html");
                BufferedReader reader = new BufferedReader(index);
                String line = "";
                while ((line = reader.readLine()) != null) {
                    answer += line;
                }
                reader.close();
            } catch(IOException ioe) {
                Log.w("Httpd", ioe.toString());
            }
            return new NanoHTTPD.Response(answer);
        }
    }
}