package io.github.junheah.jsp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.Song;

import static io.github.junheah.jsp.model.song.Song.EXTERNAL;
import static io.github.junheah.jsp.model.song.Song.LOCAL;

public class PlayListIO {
    Context context;
    SharedPreferences reader;
    SharedPreferences.Editor editor;
    Gson g;

    List<String> keys;

    private static PlayListIO io;

    public static synchronized PlayListIO getInstance(Context context){
        if(io == null){
            io = new PlayListIO(context.getApplicationContext());
        }
        return io;
    }

    public PlayListIO(Context context){
        this.context = context;
        //initialize variables
        reader = context.getSharedPreferences("JSPlayer.playlists", Context.MODE_PRIVATE);
        editor = reader.edit();
        //only get keys & load on user request (dont save playlist object!!!)
        keys = new ArrayList<>();
        keys.addAll(reader.getAll().keySet());
        g = new Gson();
    }

    public List<long[]> getids(String key){
        if(keys.indexOf(key) > -1){
            return g.fromJson((String) reader.getString(key, "[]"), new TypeToken<List<long[]>>() {}.getType());
        }
        //not found
        return new ArrayList<>();
    }

    public PlayList get(String key){
        PlayList pl = new PlayList(context, key);
        return pl;
    }

    public void detach(PlayList pl){
        System.out.println("detach!!! " + pl.getName());
    }

    public String getRaw(){
        Map<String, List<long[]>> data = new HashMap<>();
        for(String k : keys){
            data.put(k, getids(k));
        }
        return g.toJson(data);
    }


    public void writeRaw(String s){
        try {
            Map<String, List<long[]>> data = g.fromJson(s, new TypeToken<Map<String, List<long[]>>>() {}.getType());
            editor.clear();
            for(String k : data.keySet()){
                editor.putString(k, g.toJson(data.get(k)));
            }
            editor.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public PlayList create(String name){
        keys.add(name);
        PlayList playList = new PlayList(context, name);
        write(playList);
        return playList;
    }

    public void addSongs(String playList, List<long[]> items){
        List<long[]> ids = getids(playList);
        for(long[] id : items){
            ids.add(id);
        }
        editor.putString(playList, g.toJson(ids));
        editor.commit();
    }

    public void write(PlayList playList){
        List<long[]> ids = new ArrayList<>();
        for(Song s : playList){
            if(s instanceof ExternalSong){
                ids.add(new long[]{EXTERNAL, s.getSid()});
            }else{
                ids.add(new long[]{LOCAL, s.getSid()});
            }
        }
        editor.putString(playList.getName(), g.toJson(ids));
        editor.commit();
    }

    public List<String> getNames(){
        return keys;
    }

    public void rename(PlayList playList, String newName){
        delete(playList);
        playList.setName(newName);
        write(playList);
    }

    public void delete(PlayList playList){
        delete(playList.getName());
    }

    public void delete(String name){
        editor.remove(name);
        editor.commit();
    }
}
