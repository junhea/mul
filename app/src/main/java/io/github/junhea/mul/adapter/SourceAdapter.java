package io.github.junhea.mul.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.R;
import io.github.junhea.mul.SourceIO;
import io.github.junhea.mul.interfaces.SourceOnClickCallback;
import io.github.junhea.mul.model.source.Source;
import io.github.junhea.mul.model.viewHolder.HeaderViewHolder;
import io.github.junhea.mul.model.viewHolder.SourceItem;
import io.github.junhea.mul.model.viewHolder.SourceViewHolder;

public class SourceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    LayoutInflater inflater;
    List<Object> items;
    SourceOnClickCallback callback;

    public static final int HEADER = 0;
    public static final int AVAILABLE = 1;
    public static final int INSTALLED = 2;
    public static final int INSTALLING = 3;

    public SourceAdapter(Context context, SourceOnClickCallback callback) {
        this.callback = callback;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>();
        //add local sources first
        items.add(context.getString(R.string.source_installed));
        SourceIO sourceIO = SourceIO.getInstance(context);
        sourceIO.load();
        for(Source s : sourceIO.getSources()){
            items.add(new SourceItem(s.getName(), INSTALLED));
        }
        items.add(context.getString(R.string.source_available));
    }

    public void addAvailable(SourceItem item){
        //list of available sources
        for(int i=0; i<items.size(); i++){
            if(items.get(i) instanceof SourceItem) {
                if (((SourceItem)items.get(i)).equals(item)) {
                    ((SourceItem)items.get(i)).url = item.url;
                    return;
                }
            }
        }
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == HEADER){
            return new HeaderViewHolder(inflater.inflate(R.layout.header_item, parent, false));
        }else{
            return new SourceViewHolder(inflater.inflate(R.layout.source_item, parent, false));
        }
    }

    public void notifyItemChanged(SourceItem item){
        for(int i=0; i<items.size(); i++){
            if(items.get(i) instanceof SourceItem) {
                if (((SourceItem)items.get(i)).equals(item)) {
                    notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object o = items.get(position);
        if(o instanceof String){
            return HEADER;
        }else if(o instanceof SourceItem){
            return ((SourceItem)o).status;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int p) {
        int position = holder.getAbsoluteAdapterPosition();
        int type = getItemViewType(position);
        Object item = items.get(position);
        if(type == HEADER){
            ((HeaderViewHolder)holder).text.setText((String)item);
        }else{
            //type == SOURCE
            ((SourceViewHolder)holder).name.setText(((SourceItem)item).name);
            ((SourceViewHolder)holder).download.setVisibility(type == AVAILABLE ? View.VISIBLE : View.GONE);
            ((SourceViewHolder)holder).delete.setVisibility(type == INSTALLED ? View.VISIBLE : View.GONE);
            ((SourceViewHolder)holder).progress.setVisibility(type == INSTALLING ? View.VISIBLE : View.GONE);

            ((SourceViewHolder)holder).download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SourceItem)item).status = INSTALLING;
                    notifyItemChanged(position);
                    callback.download((SourceItem)item);
                }
            });

            ((SourceViewHolder)holder).delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.delete((SourceItem)item);
                }
            });
        }
    }
}
