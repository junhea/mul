package io.github.junheah.jsp.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junheah.jsp.model.room.ExternalSongDao;
import io.github.junheah.jsp.model.room.LocalSongDao;
import io.github.junheah.jsp.model.room.SongDatabase;

public class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    SongDatabase db;
    LocalSongDao l;
    ExternalSongDao e;
    public LibraryAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
