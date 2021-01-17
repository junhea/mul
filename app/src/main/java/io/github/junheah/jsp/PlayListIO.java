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

import static io.github.junheah.jsp.Utils.playListDeserializer;
import static io.github.junheah.jsp.Utils.playListSerializer;

public class PlayListIO {
    //sharedpref writer & reader
    Context context;
    SharedPreferences reader;
    SharedPreferences.Editor editor;
    Gson s, d;
    Type t;

    //original object
    List<PlayList> playLists;

    public PlayListIO(Context context){
        this.context = context;
        //initialize variables
        reader = context.getSharedPreferences("JSPlayer.playlists", Context.MODE_PRIVATE);
        editor = reader.edit();
        t = new TypeToken<PlayList>() {}.getType();
        s = playListSerializer();
        d = playListDeserializer();
    }

    public List<PlayList> fetch(){
        playLists = new ArrayList<>();
        //read playlists
        Map<String,?> data = reader.getAll();

        //deserialize playlists and save as object
        PlayList list;
        t = new TypeToken<PlayList>() {}.getType();
        for(Map.Entry<String,?> e : data.entrySet()){
            try {
                list = d.fromJson((String) e.getValue(), t);
                playLists.add(list);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return playLists;
    }

    public PlayList getPlayList(String key){
        for(PlayList pl : playLists){
            if(pl.getName().equals(key))
                return pl;
        }
        return null;
    }

    public String getRaw(){
        Map<String,?> data = reader.getAll();
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for(Map.Entry<String,?> e : data.entrySet()){
            builder.append('\"');
            builder.append(e.getKey());
            builder.append("\" : ");
            builder.append(e.getValue());
            builder.append(", ");
        }
        builder.delete(builder.length()-2, builder.length()-1);
        builder.append('}');
        return builder.toString();
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
        PlayList playList = new PlayList(name);
        this.playLists.add(playList);
        write(playList);
        return playList;
    }

    public void write(PlayList playList){
        System.out.println(s.toJson(playList, t));
        editor.putString(playList.getName(), s.toJson(playList, t));
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
