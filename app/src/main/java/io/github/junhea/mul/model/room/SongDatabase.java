package io.github.junhea.mul.model.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.LocalSong;

@Database(entities = {LocalSong.class, ExternalSong.class}, version = 1)
public abstract class SongDatabase extends RoomDatabase {
    public abstract LocalSongDao localDao();
    public abstract ExternalSongDao externalDao();
    private static SongDatabase db;
    public static synchronized SongDatabase getInstance(Context context){
        if(db == null){
            db=Room.databaseBuilder(context.getApplicationContext(), SongDatabase.class, "songs").build();;
        }
        return db;
    }
}
