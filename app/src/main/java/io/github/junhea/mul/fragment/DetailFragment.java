package io.github.junhea.mul.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.R;
import io.github.junhea.mul.activity.DebugActivity;
import io.github.junhea.mul.activity.SettingsActivity;
import io.github.junhea.mul.adapter.PlayListAdapter;
import io.github.junhea.mul.interfaces.SongCallback;
import io.github.junhea.mul.model.ItemMoveCallback;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;
import io.github.junhea.mul.model.song.SongDataParser;
import io.github.junhea.mul.model.song.SongPlayListParcel;
import io.github.junhea.mul.ui.SlowLinearLayoutManager;

import static android.app.Activity.RESULT_OK;
import static io.github.junhea.mul.MainApplication.library;
import static io.github.junhea.mul.Utils.deleteSongPopup;
import static io.github.junhea.mul.Utils.getPlayList;
import static io.github.junhea.mul.Utils.openDirectory;
import static io.github.junhea.mul.Utils.openFile;
import static io.github.junhea.mul.Utils.openLibrary;
import static io.github.junhea.mul.Utils.snackbar;
import static io.github.junhea.mul.activity.SettingsActivity.REQUEST_SETTINGS;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_FOLDER;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_LIBRARY;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_SONG;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DetailFragment extends CustomFragment implements SongCallback {
    String name;
    PlayListAdapter adapter;
    PlayListLoader loader;
    RecyclerView recycler;
    PlayListIO playListIO;

    private static PlayList playList;



    public static synchronized PlayList getCurrentPlayList(){
        return playList;
    }

    public void addSong(SongPlayListParcel parcel) {
        SongDataParser.parse(getContext(), parcel);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playListIO = PlayListIO.getInstance(getContext());
    }

    @Override
    public void onStop() {
        if(loader != null)
            loader.interrupt();
        super.onStop();
    }

    @Override
    public void notify(String pl, Song song) {
        //now playing changed
        this.pl = pl;
        if (pl != null && pl.equals(this.name))
            current = song;
        else
            current = null;
        if (adapter != null)
            adapter.currentChanged(current);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_playlist, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.playlist_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_addSong:
                        showAddMenu();
                        break;
                    case R.id.menu_edit:
                        //edit mode
                        adapter.toggleEditMode();
                        break;
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        this.title = view.findViewById(R.id.subtitle);
        title.setVisibility(View.VISIBLE);
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new SlowLinearLayoutManager(getContext()));
        ((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);

        //playlist selected
        boolean needLoad = false;

        playList = getPlayList(name);
        if (playList == null){
            playList = playListIO.get(name);
            needLoad = true;
        }
        adapter = new PlayListAdapter(getContext(), playList);
        adapter.setMenuCallback(DetailFragment.this);
        adapter.currentChanged(current);
        //drag
        ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recycler);
        adapter.setDragListener(new ItemMoveCallback.StartDragListener(){
            @Override
            public void requestDrag(RecyclerView.ViewHolder viewHolder) {
                touchHelper.startDrag(viewHolder);
            }
        });

        recycler.setAdapter(adapter);
        setTitle(name);

        if(needLoad){
            loader = new PlayListLoader(playList);
            loader.start();
        }
    }

    public static DetailFragment newInstance(String key, String currentpl, Song current){
        DetailFragment f = new DetailFragment();
        f.setName(key);
        f.notify(currentpl, current);
        return f;
    }

    @Override
    public void notify(Song song) {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.add(SongBottomMenu.newInstance(playList, song), "bottom_menu");
        ft.addToBackStack(null);
        ft.commit();
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public short onBackPressed() {
        getFragmentManager().popBackStackImmediate();
        return BACK_NONE;
    }

    void showAddMenu(){
        View view = getActivity().findViewById(R.id.menu_addSong);
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.add_menu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    final static String[] extensions = {"3gp","mp4","m4a","aac","ts","3gp","flac","gsm","mid","xmf","mxmf","rtttl","rtx","ota","imy","mp3","mkv","wav","ogg"};
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_SONG && resultCode == RESULT_OK){
            String path = data.getStringExtra("path");
            LocalSong song = new LocalSong(path, "", path);
            //add to current visible playlist
            addSong(new SongPlayListParcel(playList, song));
        }else if(requestCode == REQUEST_SELECT_FOLDER && resultCode == RESULT_OK){
            String path = data.getStringExtra("path");
            List<Song> songs = new ArrayList<>();
            for(File f : new File(path).listFiles()){
                if(!f.isDirectory()){
                    boolean supported = false;
                    for(String ext : extensions){
                        if(f.getName().toLowerCase().endsWith("."+ ext)){
                            supported = true;
                            break;
                        }
                    }
                    if(supported){
                        songs.add(new LocalSong(f.getAbsolutePath(), "", f.getAbsolutePath()));
                    }
                }
            }
            addSong(new SongPlayListParcel(playList, songs));
        }else if(requestCode == REQUEST_SELECT_LIBRARY && resultCode == RESULT_OK){
            //sid list
            long[][] d = new Gson().fromJson(data.getStringExtra("data"), new TypeToken<long[][]>(){}.getType());
            //add
            for(long[] sid : d){
                boolean res = playList.add(library.getWithId(sid));
                snackbar(getView(),
                        res ? getString(R.string.msg_add_success) : getString(R.string.msg_add_err_duplicate),
                        getString(R.string.msg_ok));
            }
        }
    }


    public class PlayListLoader extends Thread{
        boolean stop = false;
        PlayList pl;

        public PlayListLoader(PlayList pl) {
            super();
            this.pl = pl;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            List<Song> pls = new ArrayList<>();
            //add from mainapplication.library
            for(long[] id : playListIO.getids(pl.getName())){
                pls.add(library.getWithId(id));
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    pl.addAll(pls);
                }
            });
        }

        @Override
        public void interrupt() {
            stop = true;
            super.interrupt();
        }
    }
}
