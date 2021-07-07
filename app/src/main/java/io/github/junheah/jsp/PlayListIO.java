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

    List<String> keys;

    public PlayListIO(Context context){
        this.context = context;
        //initialize variables
        reader = context.getSharedPreferences("JSPlayer.playlists", Context.MODE_PRIVATE);
        editor = reader.edit();
        //only get keys & load on user request (dont save playlist object!!!)
        keys = new ArrayList<>();
        keys.addAll(reader.getAll().keySet());
        t = new TypeToken<PlayList>() {}.getType();
        s = playListSerializer();
        d = playListDeserializer();
    }

    public PlayList get(String key){
        if(keys.indexOf(key) > -1){
            t = new TypeToken<PlayList>() {}.getType();
            return d.fromJson((String) reader.getString(key, "[]"), t);
        }
        //not found
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
        keys.add(name);
        PlayList playList = new PlayList(name);
        write(playList);
        return playList;
    }

    public void write(PlayList playList){
        editor.putString(playList.getName(), s.toJson(playList, t));
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
