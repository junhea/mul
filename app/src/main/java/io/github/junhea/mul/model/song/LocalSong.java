package io.github.junhea.mul.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import io.github.junhea.mul.interfaces.BitmapCallback;

@Entity(tableName="local", indices = @Index(value = {"path"}, unique = true))
public class LocalSong extends Song {

    public boolean nocover = false;

    @Ignore
    public LocalSong(long sid){
        super(sid);
    }
    @Ignore
    public LocalSong(String name, String artist, String path) {
        super(name, artist, path);
    }

    @Ignore
    public LocalSong(String name, String artist, Uri path) {
        super(name, artist, path);
    }

    public LocalSong(String name, String artist, Uri path, boolean nocover) {
        super(name, artist, path);
        this.nocover = nocover;
    }

    public void fetchData(Context context){
        //called only once on add
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, this.path);
        name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (name == null) {
            name = getPath().toString().substring(getPath().toString().lastIndexOf('/') + 1);
        }
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        byte[] artBytes = retriever.getEmbeddedPicture();
        Bitmap cover = null;
        if (artBytes != null) {
            cover = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }
        if(cover == null) this.nocover = true;
    }

    @NonNull
    @Override
    public Object clone(){
        LocalSong s = new LocalSong(name, artist, path, nocover);
        s.setSid(sid);
        return s;
    }
}
