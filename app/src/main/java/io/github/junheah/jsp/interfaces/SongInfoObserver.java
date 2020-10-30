package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.song.Song;

public interface SongInfoObserver {
    void itemUpdated(Song song);
}
