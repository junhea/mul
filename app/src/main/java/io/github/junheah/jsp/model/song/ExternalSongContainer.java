package io.github.junheah.jsp.model.song;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.junheah.jsp.interfaces.V8Callback;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.model.source.V8Request;

public class ExternalSongContainer extends ExternalSong{
    transient List<ExternalSong> songs;
    String etype;

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

    public void fetch(Runnable cb){
        V8Request request = new V8Request("fetchContainerInfo("+new Gson().toJson(ExternalSongContainer.this)+");", new V8Callback() {
            @Override
            public void callback(String res) {
                System.out.println(res);
                songs = new Gson().fromJson(res, new TypeToken<List<ExternalSong>>() {}.getType());
                for(ExternalSong s : songs){
                    s.setSource(source);
                }
                cb.run();   // ui updates are done in runnable cb
            }

            @Override
            public void error(Exception e) {
                e.printStackTrace();
            }
        });
        source.runScript(request);
    }

    public List<ExternalSong> getSongs(){
        return songs;
    }

}
