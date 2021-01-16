package io.github.junheah.jsp.model.song;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.mozilla.javascript.JavaAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.model.source.ScriptRequest;

import static io.github.junheah.jsp.Utils.songListDeserializer;

public class ExternalSongContainer extends ExternalSong{

    transient List<ExternalSong> songs;
    String etype;
    int page = 0;

    public ExternalSongContainer(String name, String artist, String url, String cover, Map<String, String> headers, String etype) {
        super(name, artist, url, cover, headers);
        this.type="EXTERNAL.CONTAINER";
        this.etype = etype;
    }

    public ExternalSongContainer(String name, String artist, String url, String cover, Map<String, String> headers, String etype, List<ExternalSong> songs) {
        super(name, artist, url, cover, headers);
        this.songs = songs;
        this.etype = etype;
        this.type="EXTERNAL.CONTAINER";
    }

    public String getEtype() {
        return etype;
    }

    @Override
    public void fetch(Context context, Runnable cb){
        int current = page++;
        //should only be called from search res
        ScriptRequest request = new ScriptRequest("fetchContainerInfo", new Object[]{ExternalSongContainer.this, current}, new ScriptCallback() {
            @Override
            public void callback(Object res) {
                System.out.println(res);
                songs = (List<ExternalSong>) JavaAdapter.convertResult(res, List.class);
                for(ExternalSong s : songs){
                    s.setSource(source);
                }
                cb.run();   // ui updates are done in runnable cb
            }
        });
        source.runScript(request);
    }

    public List<ExternalSong> getSongs(){
        return songs;
    }

}
