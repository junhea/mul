package io.github.junhea.mul.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.interfaces.AdapterNotifier;
import io.github.junhea.mul.interfaces.PlayListChangeCallback;
import io.github.junhea.mul.interfaces.SongInfoObserver;
import io.github.junhea.mul.model.song.Song;


public class PlayList extends ArrayList<Song> implements SongInfoObserver {

    public static final short MODE_NORMAL = 0;
    public static final short MODE_SHUFFLE = 1;
    public static final short MODE_REPEAT_SONG = 2;
    public static final short MODE_REPEAT_ALL = 3;


    String name;
    transient AdapterNotifier notifier;
    transient PlayListChangeCallback playListChangeCallback;
    transient boolean tmp = false;
    transient short mode = 0;
    transient ArrayList<Song> filtered;

    public transient boolean cleared = false;

    transient PlayListIO playListIO;

    public void setMode(short newmode, Song current){
        if(this.mode != newmode) {
            this.mode = newmode;
            if (filtered != null)
                filtered.clear();

            switch (newmode) {
                case MODE_SHUFFLE:
                    filtered = new ArrayList<>(this);
                    Collections.shuffle(filtered);
                    //put current song to top
                    filtered.remove(current);
                    filtered.add(0,current);
                    break;
            }
        }
    }

    public short getMode(){
        return this.mode;
    }

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
        int i;
        switch(mode){
            case MODE_NORMAL:
                i = indexOf(s);
                if(i>-1 && i<size()-1)
                    return get(i+1);
                return null;
            case MODE_SHUFFLE:
                i = filtered.indexOf(s);
                if(i>-1 && i<filtered.size()-1)
                    return filtered.get(i+1);
                return null;
            case MODE_REPEAT_SONG:
                return s;
            case MODE_REPEAT_ALL:
                i = indexOf(s);
                return get((i+1)%size());
        }
        return null;
    }

    public Song getPrev(Song s){
        int i;
        switch(mode){
            case MODE_NORMAL:
                i = indexOf(s);
                if(i>-1 && i>0)
                    return get(i-1);
                return null;
            case MODE_SHUFFLE:
                i = filtered.indexOf(s);
                if(i>-1 && i>0)
                    return filtered.get(i-1);
                return null;
            case MODE_REPEAT_SONG:
                return s;
            case MODE_REPEAT_ALL:
                i = indexOf(s);
                if(i == 0) return get(size()-1);
                return get((i-1)%size());
        }
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
        if(indexOf(song)>-1) return false;
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

    public void forceAdd(Song song){
        super.add(song);
        song.setParent(this);
        if(notifier != null) {
            notifier.itemAdded(size()-1);
        }

        //notify player (if attached)
        if(playListChangeCallback != null) playListChangeCallback.playListUpdated();
        //playlist io
        if(!tmp) playListIO.write(PlayList.this);
    }

    public boolean addable(Song song){
        return indexOf(song)==-1;
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

        //playlistio
        if(!tmp) playListIO.write(this);

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
        if(!tmp) playListIO.write(this);
    }

    public void swap(int i, int j){
        Song tmp = get(i);
        set(i, get(j));
        set(j, tmp);
    }
    public void deleted(){
        playListChangeCallback.playListRemoved();
    }



    public void save(){
        playListIO.write(this);
    }
}