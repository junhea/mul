package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.SongInfoObserver;
import io.github.junheah.jsp.model.PlayList;

import static io.github.junheah.jsp.MainApplication.defaultCover;

public class Song{

    //todo : on application start, automatically parse metadata in background

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
    transient SongInfoObserver callback;
    transient boolean isLoaded; // = false

    String name="";
    String artist="";
    String path;
    transient Bitmap cover;
    String type;   //gson

    public Song(String name, String artist, String path){
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.type = "SONG";
    }

    public void setParent(PlayList parent) {
        this.parent = parent;
        this.callback = parent;
    }

    public PlayList getParent() {
        return this.parent;
    }

    public String getType(){
        return this.type;
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

    public final Bitmap getCover(){
        if(isLoaded) {
            if (cover == null)
                return defaultCover;    //tried but no cover
            else
                return cover;       //tried and has cover
        }else
            return null;        //havent tried yet : need load
    }

    public synchronized boolean loadCover(Context context, BitmapCallback bitmapCallback){
        if(isLoaded) {
            if(bitmapCallback != null) {
                bitmapCallback.resourceLoaded(getCover());
            }
            return true;
        }
        isLoaded = true;
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

    public void setType(String type) {
        this.type = type;
    }
}
