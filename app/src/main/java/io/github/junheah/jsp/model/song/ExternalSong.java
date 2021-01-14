package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Map;

import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.model.source.Source;

import static io.github.junheah.jsp.MainApplication.defaultCover;

public class ExternalSong extends Song{
    transient Source source;
    String coverUrl;
    Map<String, String> headers;

    public ExternalSong(String name, String artist, String url, String cover, Map<String, String> headers) {
        super(name, artist, url);
        this.coverUrl = cover;
        this.type="EXTERNAL";   //gson
        this.headers = headers;
    }
    public void setSource(Source source){
        this.source = source;
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
}
