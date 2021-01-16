package io.github.junheah.jsp.model.source;

import io.github.junheah.jsp.interfaces.ScriptCallback;


public class ScriptRequest {
    ScriptCallback callback;
    String function;
    Object[] args;

    public ScriptRequest(String function, Object[] args, ScriptCallback callback) {
        this.callback = callback;
        this.function = function;
        this.args = args;
    }

    public ScriptCallback getCallback() {
        return callback;
    }

    public String getFunction() {
        return function;
    }

    public Object[] getArgs() {
        return args;
    }
}
