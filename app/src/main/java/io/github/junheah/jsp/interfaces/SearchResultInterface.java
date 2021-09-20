package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.song.Song;

public interface SearchResultInterface {
    void clickedSong(Song song);
    void clickedSongContainer(ExternalSongContainer container);
    void clickedLoadMore();
    void longClickedSong(Song song);
}
