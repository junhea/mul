package io.github.junhea.mul.activity;

import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_FOLDER;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.junhea.mul.Preference;
import io.github.junhea.mul.R;
import io.github.junhea.mul.model.Path;

public class WatchListManagerActivity extends PlayerBaseActivity{

    RecyclerView recycler;
    CheckBoxAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_source_manager);
        panelOnCreate();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = this.findViewById(R.id.source_recycler);
        adapter = new CheckBoxAdapter(Preference.watchList);

        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_watchlist_add:
                Intent intent = new Intent(this, FileChooserActivity.class);
                intent.putExtra("mode", REQUEST_SELECT_FOLDER);
                startActivityForResult(intent, REQUEST_SELECT_FOLDER);
                return true;
            case R.id.menu_watchlist_delete:
                adapter.removeChecked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.watchlist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_SELECT_FOLDER) {
                String path = data.getStringExtra("path");
                boolean recursive = data.getBooleanExtra("recursive", false);
                addToWatchList(path,recursive);
            }
        }

    }

    public boolean addToWatchList(String spath, boolean recursive){
        Path path = new Path(spath, recursive);
        if(Preference.watchList.indexOf(path) == -1){
            //update pref
            Preference.watchList.add(path);
            Preference.saveWatchList();
            adapter.notifyDataSetChanged();
            adapter.resetCheck();
            return true;
        }
        return false;
    }

    public class CheckBoxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<Path> list;
        boolean[] checked;
        public CheckBoxAdapter(List<Path> list) {
            this.list = list;
            resetCheck();
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).hashCode();
        }

        public void resetCheck(){
            checked = new boolean[list.size()];
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_watch_list, null);
            return new WatchListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((WatchListViewHolder) holder).text.setText(list.get(position).toString());
            ((WatchListViewHolder) holder).checkBox.setChecked(checked[position]);
            ((WatchListViewHolder) holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    checked[holder.getAbsoluteAdapterPosition()] = b;
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void removeChecked(){
            for(int i=checked.length-1; i>-1; i--){
                if(checked[i]){
                    list.remove(i);
                    notifyItemRemoved(i);
                }
            }
            resetCheck();
            Preference.saveWatchList();
        }

        private class WatchListViewHolder extends RecyclerView.ViewHolder{
            TextView text;
            CheckBox checkBox;
            public WatchListViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.watch_text);
                checkBox = itemView.findViewById(R.id.watch_checkbox);
            }
        }
    };
}
