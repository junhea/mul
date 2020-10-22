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

}
