package io.github.junhea.mul.interfaces;

import io.github.junhea.mul.model.song.Song;

public interface SongInfoObserver {
    void itemUpdated(Song song);
}
