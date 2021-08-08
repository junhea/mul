package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.song.Song;

public interface SongCallback {
    void notify(Song song);
}
