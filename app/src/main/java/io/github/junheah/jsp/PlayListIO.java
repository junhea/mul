package io.github.junheah.jsp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.junheah.jsp.model.PlayList;

import static io.github.junheah.jsp.Utils.playListDeserializer;
import static io.github.junheah.jsp.Utils.playListSerializer;

public class PlayListIO {
    //sharedpref writer & reader
    Context context;
    SharedPreferences reader;
    SharedPreferences.Editor editor;
    Gson s, d;
    Type t;

    public PlayListIO(Context context){
        this.context = context;
        //initialize variables
        reader = context.getSharedPreferences("JSPlayer.playlists", Context.MODE_PRIVATE);
        editor = reader.edit();
        t = new TypeToken<PlayList>() {}.getType();
        s = playListSerializer();
        d = playListDeserializer();
    }

    public List<PlayList> get(){
        List<PlayList> playLists = new ArrayList<>();
        //read playlists
        Map<String,?> data = reader.getAll();

        //deserialize playlists and save as object
        PlayList list;
        t = new TypeToken<PlayList>() {}.getType();
        for(Map.Entry<String,?> e : data.entrySet()){
            list = d.fromJson((String)e.getValue(), t);
            playLists.add(list);
        }

        return playLists;
    }

    public void write(PlayList playList){
        editor.putString(playList.getName(), s.toJson(playList));
        editor.commit();
    }

    public Set<String> getNames(){
        return reader.getAll().keySet();
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
