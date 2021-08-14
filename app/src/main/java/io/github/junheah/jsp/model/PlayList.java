package io.github.junheah.jsp.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.interfaces.AdapterNotifier;
import io.github.junheah.jsp.interfaces.PlayListChangeCallback;
import io.github.junheah.jsp.interfaces.SongInfoObserver;
import io.github.junheah.jsp.model.song.Song;


public class PlayList extends ArrayList<Song> implements SongInfoObserver {
    //doubly linked list

    String name;
    transient AdapterNotifier notifier;
    transient PlayListChangeCallback playListChangeCallback;
    transient boolean tmp = false;
    public transient boolean cleared = false;

    transient PlayListIO playListIO;

    public String getName(){
        return name == null ? "" : name;
    }

    public void setName(String name){
        this.name = name;
    }

    public PlayList(Context context, String name) {
        super();
        this.name = name;
        this.tmp = false;
        playListIO = PlayListIO.getInstance(context);
    }

    public PlayList(Context context, String name, boolean tmp) {
        super();
        this.name = name;
        this.tmp = tmp;
        playListIO = PlayListIO.getInstance(context);
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

    public boolean add(Song song, boolean isLoad, boolean silent) {
        boolean res = super.add(song);
        song.setParent(this);
        int size = size();
        updateIndex(size-1);
        if(size>1)
            updateIndex(size-2);
        if(notifier != null && !silent) {
            notifier.itemAdded(size-1);
        }

        //notify player (if attached)
        if(playListChangeCallback != null && !silent) playListChangeCallback.playListUpdated();
        //playlist io
        if(!tmp && !isLoad) playListIO.write(PlayList.this);
        return res;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        System.out.println(o.hashCode());
        for(int i = 0; i<size(); i++){
            System.out.println("\t\t"+get(i).hashCode());
            if(get(i).hashCode() == o.hashCode()){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean add(Song song){
        return add(song, false, false);
    }

    @Override
    public void clear() {
        playListChangeCallback = null;
        notifier = null;
        cleared = true;
        super.clear();
    }


    @Override
    public void add(int index, Song song) {
        System.out.println("add  by index on "+name);
        super.add(index, song);
        song.setParent(this);
        updateIndex(index);
        updateIndex(index-1);
        updateIndex(index+1);
        if(notifier != null)
            notifier.itemAdded(index);

        //notify player (if attached)
        if(playListChangeCallback != null) playListChangeCallback.playListUpdated();

        //playlist io
        if(!tmp) playListIO.write(PlayList.this);
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
    public boolean addAll(@NonNull Collection<? extends Song> c) {
        int prev = size();
        for(Song s : c){
            add(s, true, false);
        }
        //save when adding complete
        if(!tmp) playListIO.write(PlayList.this);
        return true;
    }

    public void forcesave(){
        playListIO.write(PlayList.this);
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
            sb.append(", ");
        }
        sb.delete(sb.length()-2, sb.length()-1);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void itemUpdated(Song song) {
        if(notifier != null)
            notifier.itemUpdated(indexOf(song));
        // update metadata in db
        playListIO.write(this);
    }

    public void swap(int i, int j){
        Song tmp = get(i);
        set(i, get(j));
        set(j, tmp);
        updateIndex(i);
        if(i>0)
            updateIndex(i-1);
        if(i<size()-1)
            updateIndex(i+1);
        updateIndex(j);
        if(j>0)
            updateIndex(j-1);
        if(j<size()-1)
            updateIndex(j+1);
    }

    public void save(){
        playListIO.write(this);
    }
}