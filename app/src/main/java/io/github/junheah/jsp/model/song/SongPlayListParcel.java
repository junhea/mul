package io.github.junheah.jsp.model.song;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.model.PlayList;

public class SongPlayListParcel{
    public List<Song> songs;
    public PlayList playList;
    public SongPlayListParcel(PlayList playList, List<Song> songs){
        this.songs = songs;
        this.playList = playList;
    }

    public SongPlayListParcel(PlayList playList, Song song){
        this.songs = new ArrayList<>();
        this.songs.add(song);
        this.playList = playList;
    }
}
