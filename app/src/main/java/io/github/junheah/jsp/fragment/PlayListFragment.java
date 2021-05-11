package io.github.junheah.jsp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import java.io.File;

import io.github.junheah.jsp.activity.FileChooser;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.interfaces.RequestDataUpdate;
import io.github.junheah.jsp.model.song.SongDataParser;
import io.github.junheah.jsp.model.source.Source;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.PlayListAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

import static android.app.Activity.RESULT_OK;
import static io.github.junheah.jsp.MainApplication.playListIO;
import static io.github.junheah.jsp.Utils.YesNoPopup;
import static io.github.junheah.jsp.Utils.getPathFromUri;
import static io.github.junheah.jsp.Utils.playListDeserializer;
import static io.github.junheah.jsp.Utils.playListSerializer;
import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;

public class PlayListFragment extends CallbackFragment implements RequestDataUpdate {
    public final static int REQUEST_SELECT_SONG = 11;
    public final static int REQUEST_SELECT_FOLDER = 12;
    public final static int REQUEST_SELECT_EXTERNAL = 13;

    PlayList playList;
    PlayListAdapter adapter;
    PlayListItemClickCallback callback;
    SongDataParser parserWorker;

    public PlayListFragment() {
        // don't do anything
    }

    public static final PlayListFragment newInstance(PlayList playList) {
        PlayListFragment f = new PlayListFragment();
        f.setPlayList(playList);
        return f;
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(parserWorker == null){
            parserWorker = new SongDataParser(getContext());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(parserWorker != null){
            parserWorker.forceStop();
            parserWorker = null;
        }
    }

    @Override
    public void requestDataUpdate(Song song) {
        if(parserWorker == null || !parserWorker.running){
            parserWorker = new SongDataParser(getContext());
            parserWorker.execute(song);
        }else{
            parserWorker.add(song);
        }
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

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString("name");
            Player player = ((MainActivity) getActivity()).getPlayer();

            if (player != null && player.getPlayList().getName().equals(name)) {
                //restore using player
                playList = player.getPlayList();
            } else {
                //restore using bundle
                String playListGson = savedInstanceState.getString("playlist");
                playList = playListDeserializer().fromJson(playListGson, new TypeToken<PlayList>() {
                }.getType());
            }
        }
        callback = ((MainActivity) getActivity()).getPlayListCallback();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("name", playList.getName());
        outState.putString("playlist", playListSerializer().toJson(playList));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlayListAdapter(getContext(), playList);
        adapter.setCallback(callback);
        adapter.setDataCallback(this);
        recycler.setAdapter(adapter);
        ((TextView) view.findViewById(R.id.playlist_name)).setText(playList.getName());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addPlayList:
                if (fragmentAdapterCallback != null) {
                    singleInputPopup(getContext(), new StringCallback() {
                        @Override
                        public void callback(String data) {
                            //playlist io

                            if(playListIO.getNames().contains(data)){
                                //duplicate
                                showPopup(getContext(),data,"이 플레이리스트는 이미 존재합니다");
                            }else {
                                //create playlist instance
                                PlayList pl = playListIO.create(data);

                                //create playlist fragment and set callbacks
                                PlayListFragment fragment = PlayListFragment.newInstance(pl);
                                fragment.setAdapterCallback(fragmentAdapterCallback);

                                //add to adapter
                                fragmentAdapterCallback.addItem(fragment);
                            }
                        }
                    });
                }
                break;
            case R.id.menu_deletePlayList:
                if (fragmentAdapterCallback != null) {
                    YesNoPopup(getContext(), playList.getName(), "이 플레이리스트를 삭제하겠습니까?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //delete fragment
                                    fragmentAdapterCallback.removeItem(PlayListFragment.this);

                                    //notify player
                                    playList.playListRemoved();

                                    //save
                                    playListIO.delete(playList);
                                }
                            });
                }
                break;
            case R.id.menu_addSong:
                showAddMenu();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
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
        Intent intent = new Intent(getContext(), FileChooser.class);
        intent.putExtra("mode", REQUEST_SELECT_SONG);
        startActivityForResult(intent, REQUEST_SELECT_SONG);
    }

    public void openDirectory() {
        Intent intent = new Intent(getContext(), FileChooser.class);
        intent.putExtra("mode", REQUEST_SELECT_FOLDER);
        startActivityForResult(intent, REQUEST_SELECT_FOLDER);
    }

    final static String[] extensions = {"3gp","mp4","m4a","aac","ts","3gp","flac","gsm","mid","xmf","mxmf","rtttl","rtx","ota","imy","mp3","mkv","wav","ogg"};
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_SONG && resultCode == RESULT_OK){
            String path = data.getStringExtra("path");
            Song song = new LocalSong(path, "", path);
            //add to current visible playlist
            playList.add(song);
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
                        playList.add(song);
                    }
                }
            }
        }
    }
}
