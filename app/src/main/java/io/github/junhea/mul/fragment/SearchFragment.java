package io.github.junhea.mul.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
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

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.R;
import io.github.junhea.mul.SourceIO;
import io.github.junhea.mul.activity.SourceManagerActivity;
import io.github.junhea.mul.activity.SourceSettingActivity;
import io.github.junhea.mul.adapter.SearchResultAdapter;
import io.github.junhea.mul.interfaces.IntegerCallback;
import io.github.junhea.mul.interfaces.SearchResultInterface;
import io.github.junhea.mul.interfaces.ScriptCallback;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.room.SongDatabase;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.ExternalSongContainer;
import io.github.junhea.mul.model.song.Song;
import io.github.junhea.mul.model.source.Search;
import io.github.junhea.mul.model.source.Source;

import static io.github.junhea.mul.MainApplication.library;
import static io.github.junhea.mul.Utils.createSnackbar;
import static io.github.junhea.mul.Utils.getPlayList;
import static io.github.junhea.mul.Utils.lockuiRecursive;
import static io.github.junhea.mul.Utils.pickerPopup;
import static io.github.junhea.mul.Utils.snackbar;
import static io.github.junhea.mul.activity.SourceSettingActivity.SOURCE_SETTING_REQUEST;
import static io.github.junhea.mul.model.song.Song.EXTERNAL;
import static io.github.junhea.mul.service.PlayerServiceHandler.play;

public class SearchFragment extends CustomFragment {

    Source source;
    LinearLayoutCompat container;
    List<ExternalSong> result = new ArrayList<>();
    SearchResultAdapter adapter;
    Button prevResBtn;
    PlayListIO playListIO;
    SourceIO sourceIO;
    EditText input;


    //todo 기본적으로 라이브러리 검색 기능 + 소스 선택시 bottom menu로

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
        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    view.findViewById(R.id.search_btn).performClick();
                    return true;
                }
                return false;
            }
        });
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
            public void clickedSong(Song song) {
                //fetch data
                if(song instanceof ExternalSong)
                    ((ExternalSong)song).fetch(getContext(), new ScriptCallback() {
                        @Override
                        public void callback(Object res) {
                            //play in player
                            PlayList pl = new PlayList(getContext(),"", true);
                            pl.add(song);
                            play(getContext(), pl, song);
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
            public void longClickedSong(Song song) {
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

    public void toggleSelectMode(Song song){
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
        menu.findItem(R.id.search_select_all).setVisible(adapter == null ? false : adapter.getSelectMode());
        menu.findItem(R.id.search_source_setting).setVisible(source != null);
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
                String[] pls = new String[playListIO.getNames().size()+1];
                pls[0] = getString(R.string.fragment_home_title);
                int i = 1;
                for(String name : playListIO.getNames()){
                    pls[i++] = name;
                }
                pickerPopup(SearchFragment.this, getString(R.string.search_add_to), pls, new IntegerCallback() {
                    @Override
                    public void callback(int i) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SongDatabase db = SongDatabase.getInstance(getContext());
                                List<long[]> ids = new ArrayList<>();
                                for(int j=0; j<res.size(); j++){
                                    Song s = res.get(j);
                                    try {
                                        long id = db.externalDao().insert((ExternalSong) s);
                                        s.setSid(id);
                                        ids.add(new long[]{EXTERNAL, id});
                                    }catch (Exception e){
                                        res.set(j, db.externalDao().findWithId(((ExternalSong)s).getId()));
                                        ids.add(new long[]{EXTERNAL, res.get(j).getSid()});
                                    }
                                }
                                //ui thread
                                String data = pls[i];

                                if(i>0) {
                                    //add song to playlist
                                    PlayList playList = getPlayList(data);

                                    if(playList == null){
                                        //don't need load : just add via playListio
                                        boolean success = playListIO.addSongs(data, ids);
                                        View view = getView();
                                        if(view != null){
                                            snackbar(view,
                                                    getString(success ? R.string.msg_add_success : R.string.msg_add_err_duplicate),
                                                    getString(R.string.msg_ok));
                                        }

                                    }else {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                boolean success = true;
                                                for (Song s : res) {
                                                    if(!playList.add(s))
                                                        success = false;
                                                }
                                                View view = getView();
                                                if(view != null){
                                                    createSnackbar(view,
                                                            getString(success ? R.string.msg_add_success : R.string.msg_add_err_duplicate),
                                                            getString(R.string.msg_ok)).show();
                                                }
                                            }
                                        });
                                    }
                                }

                                for(Song s : res){
                                    library.addWithSort(s);
                                }
                            }
                        }).start();

                    }
                });
                break;
            case R.id.search_select_all:
                adapter.toggleSelectAll();
                break;
            case R.id.search_source_select:
                pickerPopup(SearchFragment.this, getString(R.string.menu_select_source), sourceIO.getNames(), new IntegerCallback() {
                    @Override
                    public void callback(int i) {
                        String data = sourceIO.getNames()[i];
                        SearchFragment.this.setSource(sourceIO.getSource(data));
                        setTitle(getString(R.string.fragment_search_title)+ " - " +data);

                        adapter.clear();
                        adapter.reset();

                        getActivity().invalidateOptionsMenu();
                    }
                });
                break;
            case R.id.search_source_setting:
                Intent intent = new Intent(getContext(), SourceSettingActivity.class);
                intent.putExtra("source", source.getName());
                startActivity(intent);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SOURCE_SETTING_REQUEST:
                if(resultCode == RESULT_OK){
                    //refresh source
                }
                break;
        }
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
