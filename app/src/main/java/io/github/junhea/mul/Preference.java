package io.github.junhea.mul;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import io.github.junhea.mul.model.Path;

public class Preference {
    public static int pointColor;
    public static float libraryOpacity;
    public static List<Path> watchList;
    static SharedPreferences p;
    public static void reload(Context context){
        Gson g = new Gson();
        p = PreferenceManager.getDefaultSharedPreferences(context);
        pointColor = p.getInt("setting_point_color", ContextCompat.getColor(context, R.color.point));
        libraryOpacity = (float)(100-p.getInt("setting_library_opacity", 20))/(float)100;
        watchList = g.fromJson(p.getString("setting_watch_list", "[]"), new TypeToken<List<Path>>(){}.getType());
    }

    public static void saveWatchList(){
        SharedPreferences.Editor e = p.edit();
        e.putString("setting_watch_list", new Gson().toJson(watchList));
        e.commit();
    }
}
