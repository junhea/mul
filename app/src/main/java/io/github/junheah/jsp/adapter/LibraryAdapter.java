package io.github.junheah.jsp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
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
import io.github.junheah.jsp.model.Library;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.viewHolder.LibraryViewHolder;
import io.github.junheah.jsp.ui.NowPlayingIcon;


public class LibraryAdapter extends PlayListAdapter {

    public LibraryAdapter(Context context, PlayList playList) {
        super(context, playList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.library_item, parent, false);
        return new LibraryViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song item = playList.get(position);
        ((LibraryViewHolder)holder).name.setText(item.getName());
        ((LibraryViewHolder)holder).name.setSelected(true); // for marquee
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


        //load cover
        if (item instanceof ExternalSong) {
            String url = ((ExternalSong) item).getCoverUrl();
            if (url != null && url.length() > 0)
                Glide.with(context)
                        .load(url)
                        .into(((LibraryViewHolder) holder).cover);
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
                        .into(((LibraryViewHolder) holder).cover);
            }else{
                ((LibraryViewHolder) holder).cover.setImageDrawable(null);
            }
        }
    }
}
