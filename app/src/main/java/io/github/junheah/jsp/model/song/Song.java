package io.github.junheah.jsp.model.song;

import android.net.Uri;

import androidx.annotation.NonNull;

import io.github.junheah.jsp.model.PlayList;

public class Song{
    String TYPE="";   //gson

    public Song getNext(){
        return next;
    }

    public void setNext(Song song){
        next = song;
    }

    public Song getPrev(){
        return prev;
    }

    public void setPrev(Song song){
        prev = song;
    }

    transient Song prev;
    transient Song next;
    transient PlayList parent;

    String name="";
    String artist="";
    String path;
    String cover;

    public Song(String name, String artist, String path, String cover){
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.cover = cover;
        this.TYPE = "SONG";
    }

    public void setParent(PlayList parent) {
        this.parent = parent;
    }

    public PlayList getParent() {
        return this.parent;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return Uri.parse(path);
    }

    public String getArtist(){
        return artist;
    }

    public String getCover(){
        if(cover != null && cover.length()>0) return cover;
        else return null;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "  "+path + "  next: " + next;
    }
}
