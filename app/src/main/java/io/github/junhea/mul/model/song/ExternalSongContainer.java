package io.github.junhea.mul.model.song;

import android.content.Context;

import org.mozilla.javascript.JavaAdapter;

import java.util.List;
import java.util.Map;

import io.github.junhea.mul.interfaces.ScriptCallback;
import io.github.junhea.mul.model.source.ScriptRequest;


public class ExternalSongContainer extends ExternalSong{

    transient List<ExternalSong> songs;
    String etype;
    int page = 0;

    public ExternalSongContainer(String id, String name, String artist, String url, String cover, Map<String, String> headers, String etype) {
        super(id, name, artist, url, cover, headers);
        this.etype = etype;
    }

    public ExternalSongContainer(String id, String name, String artist, String url, String cover, Map<String, String> headers, String etype, List<ExternalSong> songs) {
        super(id, name, artist, url, cover, headers);
        this.songs = songs;
        this.etype = etype;
    }

    public String getEtype() {
        return etype;
    }

    public void resetPage(){
        this.page=0;
    }

    @Override
    public void fetch(Context context, ScriptCallback cb){
        int current = page++;
        //should only be called from search res
        ScriptRequest request = new ScriptRequest(context, "fetchContainerInfo", new Object[]{ExternalSongContainer.this, current}, new ScriptCallback() {
            @Override
            public void callback(Object res) {
                songs = (List<ExternalSong>) JavaAdapter.convertResult(res, List.class);
                for(ExternalSong s : songs){
                    s.setSource(source);
                }
                cb.callback(res);   // ui updates are done in runnable cb
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
        source.runScript(request);
    }

    public List<ExternalSong> getSongs(){
        return songs;
    }

}
