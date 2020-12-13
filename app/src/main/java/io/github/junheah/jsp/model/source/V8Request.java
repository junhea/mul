package io.github.junheah.jsp.model.source;

import io.github.junheah.jsp.interfaces.V8Callback;

public class V8Request {
    V8Callback callback;
    String script;

    public V8Request(V8Callback callback, String script) {
        this.callback = callback;
        this.script = script;
    }

    public V8Callback getCallback() {
        return callback;
    }

    public String getScript() {
        return script;
    }
}
