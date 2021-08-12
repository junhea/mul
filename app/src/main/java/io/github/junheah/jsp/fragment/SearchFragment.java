package io.github.junheah.jsp.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.adapter.SearchResultAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.SearchResultInterface;
import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.source.Search;
import io.github.junheah.jsp.model.source.Source;

import static io.github.junheah.jsp.MainApplication.playListIO;
import static io.github.junheah.jsp.Utils.lockuiRecursive;
import static io.github.junheah.jsp.Utils.pickerPopup;

public class SearchFragment extends CustomFragment {

    Source source;
    LinearLayoutCompat container;
    List<ExternalSong> result = new ArrayList<>();
    PlayListItemClickCallback callback;
    SearchResultAdapter adapter;
    Button prevResBtn;

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
        EditText input = view.findViewById(R.id.search_input);
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
        SourceIO sourceIO = new SourceIO(getContext());
        sourceIO.load();

        view.findViewById(R.id.search_source_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerPopup(SearchFragment.this, "select source", sourceIO.getNames(), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        SearchFragment.this.setSource(sourceIO.getSource(data));
                        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Search - " +data);
                        input.setText("");
                        adapter.clear();
                        adapter.reset();
                    }
                });
            }
        });

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
                        PlayList pl = new PlayList("", true);
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
        setHasOptionsMenu(true);
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
                List<ExternalSong> res = adapter.getSelected();
                pickerPopup(SearchFragment.this, "add to playlist", playListIO.getNames().toArray(new String[playListIO.getNames().size()]), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        //add song to playlist
                        for(ExternalSong s : res){
                            //TODO : 노래 추가 - playlistio를 사용하되, 현재 플레이어에 로드된 플레이리스트일 경우와, playlistfragment에 로드된 플레이리스트일 경우를 고려
                        }
                        toggleSelectMode(null);
                    }
                });
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        String title = "Search";
        if(source != null && source.getName() != null){
            title += " - " + source.getName();
        }
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
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
