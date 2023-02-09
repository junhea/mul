package io.github.junhea.mul.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

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

    public LocalSong(String name, String artist, String path, boolean nocover) {
        super(name, artist, path);
        this.nocover = nocover;
    }

    @Override
    public boolean loadCover(Context context, BitmapCallback bitmapCallback){
        if (super.loadCover(context, bitmapCallback))
            return true;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getPath());

        byte[] artBytes = retriever.getEmbeddedPicture();
        Bitmap cover = null;
        if (artBytes != null) {
            cover = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }
        bitmapCallback.resourceLoaded(cover);
        return true;
    }

    public void fetchData(){
        //called only once on add
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this.path);
        name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (name == null) {
            name = getPath().substring(getPath().lastIndexOf('/') + 1);
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
