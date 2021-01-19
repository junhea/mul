package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.util.Map;

import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.model.source.ScriptRequest;

import static io.github.junheah.jsp.MainApplication.baseScript;
import static io.github.junheah.jsp.MainApplication.client;
import static io.github.junheah.jsp.MainApplication.defaultCover;
import static io.github.junheah.jsp.Utils.readFile;

public class ExternalSong extends Song{

    String sourceID;
    String id;
    String coverUrl;
    Map<String, String> headers;
    transient Source source;

    public ExternalSong(String songID, String name, String artist, String url, String cover, Map<String, String> headers) {
        super(name, artist, url);
        this.coverUrl = cover;
        this.type="EXTERNAL";   //gson
        this.headers = headers;
        this.id = songID;
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
            if(context == null){
                callback.onError(new Exception("need context"));
            }else {
                SourceIO io = new SourceIO(context);
                io.load();
                source = io.getSource(this.sourceID);
            }
        }
        //fetch song info
        source.runScript(new ScriptRequest("fetchSongInfo",new Object[]{ExternalSong.this}, callback));
    }

    public void fetchFromCurrentThread(Context context){
        try {
            org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();
            rhino.setOptimizationLevel(-1);
            ScriptableObject scope = new ImporterTopLevel(rhino);
            //pass client
            Object wrappedClient = org.mozilla.javascript.Context.javaToJS(client, scope);
            ScriptableObject.putProperty(scope, "httpClient", wrappedClient);
            //for debugging
            Object wrappedOut = org.mozilla.javascript.Context.javaToJS(System.out, scope);
            ScriptableObject.putProperty(scope, "out", wrappedOut);
            //base script
            rhino.evaluateString(scope, baseScript, "base", 1, null);
            //script
            SourceIO sourceIO = new SourceIO(context);
            sourceIO.load();
            File script = sourceIO.getSource(this.sourceID).getScript();
            rhino.evaluateString(scope, readFile(script), "JavaScript", 1, null);

            //run script
            Function fct = (Function)scope.get("fetchSongInfo", scope);
            fct.call(rhino, scope, scope, new Object[]{ExternalSong.this});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getId() {
        return id;
    }

    public void setId(String ID) {
        this.id = ID;
    }
}
