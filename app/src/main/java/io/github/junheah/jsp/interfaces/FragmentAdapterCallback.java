package io.github.junheah.jsp.interfaces;

public interface FragmentAdapterCallback {
    void addItem(Object obj);
    void insertItem(int index, Object obj);
    void removeItem(Object obj);
}
