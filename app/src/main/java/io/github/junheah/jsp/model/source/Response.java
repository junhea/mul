package io.github.junheah.jsp.model.source;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;

import java.io.IOException;

public class Response extends V8Object {
    int code;
    String body;

    public Response(V8 v8, okhttp3.Response r){
        super(v8);
        this.code = r.code();
        try {
            this.body = r.body().string();
        }catch (IOException e){
            e.printStackTrace();
            this.body = "";
        }
        this.add("code", this.code);
        this.add("body", this.body);
    }
    public Response(){
        this.code = 0;
        this.body = "";
        this.add("code", this.code);
        this.add("body", this.body);
    }
}
