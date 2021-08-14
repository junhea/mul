package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

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
import io.github.junheah.jsp.model.room.MapConverter;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.model.source.ScriptRequest;

import static io.github.junheah.jsp.MainApplication.baseScript;
import static io.github.junheah.jsp.MainApplication.client;
import static io.github.junheah.jsp.Utils.readFile;

@Entity(tableName = "external", indices = @Index(value = {"id"}, unique = true))
public class ExternalSong extends Song{


    @Ignore
    public ExternalSong(long sid){
        super(sid);
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    String sourceID;
    String id;
    String coverUrl;
    @TypeConverters(MapConverter.class)
    Map<String, String> headers;
    transient Source source;
    transient boolean checked = false;

    public ExternalSong(String id, String name, String artist, String path, String coverUrl, Map<String, String> headers) {
        super(name, artist, path);
        this.coverUrl = coverUrl;
        this.headers = headers;
        this.id = id;
    }

    public boolean getChecked(){
        return checked;
    }
    public void toggleCheck(){
        checked = !checked;
    }
    public void resetCheck(){
        checked = false;
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
                SourceIO io = SourceIO.getInstance(context);
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
            SourceIO sourceIO = SourceIO.getInstance(context);
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
