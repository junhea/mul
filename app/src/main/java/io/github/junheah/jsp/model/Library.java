package io.github.junheah.jsp.model;

import android.content.Context;

import java.util.Collections;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.model.room.ExternalSongDao;
import io.github.junheah.jsp.model.room.LocalSongDao;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.Song;

public class Library extends PlayList {

    public int addWithSort(Song song) {
        int index = Collections.binarySearch(this, song);
        if (index < 0) {
            index = ~index;
        }
        super.add(index, song);
        return index;
    }

    public void init(Context context){
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
