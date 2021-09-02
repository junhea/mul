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
import android.widget.TextView;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.activity.FileChooserActivity;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.adapter.PlayListNameAdapter;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.ItemMoveCallback;
import io.github.junheah.jsp.model.room.ExternalSongDao;
import io.github.junheah.jsp.model.room.LocalSongDao;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.PlayListAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

import static android.app.Activity.RESULT_OK;
import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.model.song.Song.LOCAL;



public class PlayListFragment extends CustomFragment {
    public final static int REQUEST_SELECT_SONG = 11;
    public final static int REQUEST_SELECT_FOLDER = 12;
    public final static int REQUEST_SELECT_EXTERNAL = 13;

    PlayListItemClickCallback callback;
    PlayListLoader loader;

    PlayListAdapter adapter;
    PlayListNameAdapter parentadapter;
    private static PlayList playList;

    LinearLayoutCompat titlebar;
    TextView titletext;
    RecyclerView recycler;

    PlayListIO playListIO;

    Song current;

    //TODO : 삭제시 중복있을때 인스턴스 구분


    public PlayListFragment() {
        // don't do anything
    }

    public static synchronized PlayList getCurrentPlayList(){
        return playList;
    }

    public static final PlayListFragment newInstance() {
        PlayListFragment f = new PlayListFragment();
        return f;
    }


    public void addSong(SongPlayListParcel parcel) {
        ((MainActivity)getActivity()).addSong(parcel);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        playListIO = PlayListIO.getInstance(getContext());
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //playListIO.detach(playList);
    }

    @Override
    public void onStop() {
        if(loader != null)
            loader.interrupt();
        super.onStop();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callback = ((MainActivity) getActivity()).getPlayListCallback();
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);

        titlebar = view.findViewById(R.id.playList_name_bar);
        titletext = view.findViewById(R.id.playlist_name);

        parentadapter = new PlayListNameAdapter(getContext(), new PlayListNameAdapter.PlayListSelectListener() {
            @Override
            public void itemClick(String key) {
                //playlist selected
                boolean needLoad = false;
                Player player = MainActivity.getPlayer();
                if (player != null && player.getPlayList().getName().equals(key)) {
                    //restore using player
                    playList = player.getPlayList();
                } else {
                    playList = playListIO.get(key);
                    needLoad = true;
                }
                adapter = new PlayListAdapter(getContext(), playList);
                adapter.currentChanged(current);
                adapter.setCallback(callback);
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

                getActivity().invalidateOptionsMenu();
                titlebar.setVisibility(View.VISIBLE);
                titletext.setText(key);

                if(needLoad){
                    loader = new PlayListLoader(playList);
                    loader.start();
                }
            }
        });

        view.findViewById(R.id.playlist_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().invalidateOptionsMenu();
                titlebar.setVisibility(View.GONE);
                recycler.setAdapter(parentadapter);
                if(loader != null){
                    loader.interrupt();
                    loader = null;
                }
                adapter = null;
                playList = null;
            }
        });
        recycler.setAdapter(parentadapter);

    }

    @Override
    public short onBackPressed() {
        if(titlebar.getVisibility() == View.VISIBLE) {
            getActivity().invalidateOptionsMenu();
            titlebar.setVisibility(View.GONE);
            recycler.setAdapter(parentadapter);
            if (loader != null) {
                loader.interrupt();
                loader = null;
            }
            adapter = null;
            playList = null;
            return BACK_NONE;
        }
        return BACK_HOME;
    }

    public void notify(Song song) {
        //now playing changed
        current = song;
        if(adapter != null) {
            if (playList.indexOf(song) > -1) {
                adapter.currentChanged(song);
            }else
                adapter.currentChanged(null);
        }
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
            case R.id.menu_addPlayList:
                singleInputPopup(getContext(), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        //playlist io
                        if(playListIO.getNames().contains(data)){
                            //duplicate
                            showPopup(getContext(),data,"이 플레이리스트는 이미 존재합니다");
                        }else {
                            //create playlist instance
                            playListIO.create(data);
                            if(parentadapter != null){
                                parentadapter.added(data);
                            }
                        }
                    }
                });
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        if(adapter == null)
            inflater.inflate(R.menu.playlistlist_menu, menu);
        else
            inflater.inflate(R.menu.playlist_menu, menu);
    }

    void showAddMenu(){
        View view = getActivity().findViewById(R.id.menu_addSong);
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.add_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_addLocalSong:
                        openFile();
                        break;
                    case R.id.menu_addLocalFolder:
                        openDirectory();
                        break;
                    case R.id.menu_addExternal:
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void openFile() {
        Intent intent = new Intent(getContext(), FileChooserActivity.class);
        intent.putExtra("mode", REQUEST_SELECT_SONG);
        startActivityForResult(intent, REQUEST_SELECT_SONG);
    }

    public void openDirectory() {
        Intent intent = new Intent(getContext(), FileChooserActivity.class);
        intent.putExtra("mode", REQUEST_SELECT_FOLDER);
        startActivityForResult(intent, REQUEST_SELECT_FOLDER);
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
            for(File f : new File(path).listFiles()){
                if(!f.isDirectory()){
                    boolean supported = false;
                    for(String ext : extensions){
                        System.out.println(f.getName());
                        if(f.getName().toLowerCase().endsWith("."+ ext)){
                            supported = true;
                            break;
                        }
                    }
                    if(supported){
                        Song song = new LocalSong(f.getAbsolutePath(), "", f.getAbsolutePath());
                        addSong(new SongPlayListParcel(playList, song));
                    }
                }
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
            //load songs from db
            SongDatabase db = SongDatabase.getInstance(getContext());
            LocalSongDao ld = db.localDao();
            ExternalSongDao ed = db.externalDao();

            List<Song> pls = new ArrayList<>();

            for(long[] id : playListIO.getids(pl.getName())){
                if(stop) break;
                Song target;
                if(id[0] == LOCAL){
                    target = ld.get(id[1]);
                }else{
                    target = ed.get(id[1]);
                }
                pls.add(target);
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
    };
}
