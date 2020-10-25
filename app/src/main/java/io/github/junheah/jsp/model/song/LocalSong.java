package io.github.junheah.jsp.model.song;

import io.github.junheah.jsp.model.song.Song;

public class LocalSong extends Song {
    public LocalSong(String name, String artist, String path, String cover) {
        super(name, artist, path, cover);
        this.TYPE = "LOCAL";    //gson
    }

    public void getInfo(){

    }
}
