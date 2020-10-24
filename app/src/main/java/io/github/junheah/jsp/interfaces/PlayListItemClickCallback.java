package io.github.junheah.jsp.interfaces;

import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.Song;

public interface PlayListItemClickCallback {
    void SongClicked(Song song, PlayList list);
}
