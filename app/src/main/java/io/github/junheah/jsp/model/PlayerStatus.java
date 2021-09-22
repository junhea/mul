package io.github.junheah.jsp.model;

import java.io.Serializable;
import java.util.List;

import io.github.junheah.jsp.model.song.Song;

public class PlayerStatus {
    /*
    플레이어의 상태를 저장
     */
    public static int duration;
    public static Song song;
    public static PlayList playList;
    public static int current;
    public static boolean playing;
    public static boolean loaded;
    public static void reset(){
        duration = 0;
        current = 0;
        playing = false;
        loaded = false;
        song = null;
        playList = null;
    }
}
