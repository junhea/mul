package io.github.junheah.jsp.model.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junheah.jsp.R;

public class PlayListViewHolder extends RecyclerView.ViewHolder{
    public ConstraintLayout layout;
    public TextView name, artist;
    public ImageView cover;

    public PlayListViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.item_layout);
        name = itemView.findViewById(R.id.item_name);
        artist = itemView.findViewById(R.id.item_artist);
        cover = itemView.findViewById(R.id.item_cover);
    }
}
