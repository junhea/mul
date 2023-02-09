package io.github.junhea.mul.model.viewHolder;


import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junhea.mul.R;

public class PlayListViewHolder extends RecyclerView.ViewHolder{
    public ConstraintLayout layout;
    public TextView name, artist;
    public ImageView cover;
    public CheckBox checkBox;
    public ImageView handle;
    public ImageView playing;

    public PlayListViewHolder(@NonNull View itemView) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.item_check);
        layout = itemView.findViewById(R.id.item_layout);
        name = itemView.findViewById(R.id.item_name);
        artist = itemView.findViewById(R.id.item_artist);
        cover = itemView.findViewById(R.id.item_cover);
        handle = itemView.findViewById(R.id.item_handle);
        playing = itemView.findViewById(R.id.item_playing);
    }
}
