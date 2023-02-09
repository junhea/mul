package io.github.junhea.mul.interfaces;

import io.github.junhea.mul.model.song.ExternalSongContainer;
import io.github.junhea.mul.model.song.Song;

public interface SearchResultInterface {
    void clickedSong(Song song);
    void clickedSongContainer(ExternalSongContainer container);
    void clickedLoadMore();
    void longClickedSong(Song song);
}
