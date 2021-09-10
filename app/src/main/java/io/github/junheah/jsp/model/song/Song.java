package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.PrimaryKey;

import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.SongInfoObserver;
import io.github.junheah.jsp.model.PlayList;

public class Song implements Comparable<Song>{
    public final static short NONE = -1;
    public final static short LOCAL = 0;
    public final static short EXTERNAL = 1;
    public final static short EXTERNAL_CONTAINER = 2;

    @PrimaryKey(autoGenerate = true)
    long sid;

    //todo : on application start, automatically parse metadata in background
    transient PlayList parent;
    String name="";
    String artist="";
    String path;
    transient Uri cover;

    public Song(long sid){
        this.sid = sid;
    }

    public Song(String name, String artist, String path){
        this.name = name;
        this.artist = artist;
        this.path = path;
    }

    public void setParent(PlayList parent) {
        this.parent = parent;
    }

    public PlayList getParent() {
        return this.parent;
    }

    public String getName() {
        return this.name;
    }

    public Uri getUri() {
        return Uri.parse(path);
    }

    public String getPath() {return path;}

    public String getArtist(){
        return this.artist;
    }

    public synchronized boolean loadCover(Context context, BitmapCallback bitmapCallback){
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void fetchData(){ }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    @Override
    public int compareTo(Song o) {
        return this.getName().compareTo(o.getName());
    }

    @NonNull
    @Override
    public Object clone(){
        Song s  = new Song(name, artist, path);
        s.setSid(sid);
        return s;
    }
}
