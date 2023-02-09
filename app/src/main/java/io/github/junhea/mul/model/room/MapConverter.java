package io.github.junhea.mul.model.room;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class MapConverter {
    @TypeConverter
    public static String mapToString(Map<String,String> map){
        return new Gson().toJson(map);

    }

    @TypeConverter
    public static Map<String, String> stringtoMap(String s){
        return new Gson().fromJson(s, new TypeToken<Map<String,String>>() {}.getType());
    }
}
