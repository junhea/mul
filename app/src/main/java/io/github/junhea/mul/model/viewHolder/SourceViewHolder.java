package io.github.junhea.mul.model.viewHolder;


import static io.github.junhea.mul.Preference.pointColor;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junhea.mul.R;

public class SourceViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public ImageButton download;
    public ImageButton delete;
    public ProgressBar progress;
    public SourceViewHolder(@NonNull View itemView) {
        super(itemView);
        this.name = itemView.findViewById(R.id.source_name);
        this.download = itemView.findViewById(R.id.source_download);
        this.delete = itemView.findViewById(R.id.source_delete);
        this.progress = itemView.findViewById(R.id.source_progress);
        this.progress.getIndeterminateDrawable().setColorFilter(pointColor, android.graphics.PorterDuff.Mode.SRC_IN);
    }
}
