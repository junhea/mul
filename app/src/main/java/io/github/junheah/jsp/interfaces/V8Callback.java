package io.github.junheah.jsp.interfaces;

public interface V8Callback {
    public void callback(String res);
    public void error(Exception e);
}
