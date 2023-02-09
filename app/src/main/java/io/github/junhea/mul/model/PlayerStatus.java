package io.github.junhea.mul.model;

import io.github.junhea.mul.model.song.Song;

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
    public static boolean forceUpdate;
    public static void reset(){
        duration = 0;
        current = 0;
        playing = false;
        loaded = false;
        song = null;
        playList = null;
        forceUpdate = false;
    }
}
