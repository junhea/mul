package io.github.junhea.mul.model.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.github.junhea.mul.model.song.LocalSong;

@Dao
public interface LocalSongDao {
    @Query("SELECT * FROM local")
    List<LocalSong> getAll();
    @Insert
    long[] insertAll(LocalSong... songs);
    @Insert
    long insert(LocalSong song);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long replace(LocalSong song);
    @Query("SELECT * FROM local WHERE sid = (:sid)")
    LocalSong get(long sid);
    @Query("SELECT * FROM local WHERE path = (:path)")
    LocalSong findWithPath(String path);
    @Delete
    void delete(LocalSong song);
}
