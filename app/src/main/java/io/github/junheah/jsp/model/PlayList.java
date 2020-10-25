package io.github.junheah.jsp.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.junheah.jsp.interfaces.PlayListAdapterNotifier;
import io.github.junheah.jsp.model.song.Song;

public class PlayList extends ArrayList<Song> {
    //doubly linked list

    String name;
    transient public PlayListAdapterNotifier notifier;

    public String getName(){
        return name == null ? "" : name;
    }

    public void setName(String name){
        this.name = name;
    }

    public PlayList(String name) {
        super();
        this.name = name;
    }

    public void setNotifier(PlayListAdapterNotifier notifier){
        this.notifier = notifier;
    }

    public PlayList() {
        super();
    }

    @Override
    public boolean add(Song song) {
        boolean res = super.add(song);
        song.setParent(this);
        int size = size();
        updateIndex(size-1);
        if(size>1)
            updateIndex(size-2);
        if(notifier != null)
            notifier.songAdded(size-1);
        return res;
    }

    @Override
    public void add(int index, Song song) {
        super.add(index, song);
        song.setParent(this);
        updateIndex(index);
        updateIndex(index-1);
        updateIndex(index+1);
        if(notifier != null)
            notifier.songAdded(index);
    }

    @Override
    public Song remove(int index) {
        Song obj = super.remove(index);
        int size = size();
        if(index == 0){
            if(size > 0){
                updateIndex(index);
            }
        }else if(index >= size-1){
            updateIndex(index-1);
        }else{
            updateIndex(index);
            updateIndex(index-1);
        }
        if(notifier != null)
            notifier.songRemoved(index);
        return obj;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        int index = indexOf(o);
        remove(index);
        return true;
    }

    private void updateIndex(int index){
        int size = size();
        if(index == 0){
            if(size>1)
                updateNext(index);
            else
                get(index).setNext(null);
            get(index).setPrev(null);
        }else if(index == size-1){
            updatePrev(index);
            get(index).setNext(null);
        }else {
            // in between
            updatePrev(index);
            updateNext(index);
        }
    }

    private void updatePrev(int index){
        get(index).setPrev(get(index-1));
    }

    private void updateNext(int index){
        get(index).setNext(get(index+1));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for(Song s : this){
            sb.append(s.getPrev());
            sb.append('|');
            sb.append(s);
            sb.append('|');
            sb.append(s.getNext());
            sb.append(", ");
        }
        sb.delete(sb.length()-2, sb.length()-1);
        sb.append(']');
        return sb.toString();
    }
}