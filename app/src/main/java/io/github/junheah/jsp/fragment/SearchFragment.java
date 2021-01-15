package io.github.junheah.jsp.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.adapter.SearchResultAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.SearchResultInterface;
import io.github.junheah.jsp.interfaces.V8Callback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.source.Search;
import io.github.junheah.jsp.model.source.Source;

import static io.github.junheah.jsp.model.source.Source.USER_DATA_REQUEST;

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
        SwipyRefreshLayout result_layout = view.findViewById(R.id.search_result_layout);

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
        result_layout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(swipehistory.size()>0){
                    swipehistory.get(swipehistory.size()-1).run();
                }
            }
        });

        container = view.findViewById(R.id.search_container);
        adapter = new SearchResultAdapter(getContext(), result);
        adapter.setListener(new SearchResultInterface(){
            @Override
            public void clickedSong(ExternalSong song) {
                //fetch data
                song.fetch(getContext(), new Runnable() {
                    @Override
                    public void run() {
                        //play in player
                        PlayList pl = new PlayList("");
                        pl.add(song);
                        callback.SongClicked(song, pl);
                    }
                });
            }

            @Override
            public void clickedSongContainer(ExternalSongContainer container) {
                lockui(true);
                Runnable onContainer = new Runnable() {
                    @Override
                    public void run() {
                        adapter.appendAndChangeData(container.getSongs());
                        prevResBtn.setVisibility(adapter.hasHistory() ? View.VISIBLE : View.GONE);
                        lockui(false);
                    }
                };
                container.fetch(getContext(), onContainer);
                swipehistory.add(new Runnable() {
                    @Override
                    public void run() {
                        container.fetch(getContext(), new Runnable() {
                            @Override
                            public void run() {
                                adapter.addAll(container.getSongs());
                                prevResBtn.setVisibility(adapter.hasHistory() ? View.VISIBLE : View.GONE);
                                lockui(false);
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
                Runnable onSearch = new Runnable() {
                    @Override
                    public void run() {
                        //update ui
                        adapter.addAll(search.getResult());
                        lockui(false);
                    }
                };
                search.fetch(onSearch);
                swipehistory.add(new Runnable() {
                    @Override
                    public void run() {
                        search.fetch(onSearch);
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
        if(source.init(this)){
            source.initThread(new V8Callback() {
                @Override
                public void callback(String res) {
                    // script loaded
                    lockui(false);
                }

                @Override
                public void error(Exception e) {
                    // failed
                    lockui(false);
                }
            },this.getContext(),null);
        }

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

    public void lockui(boolean lock){
        if(container != null){
            for(int i =0 ; i<container.getChildCount(); i++){
                View v = container.getChildAt(i);
                v.setEnabled(!lock);
                if(v instanceof RecyclerView){
                    ((RecyclerView)v).suppressLayout(lock);
                }
                if(v instanceof SwipyRefreshLayout){
                    ((SwipyRefreshLayout)v).setRefreshing(lock);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == USER_DATA_REQUEST){
            source.initThread(new V8Callback() {
                @Override
                public void callback(String res) {
                    // script loaded
                    lockui(false);
                }

                @Override
                public void error(Exception e) {
                    // failed
                    lockui(false);
                }
            },getActivity(), data.getStringExtra("data"));
        }
    }
}
