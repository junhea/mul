package io.github.junhea.mul.interfaces;

public interface AdapterNotifier {
    void itemRemoved(int index);
    void itemAdded(int index);
    void itemUpdated(int index);
}
