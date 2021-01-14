package io.github.junheah.jsp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.SearchResultInterface;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.source.Search;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //used in search result created from external source

    Context context;
    LayoutInflater inflater;
    List<ExternalSong> data;
    SearchResultInterface listener;

    public SearchResultAdapter(Context context, List<ExternalSong> data) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public void setListener(SearchResultInterface listener){this.listener = listener;}

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.playlist_item, parent, false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExternalSong song = (ExternalSong)data.get(position);
        PlayListViewHolder v = (PlayListViewHolder) holder;
        if(song instanceof ExternalSongContainer){
            v.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.clickedSongContainer((ExternalSongContainer) song);
                }
            });
        }else{
            v.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.clickedSong((ExternalSong) song);
                }
            });
        }

        // ui
        v.name.setText(song.getName());
        v.artist.setText(song.getArtist());
        String cover = song.getCoverUrl();
        if (cover != null && cover.length() > 0)
            Glide.with(context)
                    .load(cover)
                    .into(v.cover);
        else v.cover.setImageResource(R.drawable.music_dark);
    }


    public void clear(){
        int size = this.data.size();
        this.data.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<ExternalSong> songs){
        int size = this.data.size();
        this.data.addAll(songs);
        notifyItemRangeInserted(size, songs.size());
    }
    @Override
    public int getItemCount() {
        return data.size();
    }
}
