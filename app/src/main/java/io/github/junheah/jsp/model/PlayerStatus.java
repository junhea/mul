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

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    long time;
    boolean playing;
}
