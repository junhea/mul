package io.github.junheah.jsp.model;

import java.io.Serializable;
import java.util.List;

public class PlayerStatus {
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Song getCurrent() {
        return current;
    }

    public void setCurrent(Song current) {
        this.current = current;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public PlayList getPlayList() {
        return playList;
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    long time;
    Song current;
    boolean playing;
    PlayList playList;
}
