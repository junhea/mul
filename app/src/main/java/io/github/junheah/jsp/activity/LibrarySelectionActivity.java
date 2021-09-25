package io.github.junheah.jsp.activity;

import static io.github.junheah.jsp.MainApplication.library;
import static io.github.junheah.jsp.model.song.Song.EXTERNAL;
import static io.github.junheah.jsp.model.song.Song.LOCAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.SearchResultAdapter;
import io.github.junheah.jsp.interfaces.SearchResultInterface;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

public class LibrarySelectionActivity extends PlayerBaseActivity {

    RecyclerView recycler;
    List<Song> data = new ArrayList<>();
    SearchResultAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // returns sid list as result
        // actual add happens in mainactivity
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_library_selection);
        panelOnCreate();

        recycler = findViewById(R.id.recycler);

        adapter = new SearchResultAdapter(context, library);
        adapter.setSelectMode(true, null);

        adapter.setListener(new SearchResultInterface() {
            @Override
            public void clickedSong(Song song) {
                //none
            }

            @Override
            public void clickedSongContainer(ExternalSongContainer container) {
                //none
            }

            @Override
            public void clickedLoadMore() {
                //none
            }

            @Override
            public void longClickedSong(Song song) {
                //none
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library_selection_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.library_add:
                sendResult(adapter.getSelected());
                break;
            case R.id.library_select_all:
                adapter.toggleSelectAll();
                break;
        }
        return true;
    }

    public void sendResult(List<Song> res){
        List<long[]> d = new ArrayList<>();
        for(Song s : res){
            d.add(new long[]{s instanceof LocalSong ? LOCAL : EXTERNAL, s.getSid()});
        }
        Intent data = new Intent();
        data.putExtra("data", new Gson().toJson(d));
        setResult(RESULT_OK, data);
        finish();
    }
}