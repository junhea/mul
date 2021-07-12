package io.github.junheah.jsp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

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
    List<PlayList> lists;
    Gson g;

    List<String> keys;

    public PlayListIO(Context context){
        this.context = context;
        //initialize variables
        reader = context.getSharedPreferences("JSPlayer.playlists", Context.MODE_PRIVATE);
        editor = reader.edit();
        //only get keys & load on user request (dont save playlist object!!!)
        keys = new ArrayList<>();
        keys.addAll(reader.getAll().keySet());
        g = new Gson();
        lists = new ArrayList<>();
    }

    public List<long[]> getids(String key){
        if(keys.indexOf(key) > -1){
            return g.fromJson((String) reader.getString(key, "[]"), new TypeToken<List<long[]>>() {}.getType());
        }
        //not found
        return null;
    }

    public PlayList get(String key){
        PlayList pl = new PlayList(key);
        lists.add(pl);
        return pl;
    }

    public void detach(PlayList pl){
        System.out.println("detach!!! " + pl.getName());
        lists.remove(pl);
    }

    public String getRaw(){
        return "";
    }


    public void writeRaw(String s){
        try {
            JSONObject data = new JSONObject(s);
            Iterator keys = data.keys();
            while(keys.hasNext()){
                String key = (String)keys.next();
                editor.putString(key, data.getJSONObject(key).toString().replaceAll("\\\\",""));
            }
            editor.commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public PlayList create(String name){
        keys.add(name);
        PlayList playList = new PlayList(name);
        write(playList);
        return playList;
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
