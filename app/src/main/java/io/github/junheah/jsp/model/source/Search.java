package io.github.junheah.jsp.model.source;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.interfaces.V8Callback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.song.Song;

import static io.github.junheah.jsp.Utils.playListDeserializer;
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
        V8Request request = new V8Request("search('"+query+"',"+currentPage+");", new V8Callback() {
            @Override
            public void callback(String res) {
                System.out.println(res);
                result = songListDeserializer().fromJson(res, new TypeToken<List<Song>>() {}.getType());
                for(ExternalSong s : result){
                    s.setSource(source);
                }
                cb.run();   // ui updates are done in runnable cb
            }

            @Override
            public void error(Exception e) {
                e.printStackTrace();
                result = new ArrayList<>();
                cb.run();
                page--;
            }
        });
        source.runScript(request);
    }

    public List<ExternalSong> getResult() {
        return result;
    }
}
