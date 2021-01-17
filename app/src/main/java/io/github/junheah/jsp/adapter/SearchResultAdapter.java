package io.github.junheah.jsp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import io.github.junheah.jsp.model.viewHolder.ButtonViewHolder;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;

import static io.github.junheah.jsp.MainApplication.defaultCover;
import static io.github.junheah.jsp.Utils.lockuiRecursive;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //used in search result created from external source

    Context context;
    LayoutInflater inflater;
    List<Object> data;
    List<List<Object>> history;
    SearchResultInterface listener;
    final static int SONG = 0;
    final static int BUTTON = 1;
    boolean lock = false;

    public SearchResultAdapter(Context context, List data) {
        this.data = new ArrayList<>();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data.addAll(data);
        history = new ArrayList<>();
    }

    public void lockui(boolean lock){
        this.lock = lock;
    }

    public void setListener(SearchResultInterface listener){this.listener = listener;}

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == SONG)
            return new PlayListViewHolder(inflater.inflate(R.layout.playlist_item, parent, false));
        else
            return new ButtonViewHolder(inflater.inflate(R.layout.button_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PlayListViewHolder) {
            ExternalSong song = (ExternalSong)data.get(position);
            PlayListViewHolder v = (PlayListViewHolder) holder;
            if (song instanceof ExternalSongContainer) {
                v.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!lock)listener.clickedSongContainer((ExternalSongContainer) song);
                    }
                });
            } else {
                v.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!lock)listener.clickedSong(song);
                    }
                });
                v.layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(!lock)listener.longClickedSong(song);
                        return false;
                    }
                });
            }

            // ui
            v.name.setText(song.getName());
            v.artist.setText(song.getArtist());

            //dont load images from onbind : infinite loop
            Bitmap bitmap = song.getCover();
            if (bitmap == null) {
                String url = song.getCoverUrl();
                if (url != null && url.length() > 0)
                    Glide.with(context)
                            .load(url)
                            .into(((PlayListViewHolder) holder).cover);
            } else {
                if (bitmap == defaultCover)
                    ((PlayListViewHolder) holder).cover.setImageResource(R.drawable.music_dark);
                else
                    ((PlayListViewHolder) holder).cover.setImageBitmap(bitmap);
            }

        }else if(holder instanceof ButtonViewHolder){
            ButtonItem item = (ButtonItem) data.get(position);
            ButtonViewHolder v = (ButtonViewHolder) holder;
            v.text.setVisibility(item.loading ? View.GONE : View.VISIBLE);
            v.progressBar.setVisibility(item.loading ? View.VISIBLE : View.GONE);
            v.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!lock) {
                        listener.clickedLoadMore();
                        item.loading = true;
                        notifyItemChanged(position);
                    }
                }
            });
        }
    }

    public void reset(){
        clear();
        this.history.clear();
    }

    public void clear(){
        int size = this.data.size();
        this.data.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List songs){
        int size = this.data.size();
        if(size>0){
            if(this.data.get(data.size()-1) instanceof ButtonItem){
                this.data.remove(data.size()-1);
                notifyItemRemoved(data.size());
            }
        }
        this.data.addAll(songs);
        this.data.add(new ButtonItem());
        notifyItemRangeInserted(size, songs.size());
    }

    public void changeData(List songs){
        clear();
        addAll(songs);
    }

    public void saveInHistory(){
        data.remove(data.size()-1);
        notifyItemRemoved(data.size()-1);

        history.add(new ArrayList<>(data));
        clear();
    }

    public boolean prevData(){
        if(history.size()>0){
            changeData(history.get(history.size()-1));
            history.remove(history.size()-1);
            return hasHistory();
        }else{
            return false;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position) instanceof Song){
            return SONG;
        }else{
            return BUTTON;
        }
    }

    public boolean hasHistory(){
        return history.size()>0;
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ButtonItem{
        public boolean loading = false;
    }
}
