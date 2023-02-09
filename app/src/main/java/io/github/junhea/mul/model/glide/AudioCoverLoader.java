package io.github.junhea.mul.model.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

class AudioCoverLoader implements ModelLoader<AudioCoverModel, InputStream> {

    @NonNull
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull AudioCoverModel AudioCoverModel, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(AudioCoverModel), new AudioCoverFetcher(AudioCoverModel));
    }

    @Override
    public boolean handles(@NonNull AudioCoverModel AudioCoverModel) {
        return true;
    }

    static class Factory implements ModelLoaderFactory<AudioCoverModel, InputStream> {
        @NonNull
        @Override
        public ModelLoader<AudioCoverModel, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new AudioCoverLoader();
        }

        @Override
        public void teardown() {

        }
    }

}
