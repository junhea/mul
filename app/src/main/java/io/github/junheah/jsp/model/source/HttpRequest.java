package io.github.junheah.jsp.model.source;

import java.util.Map;

public class HttpRequest {
    String url;
    Map<String, String> headers;

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
