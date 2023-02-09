package io.github.junhea.mul.model.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.github.junhea.mul.model.song.ExternalSong;

@Dao
public interface ExternalSongDao {
    @Query("SELECT * FROM external")
    List<ExternalSong> getAll();
    @Insert
    long[] insertAll(ExternalSong... songs);
    @Insert
    long insert(ExternalSong song);
    @Query("SELECT * FROM external WHERE sid = (:sid)")
    ExternalSong get(long sid);
    @Query("SELECT * FROM external WHERE id = (:id)")
    ExternalSong findWithId(String id);
    @Delete
    void delete(ExternalSong song);
}
