package io.github.junhea.mul.interfaces;

import io.github.junhea.mul.model.song.Song;

public interface PlayListChangeCallback {
    void playListRemoved();
    void playListUpdated();
    void songRemoved(Song song);
}
