package io.github.junheah.jsp.fragment;

import static android.app.Activity.RESULT_OK;

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
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.transition.Fade;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.activity.DebugActivity;
import io.github.junheah.jsp.activity.FileChooserActivity;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.activity.SourceManagerActivity;
import io.github.junheah.jsp.adapter.LibraryAdapter;
import io.github.junheah.jsp.adapter.PlayListAdapter;
import io.github.junheah.jsp.animation.DetailsTransition;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.Library;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.room.ExternalSongDao;
import io.github.junheah.jsp.model.room.LocalSongDao;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.ui.SlowLinearLayoutManager;

import static io.github.junheah.jsp.MainApplication.library;
import static io.github.junheah.jsp.Utils.openDirectory;
import static io.github.junheah.jsp.Utils.openFile;
import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.model.song.Song.LOCAL;

public class HomeFragment extends CustomFragment {

    public final static int REQUEST_SELECT_SONG = 11;
    public final static int REQUEST_SELECT_FOLDER = 12;
    public final static int REQUEST_SELECT_LIBRARY = 13;

    LibraryLoader loader;
    LibraryAdapter adapter;
    Song current;
    String pl;

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

        Player player = MainActivity.getPlayer();
        if (player != null && player.getPlayList().getName().equals("")) {
            //restore using player
            library = (Library) player.getPlayList();
        }

        adapter = new LibraryAdapter(getContext(), library);
        adapter.setCallback(((MainActivity) getActivity()).getPlayListCallback());

        recycler.setAdapter(adapter);
//        if(needLoad) {
//            loader = new LibraryLoader();
//            loader.start();
//        }
    }

    @Override
    public void notify(String pl, Song song) {
        this.pl = pl;
        if(pl != null) {
            if (pl.equals(""))
                current = song;
            else
                current = null;
            if (adapter != null)
                adapter.currentChanged(current);
        }
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
            ((MainActivity)getActivity()).addSong(new SongPlayListParcel(null, song));
        }else if(requestCode == REQUEST_SELECT_FOLDER && resultCode == RESULT_OK){
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
                        Song song = new LocalSong(f.getAbsolutePath(), "", f.getAbsolutePath());
                        ((MainActivity)getActivity()).addSong(new SongPlayListParcel(null, song));
                    }
                }
            }
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
