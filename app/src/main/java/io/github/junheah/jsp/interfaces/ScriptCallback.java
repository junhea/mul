package io.github.junheah.jsp.interfaces;

public interface ScriptCallback {
    void callback(Object res);
    void onError(Exception e);
}
