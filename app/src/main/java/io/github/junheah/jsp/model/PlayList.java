package io.github.junheah.jsp.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PlayList extends ArrayList<Song> {
    //doubly linked list

    String name;

    public PlayList(String name) {
        super();
        this.name = name;
    }

    @Override
    public boolean add(Song song) {
        boolean res = super.add(song);
        int size = size();
        updateIndex(size-1);
        if(size>1)
            updateIndex(size-2);
        return res;
    }

    @Override
    public void add(int index, Song song) {
        super.add(index, song);
        updateIndex(index);
        updateIndex(index-1);
        updateIndex(index+1);
    }

    @Override
    public Song remove(int index) {
        Song obj = super.remove(index);
        int size = size();
        if(index == 0){
            if(size > 0){
                updateIndex(index);
            }
        }else if(index == size-1){
            updateIndex(index-1);
        }else{
            updateIndex(index);
            updateIndex(index-1);
        }
        return obj;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        remove(indexOf(o));
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