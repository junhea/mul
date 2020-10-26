package io.github.junheah.jsp.interfaces;

public interface AdapterNotifier {
    void itemRemoved(int index);
    void itemAdded(int index);
    void itemUpdated(int index);
}
