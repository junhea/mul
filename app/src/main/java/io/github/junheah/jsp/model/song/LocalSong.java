package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.util.HashMap;

import io.github.junheah.jsp.model.song.Song;

public class LocalSong extends Song {

    transient Bitmap coverBitmap;

    public LocalSong(String name, String artist, String path, String cover, Context context) {
        super(name, artist, path, cover);
        this.type = "LOCAL";    //gson
        getInfo(context);
    }

    public void getInfo(Context context){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, getUri());
        name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        byte[] artBytes =  retriever.getEmbeddedPicture();
        if(artBytes!=null)
        {
            coverBitmap =  BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }
    }

    public Bitmap getCoverBitmap(){
        return coverBitmap;
    }
}
