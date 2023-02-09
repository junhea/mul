package io.github.junhea.mul.model;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.model.room.SongDatabase;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;

import static io.github.junhea.mul.model.song.Song.LOCAL;

public class Library extends PlayList {

    Map<Long,Song> refl;
    Map<Long,Song> refe;

    public Song getWithId(long[] sid){
        if(sid[0] == LOCAL){
            return refl.get(sid[1]);
        }else{
            return refe.get(sid[1]);
        }
    }

    public synchronized int addWithSort(Song song) {
        if(indexOf(song) > -1) return -1;
        if(song instanceof LocalSong)
            refl.put(song.getSid(), song);
        else
            refe.put(song.getSid(), song);
        int index = Collections.binarySearch(this, song);
        if (index < 0) {
            index = ~index;
        }
        super.add(index, song);
        return index;
    }

    @Override
    public boolean add(Song song, boolean isLoad, boolean silent) {
        return false;
    }

    @Override
    public boolean add(Song song) {
        return false;
    }

    @Override
    public void add(int index, Song song) {

    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends Song> c) {
        return false;
    }

    public void init(Context context){
        refl = new HashMap<>();
        refe = new HashMap<>();
        SongDatabase db = SongDatabase.getInstance(context);
        for(Song s : db.localDao().getAll()){
            this.addWithSort(s);
        }
        for(Song s : db.externalDao().getAll()){
            this.addWithSort(s);
        }
    }

    public Library(Context context) {
        this.tmp = true;
        this.name = "";
        playListIO = PlayListIO.getInstance(context);
        init(context);
    }
}
