package io.github.junheah.jsp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.AdapterNotifier;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

public class PlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    PlayList playList;
    Context context;
    LayoutInflater inflater;
    PlayListItemClickCallback callback;

    public PlayListAdapter(Context context, PlayList playList){
        this.playList = playList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        playList.setNotifier(new AdapterNotifier() {
            @Override
            public void itemRemoved(int index) {
                notifyItemRemoved(index);
            }

            @Override
            public void itemAdded(int index) {
                notifyItemInserted(index);
            }

            @Override
            public void itemUpdated(int index) {
                notifyItemChanged(index);
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.playlist_item, parent, false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song item = playList.get(position);
        ((PlayListViewHolder)holder).name.setText(item.getName());
        ((PlayListViewHolder)holder).artist.setText(item.getArtist());

        String coverImage = item.getCover();
        if(coverImage == null){
            if (item instanceof LocalSong) {
                Bitmap coverBitmap = ((LocalSong)item).getCoverBitmap();
                if(coverBitmap == null)
                    ((PlayListViewHolder)holder).cover.setImageResource(R.drawable.music_dark);
                else
                    ((PlayListViewHolder) holder).cover.setImageBitmap(coverBitmap);
            } else ((PlayListViewHolder)holder).cover.setImageResource(R.drawable.music_dark);
        }else{
            //load external image
            Glide.with(context)
                    .load(coverImage)
                    .into(((PlayListViewHolder) holder).cover);
        }

        ((PlayListViewHolder)holder).layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //on click listener
                if(callback != null){
                    callback.SongClicked(item, playList);
                }
            }
        });
        ((PlayListViewHolder)holder).layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(callback != null){
                    callback.SongLongClicked(item, playList);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    public void setCallback(PlayListItemClickCallback callback){
        this.callback = callback;
    }

    class PlayListViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout layout;
        TextView name, artist;
        ImageView cover;

        public PlayListViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.item_layout);
            name = itemView.findViewById(R.id.item_name);
            artist = itemView.findViewById(R.id.item_artist);
            cover = itemView.findViewById(R.id.item_cover);
        }
    }


}
