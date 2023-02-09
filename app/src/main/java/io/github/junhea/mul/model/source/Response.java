package io.github.junhea.mul.model.source;

import java.io.IOException;

import okhttp3.Headers;

public class Response {
    Headers headers;
    String body;
    int code;
    public Response(okhttp3.Response r)throws IOException {
        this.headers = r.headers();
        this.body = r.body().string();
        this.code = r.code();
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public int getCode() {
        return code;
    }
}
