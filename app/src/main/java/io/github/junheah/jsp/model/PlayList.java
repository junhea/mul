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

    public Song getNext(Song s){
        int i = indexOf(s);
        if(i>-1 && i<size()-1)
            return get(i+1);
        return null;
    }

    public Song getPrev(Song s){
        int i = indexOf(s);
        if(i>-1 && i>0)
            return get(i-1);
        return null;
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
        if(notifier != null && !silent) {
            notifier.itemAdded(size()-1);
        }

        //notify player (if attached)
        if(playListChangeCallback != null && !silent) playListChangeCallback.playListUpdated();
        //playlist io
        if(!tmp && !isLoad) playListIO.write(PlayList.this);
        return res;
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
        super.add(index, song);
        song.setParent(this);
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
    }

    public void save(){
        playListIO.write(this);
    }
}