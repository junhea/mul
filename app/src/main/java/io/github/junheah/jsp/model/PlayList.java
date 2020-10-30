package io.github.junheah.jsp.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.junheah.jsp.interfaces.AdapterNotifier;
import io.github.junheah.jsp.interfaces.PlayListChangeCallback;
import io.github.junheah.jsp.interfaces.SongInfoObserver;
import io.github.junheah.jsp.model.song.Song;

public class PlayList extends ArrayList<Song> implements SongInfoObserver {
    //doubly linked list

    String name;
    transient AdapterNotifier notifier;
    transient PlayListChangeCallback playListChangeCallback;

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

    public void playListRemoved(){
        if(playListChangeCallback != null)
            playListChangeCallback.playListRemoved();
    }

    public void setNotifier(AdapterNotifier notifier){
        this.notifier = notifier;
    }

    public void setPlayListChangeCallback(PlayListChangeCallback playListChangeCallback){
        this.playListChangeCallback = playListChangeCallback;
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
            notifier.itemAdded(size-1);

        //notify player (if attached)
        if(playListChangeCallback != null) playListChangeCallback.playListUpdated();

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
            notifier.itemAdded(index);

        //notify player (if attached)
        if(playListChangeCallback != null) playListChangeCallback.playListUpdated();
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
            notifier.itemRemoved(index);

        //notify player (if attached)
        if(playListChangeCallback != null) playListChangeCallback.songRemoved(obj);

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
            sb.append(s.getName());
            sb.append("__");
            sb.append(s.getType());
            sb.append(", ");
        }
        sb.delete(sb.length()-2, sb.length()-1);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void itemUpdated(Song song) {
        notifier.itemUpdated(indexOf(song));
    }
}