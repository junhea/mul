package io.github.junheah.jsp.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.Song;

public class PlayListSerializer implements JsonSerializer<PlayList> {

    @Override
    public JsonElement serialize(PlayList data, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("data", context.serialize(data, new TypeToken<List<Song>>() {}.getType()).getAsJsonArray());
        obj.addProperty("name", data.getName());
        return obj;
    }
}
