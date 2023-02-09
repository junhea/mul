package io.github.junhea.mul.interfaces;

import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.song.Song;

public interface PlayListItemClickCallback {
    //any changes made to playlist must be done in MainActivity (because it is the only one bound to player service)
    void SongClicked(Song song, PlayList list);
    void SongLongClicked(Song song, PlayList list);
    void SongLongClicked(Song song);
}
