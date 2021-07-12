package io.github.junheah.jsp.model.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;

@Database(entities = {LocalSong.class, ExternalSong.class}, version = 1)
public abstract class SongDatabase extends RoomDatabase {
    public abstract LocalSongDao localDao();
    public abstract ExternalSongDao externalDao();
    public static SongDatabase getInstance(Context context){
        return Room.databaseBuilder(context, SongDatabase.class, "songs").build();
    }
}
