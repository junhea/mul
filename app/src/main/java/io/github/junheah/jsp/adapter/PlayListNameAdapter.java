package io.github.junheah.jsp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;

public class PlayListNameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    List<String> keys;
    Context context;
    LayoutInflater inflater;
    PlayListSelectListener callback;
    PlayListIO playListIO;

    public PlayListNameAdapter(Context context, PlayListSelectListener callback){
        this.callback = callback;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.playListIO = PlayListIO.getInstance(context);
        keys = playListIO.getNames();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = inflater.inflate(R.layout.playlist_name_item, parent, false);
        return new PlayListNameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String k = keys.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null)
                    callback.itemClick(k, ((PlayListNameViewHolder)holder).name);
            }
        });
        ((PlayListNameViewHolder)holder).name.setText(k);
        ViewCompat.setTransitionName(((PlayListNameViewHolder)holder).name,"playlist_title:"+position);
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    public interface PlayListSelectListener{
        void itemClick(String key, TextView sharedElement);
    }

    public void added(String key){
        notifyItemInserted(keys.size()-1);
    }

    public class PlayListNameViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        public PlayListNameViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.subtitle);
        }
    }
}
