package io.github.junheah.jsp.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.AdapterNotifier;
import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;

import static io.github.junheah.jsp.MainApplication.defaultCover;

public class PlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    PlayList playList;
    Context context;
    LayoutInflater inflater;
    PlayListItemClickCallback callback;

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
            System.out.println("updated : index");

        }
    };

    public void runOnUiThread(Runnable r){
        ((Activity)context).runOnUiThread(r);
    }

    public PlayListAdapter(Context context, PlayList playList){
        this.playList = playList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        playList.setNotifier(notifier);
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

        //dont load images from onbind : infinite loop
        Bitmap bitmap = item.getCover();
        if(bitmap == null){
            if (item instanceof ExternalSong) {
                String url = ((ExternalSong)item).getCoverUrl();
                if (url != null && url.length() > 0)
                    Glide.with(context)
                            .load(url)
                            .into(((PlayListViewHolder)holder).cover);
            } else {
                ((PlayListViewHolder) holder).cover.setImageResource(R.drawable.music_dark);
            }
        }else {
            if (bitmap == defaultCover)
                ((PlayListViewHolder) holder).cover.setImageResource(R.drawable.music_dark);
            else
                ((PlayListViewHolder) holder).cover.setImageBitmap(bitmap);
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


}
