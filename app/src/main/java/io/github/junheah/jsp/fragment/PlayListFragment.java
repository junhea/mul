package io.github.junheah.jsp.fragment;

import android.content.DialogInterface;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.PlayListAdapter;
import io.github.junheah.jsp.interfaces.AdapterNotifier;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.Song;

import static io.github.junheah.jsp.Utils.YesNoPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.Utils.songAdderPopup;

public class PlayListFragment extends CallbackFragment {
    PlayList playList;
    PlayListAdapter adapter;
    PlayListItemClickCallback callback;

    public PlayListFragment(PlayList playList, PlayListItemClickCallback callback) {
        this.playList = playList;
        this.callback = callback;
    }

    public PlayList getPlayList(){
        return this.playList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_recycler,container,false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlayListAdapter(getContext(), playList);
        adapter.setCallback(callback);
        recycler.setAdapter(adapter);
        ((TextView)view.findViewById(R.id.playlist_name)).setText(playList.getName());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.addPlayList:
                if(fragmentAdapterCallback != null){
                    singleInputPopup(getContext(), new StringCallback() {
                        @Override
                        public void callback(String data) {
                            PlayList pl = new PlayList(data);
                            fragmentAdapterCallback.addItem(new PlayListFragment(pl, callback));
                            new PlayListIO(getContext()).write(pl);
                        }
                    });
                }
                break;
            case R.id.deletePlayList:
                if(fragmentAdapterCallback != null){
                    YesNoPopup(getContext(), playList.getName(), "이 플레이리스트를 삭제하겠습니까?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //delete fragment
                                    fragmentAdapterCallback.removeItem(PlayListFragment.this);

                                    //save
                                    new PlayListIO(getContext()).delete(playList);
                                }
                            });
                }
                break;
            case R.id.addSong:
                songAdderPopup(getContext(), new SongCallback(){
                    @Override
                    public void callback(Song song) {
                        //add to current visible playlist
                        playList.add(song);
                        //save
                        new PlayListIO(getContext()).write(playList);
                    }
                });
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.playlist_menu, menu);
    }
}
