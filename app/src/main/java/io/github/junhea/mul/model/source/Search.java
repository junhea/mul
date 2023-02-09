package io.github.junhea.mul.model.source;

import android.content.Context;

import org.mozilla.javascript.JavaAdapter;

import java.util.List;

import io.github.junhea.mul.interfaces.ScriptCallback;
import io.github.junhea.mul.model.song.ExternalSong;


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

    public void fetch(ScriptCallback cb){
        fetch(null, cb);
    }

    public void fetch(Context context, ScriptCallback cb){
        int currentPage = page++;
        ScriptRequest request = new ScriptRequest(context, "search", new Object[]{query, currentPage}, new ScriptCallback() {
            @Override
            public void callback(Object res) {
                result = (List<ExternalSong>) JavaAdapter.convertResult(res, List.class);
                for (ExternalSong s : result) {
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

    public List<ExternalSong> getResult() {
        return result;
    }
}
