package io.github.junhea.mul.model.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junhea.mul.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView text;
    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        this.text = itemView.findViewById(R.id.header_text);
    }
}
