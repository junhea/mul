package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.song.Song;

public interface PlayListChangeCallback {
    void playListRemoved();
    void playListUpdated();
    void songRemoved(Song song);
}
