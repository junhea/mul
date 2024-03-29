package io.github.junhea.mul.fragment;

import static android.app.Activity.RESULT_OK;

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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.activity.DebugActivity;
import io.github.junhea.mul.R;
import io.github.junhea.mul.activity.SettingsActivity;
import io.github.junhea.mul.adapter.LibraryAdapter;
import io.github.junhea.mul.interfaces.SongCallback;
import io.github.junhea.mul.model.Library;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.room.ExternalSongDao;
import io.github.junhea.mul.model.room.LocalSongDao;
import io.github.junhea.mul.model.room.SongDatabase;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;
import io.github.junhea.mul.model.song.SongDataParser;
import io.github.junhea.mul.model.song.SongPlayListParcel;
import io.github.junhea.mul.ui.SlowLinearLayoutManager;

import static io.github.junhea.mul.MainApplication.library;
import static io.github.junhea.mul.Utils.deleteSongPopup;
import static io.github.junhea.mul.Utils.getPlayList;
import static io.github.junhea.mul.Utils.openDirectory;
import static io.github.junhea.mul.Utils.openFile;
import static io.github.junhea.mul.activity.SettingsActivity.REQUEST_SETTINGS;

public class HomeFragment extends CustomFragment{

    public final static int REQUEST_SELECT_SONG = 11;
    public final static int REQUEST_SELECT_FOLDER = 12;
    public final static int REQUEST_SELECT_LIBRARY = 13;

    LibraryLoader loader;
    LibraryAdapter adapter;

    public HomeFragment(){
        //don't do anything
    }

    public static final HomeFragment newInstance() {
        HomeFragment f = new HomeFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RecyclerView recycler = view.findViewById(R.id.recycler);
        SlowLinearLayoutManager lm = new SlowLinearLayoutManager(getContext());
        recycler.setLayoutManager(lm);

        PlayList tmp = getPlayList("");
        if(tmp != null){
            library = (Library)tmp;
        }

        adapter = new LibraryAdapter(getContext(), library);
        notify(pl, current);
        adapter.setMenuCallback(new SongCallback() {
            @Override
            public void notify(Song song) {
                //open menu
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(SongBottomMenu.newInstance(library, song), "bottom_menu");
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        recycler.setAdapter(adapter);
    }

    @Override
    public void notify(String pl, Song song) {
        this.pl = pl;
        if (pl != null && pl.equals(""))
            current = song;
        else
            current = null;
        if (adapter != null)
            adapter.currentChanged(current);
    }

    @Override
    public void onAnimationEnd() {
        //show covers
        adapter.setShowCover(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_addSong:
                showAddMenu();
                break;
            case R.id.menu_debug:
                startActivity(new Intent(getContext(), DebugActivity.class));
                break;
            case R.id.menu_settings:
                getActivity().startActivityForResult(new Intent(getContext(), SettingsActivity.class), REQUEST_SETTINGS);
                break;

        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(loader != null && !loader.stop)
            loader.interrupt();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.home_menu, menu);
    }
    final static String[] extensions = {"3gp","mp4","m4a","aac","ts","3gp","flac","gsm","mid","xmf","mxmf","rtttl","rtx","ota","imy","mp3","mkv","wav","ogg"};

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_SONG && resultCode == RESULT_OK){
            String path = data.getStringExtra("path");
            LocalSong song = new LocalSong(path, "", path);
            //add to current visible playlist
            SongDataParser.parse(getContext(), new SongPlayListParcel(null, song));
        }else if(requestCode == REQUEST_SELECT_FOLDER && resultCode == RESULT_OK){
            List<Song> songs = new ArrayList<>();
            String path = data.getStringExtra("path");
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
            SongDataParser.parse(getContext(), new SongPlayListParcel(null, songs));
        }
    }

    void showAddMenu(){
        View view = getActivity().findViewById(R.id.menu_addSong);
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.add_menu);
        popupMenu.getMenu().findItem(R.id.menu_addFromLibrary).setVisible(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_addLocalSong:
                        openFile(HomeFragment.this);
                        break;
                    case R.id.menu_addLocalFolder:
                        openDirectory(HomeFragment.this);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public class LibraryLoader extends Thread{
        boolean stop = false;

        public LibraryLoader() {
            super();
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            //load songs from db
            SongDatabase db = SongDatabase.getInstance(getContext());
            LocalSongDao ld = db.localDao();
            ExternalSongDao ed = db.externalDao();

            List<Song> pls = new ArrayList<>();
            pls.addAll(ld.getAll());
            pls.addAll(ed.getAll());

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for(Song s : pls){
                        library.addWithSort(s);
                    }
                }
            });
        }

        @Override
        public void interrupt() {
            stop = true;
            super.interrupt();
        }
    };

}
