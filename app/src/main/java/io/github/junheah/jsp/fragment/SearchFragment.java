package io.github.junheah.jsp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
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

public class SearchFragment extends CallbackFragment{

    Source source;
    LinearLayoutCompat container;
    List<ExternalSong> result = new ArrayList<>();
    PlayListItemClickCallback callback;
    SearchResultAdapter adapter;

    public SearchFragment(){
        // do nothing
    }

    public static SearchFragment newInstance(Source source){
        SearchFragment fragment = new SearchFragment();
        fragment.setSource(source);

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
        Activity activity = getActivity();
        RecyclerView recyclerView = view.findViewById(R.id.search_result);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        EditText input = view.findViewById(R.id.search_input);
        Button prevResBtn = view.findViewById(R.id.prev_result_btn);

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
                pickerPopup(SearchFragment.this, "add to playlist", playListIO.getNames().toArray(new String[playListIO.getNames().size()]), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        //add song to playlist
                        song.fetch(new ScriptCallback() {
                            @Override
                            public void callback(Object res) {
                                playListIO.getPlayList(data).add(song);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockui(true);
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
        });
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.search_close:
                fragmentAdapterCallback.removeItem(SearchFragment.this);
                source.close();
                break;
        }
        return true;
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
