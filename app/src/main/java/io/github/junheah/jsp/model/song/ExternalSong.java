package io.github.junheah.jsp.model.song;

public class ExternalSong extends Song{

    public ExternalSong(String name, String artist, String path, String cover) {
        super(name, artist, path, cover);
        this.TYPE="EXTERNAL";   //gson
    }
}
