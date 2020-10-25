package io.github.junheah.jsp.interfaces;

public interface PlayListAdapterNotifier {
    void songRemoved(int index);
    void songAdded(int index);
    void songUpdated(int index);
}
