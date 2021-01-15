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

import java.util.List;
import java.util.Map;

import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.V8Callback;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.model.source.V8Request;

import static io.github.junheah.jsp.MainApplication.defaultCover;
import static io.github.junheah.jsp.Utils.songListDeserializer;

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


    public void fetch(Context context, Runnable cb){
        if(source == null){
            SourceIO io = new SourceIO(context);
            io.load();
            source = io.getSource(this.sourceID);
        }else{
            source.runScript(new V8Request("fetchSongInfo("+new Gson().toJson(ExternalSong.this)+");", new V8Callback() {
                @Override
                public void callback(String res) {
                    //song loaded
                    ExternalSong newsong = new Gson().fromJson(res, new TypeToken<ExternalSong>(){}.getType());
                    update(newsong);
                    cb.run();
                }

                @Override
                public void error(Exception e) {
                    source.close();
                }
            }));
        }

        // one-time use of source
        if(source != null){
            //init source
            source.init(null);
            source.initThread(new V8Callback() {
                @Override
                public void callback(String res) {
                    //fetch song info
                    source.runScript(new V8Request("fetchSongInfo("+new Gson().toJson(ExternalSong.this)+");", new V8Callback() {
                        @Override
                        public void callback(String res) {
                            //song loaded
                            ExternalSong newsong = new Gson().fromJson(res, new TypeToken<ExternalSong>(){}.getType());
                            update(newsong);
                            source.close();
                            cb.run();
                        }

                        @Override
                        public void error(Exception e) {
                            source.close();
                        }
                    }));
                }

                @Override
                public void error(Exception e) {
                    source.close();
                }
            }, context, null);
        }
    }

    public void update(ExternalSong song){
        this.name = song.getName();
        this.path = song.getPath();
        this.coverUrl = song.getCoverUrl();
        this.headers = song.getHeaders();
        this.artist = song.getArtist();
    }
}
