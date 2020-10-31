package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.util.HashMap;

import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.model.song.Song;

import static io.github.junheah.jsp.MainApplication.defaultCover;

public class LocalSong extends Song {

    public LocalSong(String name, String artist, String path) {
        super(name, artist, path);
        this.type = "LOCAL";    //gson
    }

    @Override
    public synchronized boolean loadCover(Context context, BitmapCallback bitmapCallback){
        if (super.loadCover(context, bitmapCallback))
            return true;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, getUri());
        name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        byte[] artBytes = retriever.getEmbeddedPicture();
        if (artBytes != null) {
            cover = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }

        if(bitmapCallback !=null) bitmapCallback.resourceLoaded(getCover());
        callback.itemUpdated(this);
        return true;

    }
}
