package io.github.junhea.mul.interfaces;

public interface ScriptCallback {
    void callback(Object res);
    void onError(Exception e);
}
