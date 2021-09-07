package io.github.junheah.jsp.model.viewHolder;

import android.content.Context;
import android.text.Layout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.ui.NowPlayingIcon;

public class LibraryViewHolder extends RecyclerView.ViewHolder {
    public CheckBox checkBox;
    //public ImageView handle;
    public ImageView cover;
    public ConstraintLayout layout;
    public TextView name, artist;
    public ImageView external;

    public LibraryViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.item_check);
        layout = itemView.findViewById(R.id.item_layout);
        name = itemView.findViewById(R.id.item_name);
        artist = itemView.findViewById(R.id.item_artist);
        external = itemView.findViewById(R.id.item_external);
        cover = itemView.findViewById(R.id.item_cover);
        //handle = itemView.findViewById(R.id.item_handle);
        //playing = itemView.findViewById(R.id.item_playing);
        //playing.setImageDrawable(NowPlayingIcon.getInstance(context));
    }
}
