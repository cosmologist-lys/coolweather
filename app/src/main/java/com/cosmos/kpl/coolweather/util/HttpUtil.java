package com.cosmos.kpl.coolweather.util;


import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * http util : pack okhttp3 util.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        /*
            1.create okhttpclient
            2.create Request.Builder to input address in url to build a new request
            3.the created okhttpclient do call by inputting request,and enqueue callback
         */
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
