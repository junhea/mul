package io.github.junhea.mul.model.viewHolder;

import static io.github.junhea.mul.Preference.libraryOpacity;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.yayandroid.parallaxrecyclerview.ParallaxImageView;
import com.yayandroid.parallaxrecyclerview.ParallaxViewHolder;

import io.github.junhea.mul.R;

public class LibraryViewHolder extends ParallaxViewHolder {
    public CheckBox checkBox;
    //public ImageView handle;
    public ParallaxImageView cover;
    public ConstraintLayout layout;
    public TextView name, artist;
    public ImageView external;
    public ImageView playing;
    public View overlay;

    public LibraryViewHolder(@NonNull View itemView) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.item_check);
        layout = itemView.findViewById(R.id.item_layout);
        name = itemView.findViewById(R.id.item_name);
        artist = itemView.findViewById(R.id.item_artist);
        external = itemView.findViewById(R.id.item_external);
        cover = itemView.findViewById(R.id.item_cover);
        //handle = itemView.findViewById(R.id.item_handle);
        playing = itemView.findViewById(R.id.item_playing);
        overlay = itemView.findViewById(R.id.item_cover_overlay);
        overlay.setAlpha(libraryOpacity);
    }

    @Override
    public int getParallaxImageId() {
        return R.id.item_cover;
    }
}
