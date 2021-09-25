package io.github.junheah.jsp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.transition.Fade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.activity.FileChooserActivity;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.adapter.PlayListAdapter;
import io.github.junheah.jsp.adapter.PlayListNameAdapter;
import io.github.junheah.jsp.animation.DetailsTransition;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.ItemMoveCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.room.ExternalSongDao;
import io.github.junheah.jsp.model.room.LocalSongDao;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.song.SongDataParser;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.service.PlayerServiceHandler;
import io.github.junheah.jsp.ui.SlowLinearLayoutManager;

import static android.app.Activity.RESULT_OK;
import static io.github.junheah.jsp.MainApplication.library;
import static io.github.junheah.jsp.Utils.deleteSongPopup;
import static io.github.junheah.jsp.Utils.getPlayList;
import static io.github.junheah.jsp.Utils.openDirectory;
import static io.github.junheah.jsp.Utils.openFile;
import static io.github.junheah.jsp.Utils.openLibrary;
import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.Utils.snackbar;
import static io.github.junheah.jsp.fragment.HomeFragment.REQUEST_SELECT_FOLDER;
import static io.github.junheah.jsp.fragment.HomeFragment.REQUEST_SELECT_LIBRARY;
import static io.github.junheah.jsp.fragment.HomeFragment.REQUEST_SELECT_SONG;
import static io.github.junheah.jsp.model.song.Song.LOCAL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DetailFragment extends CustomFragment implements SongCallback {
    String name;
    PlayListAdapter adapter;
    PlayListLoader loader;
    RecyclerView recycler;
    PlayListIO playListIO;
    Song current;
    String pl;

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
//        SongBottomMenu f = SongBottomMenu.newInstance(playList, song);
//        f.show(getFragmentManager(), "bottom_menu");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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


    void showAddMenu(){
        View view = getActivity().findViewById(R.id.menu_addSong);
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.add_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_addLocalSong:
                        openFile(DetailFragment.this);
                        break;
                    case R.id.menu_addLocalFolder:
                        openDirectory(DetailFragment.this);
                        break;
                    case R.id.menu_addFromLibrary:
                        openLibrary(DetailFragment.this);
                        break;
                }
                return true;
            }
        });
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
            System.out.println(data.getStringExtra("data"));
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.playlist_menu, menu);
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
