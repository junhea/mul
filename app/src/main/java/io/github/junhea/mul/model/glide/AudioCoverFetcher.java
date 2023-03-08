package io.github.junhea.mul.model.glide;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class AudioCoverFetcher implements DataFetcher<InputStream> {

    private final AudioCoverModel model;
    private final Context context;

    AudioCoverFetcher(Context context, AudioCoverModel model) {
        this.model = model;
        this.context = context;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, model.mediaPath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (null != picture) {
                callback.onDataReady(new ByteArrayInputStream(picture));
            }else{
                callback.onDataReady(null);
            }
            retriever.release();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {
        // cannot cancel
    }


    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

}
