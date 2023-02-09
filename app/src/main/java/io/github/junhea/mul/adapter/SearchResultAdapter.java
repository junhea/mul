package io.github.junhea.mul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.R;
import io.github.junhea.mul.interfaces.SearchResultInterface;
import io.github.junhea.mul.model.glide.AudioCoverModel;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.ExternalSongContainer;
import io.github.junhea.mul.model.song.Song;
import io.github.junhea.mul.model.viewHolder.ButtonViewHolder;
import io.github.junhea.mul.model.viewHolder.PlayListViewHolder;

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
    boolean selectMode = false;
    short[] checked;

    final static short CHECK = 1;
    final static short NONE = 0;
    final static short INVALID = -1;


    public SearchResultAdapter(Context context, List data) {
        this.data = new ArrayList<>();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data.addAll(data);
        history = new ArrayList<>();
        setHasStableIds(true);
        this.checked = new short[this.data.size()];
        for(int i=0; i<this.checked.length; i++) {
            if(this.data.get(i) instanceof ExternalSongContainer) this.checked[i] = INVALID;
            else this.checked[i] = NONE;
        }
    }

    public void lockui(boolean lock) {
        this.lock = lock;
    }

    public void setSelectMode(boolean mode, Song song){
        this.selectMode = mode;
        for (int i=0; i<data.size(); i++) {
            Object o = data.get(i);
            if(o instanceof ExternalSongContainer){
                checked[i] = INVALID;
            }else if (o instanceof Song) {
                //is longclicked song
                if (song != null && o == song) checked[i] = CHECK;
                else checked[i] = NONE;
                notifyItemChanged(i);
            }
        }
    }

    public boolean getSelectMode(){
        return this.selectMode;
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
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder instanceof PlayListViewHolder)
            Glide.with(((PlayListViewHolder) holder).cover).clear(((PlayListViewHolder) holder).cover);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PlayListViewHolder) {
            Song song = (Song)data.get(holder.getAbsoluteAdapterPosition());
            PlayListViewHolder v = (PlayListViewHolder) holder;
            if (song instanceof ExternalSongContainer) {
                v.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!lock && !selectMode){
                            if(listener != null) listener.clickedSongContainer((ExternalSongContainer) song);
                        }
                    }
                });
                v.layout.setOnLongClickListener(null);
                v.checkBox.setVisibility(View.GONE);
            } else {
                v.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!lock) {
                            if(selectMode){
                                checked[holder.getAbsoluteAdapterPosition()] = checked[holder.getAbsoluteAdapterPosition()] == NONE ? CHECK : NONE;
                                v.checkBox.performClick();
                            }else if(listener != null) listener.clickedSong(song);
                        }
                    }
                });
                v.layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(!lock){
                            if(listener != null) listener.longClickedSong(song);
                        }
                        return true;
                    }
                });
                v.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        checked[holder.getAbsoluteAdapterPosition()] = b ? CHECK : NONE;
                    }
                });
                v.checkBox.setVisibility(selectMode ? View.VISIBLE : View.GONE);
            }

            // ui
            v.name.setText(song.getName());
            v.artist.setText(song.getArtist());
            v.checkBox.setChecked(checked[holder.getAbsoluteAdapterPosition()] == CHECK);

            if (song instanceof ExternalSong) {
                String url = ((ExternalSong)song).getCoverUrl();
                if (url != null && url.length() > 0)
                    Glide.with(((PlayListViewHolder) holder).cover)
                            .load(url)
                            .placeholder(R.drawable.music_dark)
                            .fallback(R.drawable.music_dark)
                            .into(((PlayListViewHolder) holder).cover);
            } else {
                Glide.with(((PlayListViewHolder) holder).cover)
                        .load(new AudioCoverModel(song.getPath()))
                        .dontTransform()
                        .placeholder(R.drawable.music_dark)
                        .fallback(R.drawable.music_dark)
                        .into(((PlayListViewHolder)holder).cover);
            }

        }else if(holder instanceof ButtonViewHolder){
            ButtonItem item = (ButtonItem) data.get(holder.getAbsoluteAdapterPosition());
            ButtonViewHolder v = (ButtonViewHolder) holder;
            v.text.setVisibility(item.loading ? View.GONE : View.VISIBLE);
            v.progressBar.setVisibility(item.loading ? View.VISIBLE : View.GONE);
            v.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!lock) {
                        if(listener != null) listener.clickedLoadMore();
                        item.loading = true;
                        notifyItemChanged(holder.getAbsoluteAdapterPosition());
                    }
                }
            });
        }
    }

    public void toggleSelectAll(){
        //check if there are any selected items
        boolean flag = false;
        for(int i=0; i<data.size(); i++){
            Object o = data.get(i);
            if(o instanceof Song){
                if(checked[i] == CHECK){
                    flag = true;
                    break;
                }
            }
        }

        for(int i = 0; i<data.size(); i++){
            Object o = data.get(i);
            if(o instanceof Song && !(o instanceof ExternalSongContainer)) {
                if(checked[i] == (flag ? CHECK : NONE)) {
                    checked[i] = (flag ? NONE : CHECK);
                    notifyItemChanged(i);
                }
            }
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
        checked = new short[songs.size()];
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

    public List<Song> getSelected(){
        List<Song> res = new ArrayList<>();
        for(int i=0; i<data.size(); i++){
            Object o = data.get(i);
            if(o instanceof Song && !(o instanceof ExternalSongContainer)){
                if(checked[i] == CHECK){
                    res.add((Song) o);
                }
            }
        }
        return res;
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
        return data.get(position).hashCode();
    }

    public class ButtonItem{
        public boolean loading = false;

        @Override
        public int hashCode() {
            return loading? -1: -2;
        }
    };
}
