package io.github.junheah.jsp;


import java.util.Map;
import java.util.ResourceBundle;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpClient {
    int count = 0;
    OkHttpClient client;
    public HttpClient(){
        client = new OkHttpClient();
        count = -1;
        System.out.println("created!" + count);
    }

    public io.github.junheah.jsp.model.source.Response get(Request r){
        try (okhttp3.Response res = client.newCall(r).execute()) {
            return new io.github.junheah.jsp.model.source.Response(res);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
