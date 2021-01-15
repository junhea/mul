package io.github.junheah.jsp.model.song;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.interfaces.V8Callback;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.model.source.V8Request;

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

    @Override
    public void fetch(Context context, Runnable cb){
        int current = page++;
        //should only be called from search res
        V8Request request = new V8Request("fetchContainerInfo("+new Gson().toJson(ExternalSongContainer.this)+","+current+");", new V8Callback() {
            @Override
            public void callback(String res) {
                System.out.println(res);
                songs = songListDeserializer().fromJson(res, new TypeToken<List<Song>>() {}.getType());
                for(ExternalSong s : songs){
                    s.setSource(source);
                }
                cb.run();   // ui updates are done in runnable cb
            }

            @Override
            public void error(Exception e) {
                e.printStackTrace();
                songs = new ArrayList<>();
                cb.run();
            }
        });
        source.runScript(request);
    }

    public List<ExternalSong> getSongs(){
        return songs;
    }

}
