package io.github.junheah.jsp.model.source;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Entities;
import org.w3c.dom.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.junheah.jsp.model.song.ExternalSong;

public class JsUtil {
    public static List<ExternalSong> newList(){
        return new ArrayList<ExternalSong>();
    }
    public static Map<String, String> newMap(){
        return new HashMap<String, String>();
    }
    public static String escapeHtml(String input) {return StringEscapeUtils.unescapeHtml4(input);}
}
