package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.mozilla.javascript.JavaAdapter;

import java.util.List;
import java.util.Map;

import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.model.source.ScriptRequest;

import static io.github.junheah.jsp.MainApplication.defaultCover;

public class ExternalSong extends Song{

    String sourceID;
    String coverUrl;
    Map<String, String> headers;
    transient Source source;

    public ExternalSong(String name, String artist, String url, String cover, Map<String, String> headers) {
        super(name, artist, url);
        this.coverUrl = cover;
        this.type="EXTERNAL";   //gson
        this.headers = headers;
    }
    public void setSource(Source source){
        this.source = source;
        this.sourceID = source.getName();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public synchronized boolean loadCover(Context context, BitmapCallback bitmapCallback){
        if(super.loadCover(context, bitmapCallback))
            return true;

        if(coverUrl != null && coverUrl.length()>0) {
            Glide.with(context)
                    .asBitmap()
                    .load(coverUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            cover = resource;
                            if(bitmapCallback != null) bitmapCallback.resourceLoaded(getCover());
                            callback.itemUpdated(ExternalSong.this);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }else{
            if(bitmapCallback != null) bitmapCallback.resourceLoaded(defaultCover);
        }
        return true;
    }

    public String getPath(){
        return this.path;
    }


    public void fetch(ScriptCallback callback){
        fetch(null, callback);
    }

    public void fetch(Context context, ScriptCallback callback){
        if(source == null){
            SourceIO io = new SourceIO(context);
            io.load();
            source = io.getSource(this.sourceID);
        }
        //fetch song info
        source.runScript(new ScriptRequest("fetchSongInfo",new Object[]{ExternalSong.this}, callback));
    }

    public void update(ExternalSong song){
        this.name = song.getName();
        this.path = song.getPath();
        this.coverUrl = song.getCoverUrl();
        this.headers = song.getHeaders();
        this.artist = song.getArtist();
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
