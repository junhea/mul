package io.github.junheah.jsp.model.source;

import java.util.Map;

public class Request {
    String url;
    Map<String, String> headers;

    Request(String url, Map<String, String> headers){
        this.url = url;
        this.headers = headers;
    }
}
