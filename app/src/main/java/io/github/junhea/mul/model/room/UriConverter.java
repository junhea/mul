package io.github.junhea.mul.model.room;

import android.net.Uri;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

@ProvidedTypeConverter
public class UriConverter {
    @TypeConverter
    public Uri stringToUri(String string){
        return Uri.parse(string);
    }

    @TypeConverter
    public String uriToString(Uri uri){
        return uri.toString();
    }
}
