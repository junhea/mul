package io.github.junheah.jsp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.activity.SourceManagerActivity;
import io.github.junheah.jsp.adapter.SearchResultAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.SearchResultInterface;
import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.source.Search;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.service.Player;

import static io.github.junheah.jsp.Utils.lockuiRecursive;
import static io.github.junheah.jsp.Utils.pickerPopup;
import static io.github.junheah.jsp.model.song.Song.EXTERNAL;

public class SearchFragment extends CustomFragment {

    Source source;
    LinearLayoutCompat container;
    List<ExternalSong> result = new ArrayList<>();
    PlayListItemClickCallback callback;
    SearchResultAdapter adapter;
    Button prevResBtn;
    PlayListIO playListIO;
    SourceIO sourceIO;
    EditText input;

    public SearchFragment(){
        // do nothing
    }

    public static SearchFragment newInstance(){
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    public void setSource(Source source){
        this.source = source;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_search, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.search_result);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        input = view.findViewById(R.id.search_input);
        prevResBtn = view.findViewById(R.id.prev_result_btn);

        List<Runnable> swipehistory = new ArrayList<>();

        prevResBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapter.prevData()){
                    prevResBtn.setVisibility(View.VISIBLE);
                }else{
                    prevResBtn.setVisibility(View.GONE);
                }
                if(swipehistory.size()>0){
                    swipehistory.remove(swipehistory.size()-1);
                }
            }
        });

        //source io
        sourceIO = SourceIO.getInstance(getContext());
        sourceIO.load();

        container = view.findViewById(R.id.search_container);
        adapter = new SearchResultAdapter(getContext(), result);
        adapter.setListener(new SearchResultInterface(){
            @Override
            public void clickedSong(ExternalSong song) {
                //fetch data
                song.fetch(getContext(), new ScriptCallback() {
                    @Override
                    public void callback(Object res) {
                        //play in player
                        PlayList pl = new PlayList(getContext(),"", true);
                        pl.add(song);
                        callback.SongClicked(song, pl);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void clickedSongContainer(ExternalSongContainer container) {
                lockui(true);
                adapter.saveInHistory();
                container.resetPage();
                container.fetch(getContext(), new ScriptCallback() {
                    @Override
                    public void callback(Object res) {
                        adapter.addAll(container.getSongs());
                        prevResBtn.setVisibility(adapter.hasHistory() ? View.VISIBLE : View.GONE);
                        lockui(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        lockui(false);
                    }
                });
                swipehistory.add(new Runnable() {
                    @Override
                    public void run() {
                        container.fetch(getContext(), new ScriptCallback() {
                            @Override
                            public void callback(Object res) {
                                adapter.addAll(container.getSongs());
                                prevResBtn.setVisibility(adapter.hasHistory() ? View.VISIBLE : View.GONE);
                                lockui(false);
                            }

                            @Override
                            public void onError(Exception e) {
                                lockui(false);
                            }
                        });
                    }
                });
            }

            @Override
            public void clickedLoadMore() {
                lockui(true);
                if(swipehistory.size()>0){
                    swipehistory.get(swipehistory.size()-1).run();
                }
            }

            @Override
            public void longClickedSong(ExternalSong song) {
                toggleSelectMode(song);
            }
        });

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(source != null && source.getName() != null) {
                    lockui(true);
                    if (adapter.getSelectMode())
                        toggleSelectMode(null);
                    adapter.reset();
                    prevResBtn.setVisibility(View.GONE);
                    Search search = source.getSearch(input.getText().toString());
                    ScriptCallback onSearch = new ScriptCallback() {
                        @Override
                        public void callback(Object res) {
                            //update ui
                            adapter.addAll(search.getResult());
                            lockui(false);
                        }

                        @Override
                        public void onError(Exception e) {
                            lockui(false);
                        }
                    };
                    search.fetch(getContext(), onSearch);
                    swipehistory.add(new Runnable() {
                        @Override
                        public void run() {
                            search.fetch(getContext(), onSearch);
                        }
                    });
                }
            }
        });
    }

    public void toggleSelectMode(ExternalSong song){
        adapter.setSelectMode(!adapter.getSelectMode(), song);
        prevResBtn.setEnabled(!adapter.getSelectMode());
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            // restore state
            new Gson().fromJson(savedInstanceState.getString("source", "{}"), new TypeToken< Source >() {
            }.getType());
        }

        //initialize source
        lockui(true);

        //playlist callback
        callback = ((MainActivity) getActivity()).getPlayListCallback();

        playListIO = PlayListIO.getInstance(getContext());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save state
        outState.putString("source", new Gson().toJson(source));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.search_add).setVisible(adapter == null ? false : adapter.getSelectMode());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.search_add:
                List<Song> res = adapter.getSelected();
                toggleSelectMode(null);
                pickerPopup(SearchFragment.this, "add to playlist", playListIO.getNames().toArray(new String[playListIO.getNames().size()]), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SongDatabase db = SongDatabase.getInstance(getContext());
                                List<long[]> ids = new ArrayList<>();
                                for(int i=0; i<res.size(); i++){
                                    Song s = res.get(i);
                                    try {
                                        long id = db.externalDao().insert((ExternalSong) s);
                                        s.setSid(id);
                                        ids.add(new long[]{EXTERNAL, id});
                                    }catch (Exception e){
                                        res.set(i, db.externalDao().findWithId(((ExternalSong)s).getId()));
                                        ids.add(new long[]{EXTERNAL, res.get(i).getSid()});
                                    }
                                }
                                //ui thread
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        PlayList playList = null;
                                        //add song to playlist
                                        Player player = MainActivity.getPlayer();
                                        if(player != null && player.getPlayList() != null && player.getPlayList().getName().equals(data)){
                                            //from player
                                            playList = player.getPlayList();
                                        }else if(DetailFragment.getCurrentPlayList() != null && DetailFragment.getCurrentPlayList().getName().equals(data)){
                                            //from playlist fragment
                                            playList = DetailFragment.getCurrentPlayList();
                                        }
                                        if(playList == null){
                                            //don't need load : just add via playListio
                                            playListIO.addSongs(data, ids);
                                        }else {
                                            for(Song s : res){
                                                playList.add(s);
                                            }
                                        }
                                    }
                                });
                            }
                        }).start();

                    }
                });
                break;
            case R.id.search_source_select:
                pickerPopup(SearchFragment.this, "select source", sourceIO.getNames(), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        SearchFragment.this.setSource(sourceIO.getSource(data));
                        setTitle("Search - " +data);
                        input.setText("");
                        adapter.clear();
                        adapter.reset();
                    }
                });
                break;
            case R.id.search_source_manager:
                startActivity(new Intent(getContext(), SourceManagerActivity.class));
                break;
        }
        return true;
    }

    @Override
    public short onBackPressed() {
        if(adapter.getSelectMode()){
            toggleSelectMode(null);
            return BACK_NONE;
        }
        //navigate history
        if(prevResBtn.getVisibility() == View.VISIBLE){
            prevResBtn.performClick();
            return BACK_NONE;
        }
        return BACK_HOME;
    }


    public void lockui(boolean lock){
        if(container != null){
            lockuiRecursive(container, lock);
        }
        if(adapter != null){
            adapter.lockui(lock);
        }
    }
}
