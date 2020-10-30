package io.github.junheah.jsp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.Activity2.MainActivity;
import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.PlayListAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

import static android.app.Activity.RESULT_OK;
import static io.github.junheah.jsp.Utils.YesNoPopup;
import static io.github.junheah.jsp.Utils.playListDeserializer;
import static io.github.junheah.jsp.Utils.playListSerializer;
import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;

public class PlayListFragment extends CallbackFragment {
    final static int REQUEST_SELECT_SONG = 11;
    final static int REQUEST_SELECT_FOLDER = 12;
    final static int REQUEST_SELECT_EXTERNAL = 13;

    PlayList playList;
    PlayListAdapter adapter;
    PlayListItemClickCallback callback;

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

        //playlist callback
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
                            PlayListIO io =  new PlayListIO(getContext());

                            if(io.getNames().contains(data)){
                                //duplicate
                                showPopup(getContext(),data,"이 플레이리스트는 이미 존재합니다");
                            }else {
                                //create playlist instance
                                PlayList pl = new PlayList(data);

                                //create playlist fragment and set callbacks
                                PlayListFragment fragment = PlayListFragment.newInstance(pl);
                                fragment.setAdapterCallback(fragmentAdapterCallback);

                                //add to adapter
                                fragmentAdapterCallback.addItem(fragment);

                                //save
                                io.write(pl);
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
                                    new PlayListIO(getContext()).delete(playList);
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
                        openFile(REQUEST_SELECT_SONG);
                        break;
                    case R.id.menu_addLocalFolder:
                        openDirectory(REQUEST_SELECT_FOLDER);
                        break;
                    case R.id.menu_addExternal:
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void openFile(int requestCode) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
        }else{
            //internal file browser
        }
        startActivityForResult(intent, requestCode);
    }

    public void openDirectory(int requestCode) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            //internal file browser
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_SONG && resultCode == RESULT_OK){
            Uri uri = data.getData();
            Song song = new LocalSong(uri.toString(), "", uri.toString(),"", getContext());

            //add to current visible playlist
            playList.add(song);
            //save
            new PlayListIO(getContext()).write(playList);
        }else if(requestCode == REQUEST_SELECT_FOLDER && resultCode == RESULT_OK){
            Uri uri = data.getData();
        }
    }
}
