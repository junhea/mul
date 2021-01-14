package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;

public interface SearchResultInterface {
    void clickedSong(ExternalSong song);
    void clickedSongContainer(ExternalSongContainer container);
}
