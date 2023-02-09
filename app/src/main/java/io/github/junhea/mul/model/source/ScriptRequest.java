package io.github.junhea.mul.model.source;

import android.content.Context;

import io.github.junhea.mul.interfaces.ScriptCallback;


public class ScriptRequest {
    ScriptCallback callback;
    String function;
    Object[] args;
    Context context;

    public ScriptRequest(Context context, String function, Object[] args, ScriptCallback callback) {
        this.context = context;
        this.callback = callback;
        this.function = function;
        this.args = args;
    }

    public ScriptRequest(String function, Object[] args, ScriptCallback callback) {
        this.callback = callback;
        this.function = function;
        this.args = args;
    }

    public Context getContext(){
        return this.context;
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
