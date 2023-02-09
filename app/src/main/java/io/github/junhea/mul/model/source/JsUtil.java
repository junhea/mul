package io.github.junhea.mul.model.source;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.junhea.mul.model.song.ExternalSong;

public class JsUtil {
    public static List<ExternalSong> newList(){
        return new ArrayList<ExternalSong>();
    }
    public static Map<String, String> newMap(){
        return new HashMap<String, String>();
    }
    public static String escapeHtml(String input) {return StringEscapeUtils.unescapeHtml4(input);}
}
