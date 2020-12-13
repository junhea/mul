package io.github.junheah.jsp;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;

import java.util.Map;

import okhttp3.OkHttpClient;
import io.github.junheah.jsp.model.source.Response;

public class HttpClient {
    OkHttpClient client;
    public HttpClient(){
        client = new OkHttpClient();
    }

    public Response httpget(V8 runtime, V8Object r){
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder().url(r.getString("url"));
        V8Object headers = r.getObject("headers");
        if(headers != null){
            for(String k : headers.getKeys()){
                requestBuilder.addHeader(k, headers.getString(k));
            }
        }

        try (okhttp3.Response res = client.newCall(requestBuilder.build()).execute()) {
            return new Response(runtime, res);
        }catch(Exception e){
            e.printStackTrace();
        }
        return new Response();
    }

}
