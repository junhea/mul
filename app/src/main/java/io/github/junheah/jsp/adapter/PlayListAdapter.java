package io.github.junheah.jsp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.AdapterNotifier;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.ItemMoveCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;


public class PlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    PlayList playList;
    Context context;
    LayoutInflater inflater;
    PlayListItemClickCallback callback;
    boolean editMode = false;
    boolean selectMode = false;
    ItemMoveCallback.StartDragListener drag;
    Song current;

    AdapterNotifier notifier = new AdapterNotifier() {
        @Override
        public void itemRemoved(int index) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(index);
                }
            });

        }

        @Override
        public void itemAdded(int index) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(index);
                }
            });

        }

        @Override
        public void itemUpdated(int index) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(index);
                }
            });
        }
    };

    public void toggleEditMode(){
        this.editMode = !this.editMode;
        for(int i=0; i<playList.size(); i++){
            notifyItemChanged(i);
        }
    }

    public void runOnUiThread(Runnable r){
        ((Activity)context).runOnUiThread(r);
    }

    public PlayListAdapter(Context context, PlayList playList){
        this.playList = playList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        playList.setNotifier(notifier);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.playlist_item, parent, false);
        return new PlayListViewHolder(view);
    }

    public void currentChanged(Song song){
        // if song not in this playlist, song is null
        if(current != null){
            int old = playList.indexOf(current);
            current = null;
            notifyItemChanged(old);
        }
        this.current = song;
        if(current != null){
            notifyItemChanged(playList.indexOf(current));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song item = playList.get(position);
        ((PlayListViewHolder)holder).name.setText(item.getName());
        ((PlayListViewHolder)holder).name.setSelected(true); // for marquee
        ((PlayListViewHolder)holder).artist.setText(item.getArtist());
        ((PlayListViewHolder)holder).handle.setVisibility(this.editMode ? View.VISIBLE : View.GONE);

        ((PlayListViewHolder)holder).handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    drag.requestDrag(holder);
                }
                return false;
            }
        });

        if(current != null && item.equals(current)){
            ((PlayListViewHolder) holder).playing.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(R.drawable.nowplaying)
                    .into(((PlayListViewHolder) holder).playing);
        }else {
            ((PlayListViewHolder) holder).playing.setVisibility(View.GONE);
        }

        if (item instanceof ExternalSong) {
            String url = ((ExternalSong) item).getCoverUrl();
            if (url != null && url.length() > 0)
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.music_dark)
                        .fallback(R.drawable.music_dark)
                        .into(((PlayListViewHolder) holder).cover);
        } else {
            if(!((LocalSong)item).nocover) {
                Glide.with(context)
                        .load(new AudioCoverModel(item.getPath()))
                        .dontTransform()
                        .placeholder(R.drawable.music_dark)
                        .fallback(R.drawable.music_dark)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                //no cover
                                ((LocalSong) item).nocover = true;
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(((PlayListViewHolder) holder).cover);
            }else{
                //local and no cover
                Glide.with(context)
                        .load(R.drawable.music_dark)
                        .dontTransform()
                        .into(((PlayListViewHolder) holder).cover);
            }
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

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                playList.swap(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                playList.swap(i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        //playlistio
        playList.save();
    }

    public void setDragListener(ItemMoveCallback.StartDragListener listener){
        this.drag = listener;
    }

    @Override
    public long getItemId(int position) {
        return playList.get(position).hashCode();
    }
}
