package io.github.junhea.mul;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.Song;

import static io.github.junhea.mul.Utils.getPlayList;
import static io.github.junhea.mul.model.song.Song.EXTERNAL;
import static io.github.junhea.mul.model.song.Song.LOCAL;

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
        reader = context.getSharedPreferences("mul.playlists", Context.MODE_PRIVATE);
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


    public String getRaw(){
        Map<String, List<long[]>> data = new HashMap<>();
        for(String k : keys){
            data.put(k, getids(k));
        }
        return g.toJson(data);
    }

    public Map<String, List<long[]>> getRawObject(){
        Map<String, List<long[]>> data = new HashMap<>();
        for(String k : keys){
            data.put(k, getids(k));
        }
        return data;
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

    public void writeRawObj(Map<String, List<long[]>> data){
        editor.clear();
        for(String k : data.keySet()){
            editor.putString(k, g.toJson(data.get(k)));
        }
        editor.commit();
    }

    public PlayList create(String name){
        keys.add(name);
        PlayList playList = new PlayList(context, name);
        write(playList);
        return playList;
    }

    public boolean addSongs(String playList, List<long[]> items){
        boolean success = true;
        List<long[]> ids = getids(playList);
        for(long[] id : items){
            if(success)
                for(long[] i : ids){
                    if(Arrays.equals(i, id)) {
                        success = false;
                        break;
                    }
                }
            ids.add(id);
        }
        editor.putString(playList, g.toJson(ids));
        editor.commit();
        return success;
    }

    public void write(PlayList playList){
        String name = playList.getName();
        PlayList tmp = (PlayList) playList.clone();
        List<long[]> ids = new ArrayList<>();
        for(Song s : tmp){
            if(s instanceof ExternalSong){
                ids.add(new long[]{EXTERNAL, s.getSid()});
            }else{
                ids.add(new long[]{LOCAL, s.getSid()});
            }
        }
        editor.putString(name, g.toJson(ids));
        editor.commit();
    }

    public void writeIds(String name, List<long[]> ids){
        editor.putString(name, g.toJson(ids));
        editor.commit();
    }

    public List<String> getNames(){
        return keys;
    }

    public void rename(PlayList playList, String newName){
        List<long[]> ids = getids(playList.getName());
        editor.remove(playList.getName());
        editor.commit();
        playList.setName(newName);
        writeIds(newName, ids);
    }

    public void delete(PlayList playList){
        delete(playList.getName());
    }

    public void delete(String name){
        PlayList pl = getPlayList(name);
        if(pl != null){
            //notify playlist removed
            pl.playListRemoved();
        }
        editor.remove(name);
        editor.commit();
    }
}
