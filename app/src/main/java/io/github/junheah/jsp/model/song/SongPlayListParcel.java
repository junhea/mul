package io.github.junheah.jsp.model.song;

import io.github.junheah.jsp.model.PlayList;

public class SongPlayListParcel{
    public Song song;
    public PlayList playList;
    public SongPlayListParcel(PlayList playList, Song song){
        this.song = song;
        this.playList = playList;
    }
}
