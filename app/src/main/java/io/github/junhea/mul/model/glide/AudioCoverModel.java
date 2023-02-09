package io.github.junhea.mul.model.glide;

public class AudioCoverModel {

    public String mediaPath;

    public AudioCoverModel(String path) {
        this.mediaPath = path;
    }

    @Override
    public int hashCode() {
        return Math.abs((mediaPath.getBytes().length + mediaPath.hashCode()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        AudioCoverModel compare = (AudioCoverModel) obj;

        try {
            return (compare.mediaPath.equals(this.mediaPath) && compare.mediaPath.getBytes().length == this.mediaPath.getBytes().length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }




}
