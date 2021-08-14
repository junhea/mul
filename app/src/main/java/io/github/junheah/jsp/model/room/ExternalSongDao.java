package io.github.junheah.jsp.model.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

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
}
