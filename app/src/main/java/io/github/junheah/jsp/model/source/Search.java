package io.github.junheah.jsp.model.source;

import com.google.gson.reflect.TypeToken;

import org.mozilla.javascript.JavaAdapter;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.Song;

import static io.github.junheah.jsp.Utils.songListDeserializer;

public class Search {
    Source source;
    String query;
    List<ExternalSong> result;
    int page;

    public Search(Source source, String query){
        this.source = source;
        this.query = query;
        page = 0;
    }

    public void fetch(Runnable cb){
        int currentPage = page++;
        ScriptRequest request = new ScriptRequest("search", new Object[]{query, currentPage}, new ScriptCallback() {
            @Override
            public void callback(Object res) {
                result = (List<ExternalSong>) JavaAdapter.convertResult(res, List.class);
                for (ExternalSong s : result) {
                    s.setSource(source);
                }
                cb.run();   // ui updates are done in runnable cb
            }
        });
        source.runScript(request);
    }

    public List<ExternalSong> getResult() {
        return result;
    }
}
