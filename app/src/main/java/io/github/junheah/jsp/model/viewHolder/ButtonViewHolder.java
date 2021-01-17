package io.github.junheah.jsp.model.viewHolder;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junheah.jsp.R;

public class ButtonViewHolder extends RecyclerView.ViewHolder {
    public TextView text;
    public ConstraintLayout layout;
    public ProgressBar progressBar;


    public ButtonViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.item_layout);
        text = itemView.findViewById(R.id.item_text);
        progressBar = itemView.findViewById(R.id.item_progress);
    }
}
