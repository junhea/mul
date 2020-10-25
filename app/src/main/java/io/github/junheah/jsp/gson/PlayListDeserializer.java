package io.github.junheah.jsp.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.List;

import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.Song;

public class PlayListDeserializer implements JsonDeserializer<PlayList> {

    @Override
    public PlayList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        PlayList playList = new PlayList(obj.get("name").toString());
        JsonArray data = obj.getAsJsonArray("data");
        List<Song> songs = context.deserialize(data, new TypeToken<List<Song>>() {}.getType());
        for(Song s : songs){
            playList.add(s);
        }
        return playList;
    }
}
