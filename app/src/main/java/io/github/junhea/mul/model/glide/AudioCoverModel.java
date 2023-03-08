package io.github.junhea.mul.model.glide;

import android.content.Context;
import android.net.Uri;

public class AudioCoverModel {

    public Uri mediaPath;

    public AudioCoverModel(Uri path) {
        this.mediaPath = path;
    }

    @Override
    public int hashCode() {
        return Math.abs(mediaPath.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        AudioCoverModel compare = (AudioCoverModel) obj;

        try {
            return compare.mediaPath.equals(this.mediaPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }




}
