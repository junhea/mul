package io.github.junheah.jsp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

import java.util.Map;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.viewHolder.LibraryViewHolder;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;


public class LibraryAdapter extends PlayListAdapter {

    boolean showCover = false;


    public LibraryAdapter(Context context, PlayList playList) {
        super(context, playList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.library_item, parent, false);
        return new LibraryViewHolder(view);
    }

    public void setShowCover(boolean s){
        this.showCover = s;
        for(int i=0; i<playList.size(); i++){
            notifyItemChanged(i);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        ((LibraryViewHolder) holder).playing.setVisibility(View.GONE);
        ((LibraryViewHolder) holder).cover.reuse();
        ((LibraryViewHolder) holder).cover.setImageDrawable(null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song item = playList.get(position);
        ((LibraryViewHolder)holder).name.setText(item.getName());
        if(item.getArtist() != null && item.getArtist().length()>0) {
            ((LibraryViewHolder) holder).artist.setVisibility(View.VISIBLE);
            ((LibraryViewHolder) holder).artist.setText(item.getArtist());
        }else{
            ((LibraryViewHolder) holder).artist.setVisibility(View.GONE);
        }

        ((LibraryViewHolder) holder).external.setVisibility(item instanceof ExternalSong ? View.VISIBLE : View.GONE);


        ((LibraryViewHolder)holder).layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //on click listener
                if(callback != null){
                    callback.SongClicked(item, playList);
                }
            }
        });
        ((LibraryViewHolder)holder).layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(callback != null){
                    callback.SongLongClicked(item, playList);
                }
                return false;
            }
        });

        //nowplaying
        if(current != null && item.equals(current)){
            ((LibraryViewHolder) holder).playing.setVisibility(View.VISIBLE);
        }else {
            ((LibraryViewHolder) holder).playing.setVisibility(View.GONE);
        }

        if(showCover) {
            //load cover
            boolean hascover = false;
            if (item instanceof ExternalSong) {
                String url = ((ExternalSong) item).getCoverUrl();
                if (url != null && url.length() > 0) {
                    Glide.with(context)
                            .load(url)
                            .into(((LibraryViewHolder) holder).cover);
                    hascover = true;
                }
            } else {
                if (!((LocalSong) item).nocover) {
                    hascover = true;
                    Glide.with(context)
                            .load(new AudioCoverModel(item.getPath()))
                            .dontTransform()
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
                            .into(((LibraryViewHolder) holder).cover);
                }
            }
            if (hascover) {
                ((LibraryViewHolder) holder).cover.setVisibility(View.VISIBLE);
            } else ((LibraryViewHolder) holder).cover.setVisibility(View.GONE);
        }else ((LibraryViewHolder) holder).cover.setVisibility(View.GONE);
    }
}
