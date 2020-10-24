package io.github.junheah.jsp.model;

import androidx.annotation.NonNull;

public class Song{
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

    Song prev;
    Song next;

    String name="";
    String artist="";
    String url;

    public Song(String name, String artist, String url){
        this.name = name;
        this.artist = artist;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getArtist(){
        return artist;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "  "+url + "  next: " + next;
    }
}
