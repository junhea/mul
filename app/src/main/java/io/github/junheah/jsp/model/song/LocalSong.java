package io.github.junheah.jsp.model.song;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.HashMap;

import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.model.song.Song;


public class LocalSong extends Song {

    public boolean nocover = false;

    public LocalSong(String name, String artist, String path) {
        super(name, artist, path);
        this.type = "LOCAL";    //gson
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
    }
}
