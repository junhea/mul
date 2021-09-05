package io.github.junheah.jsp.fragment;

import android.content.Intent;
import android.os.Build;
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
import androidx.transition.ArcMotion;
import androidx.transition.ChangeScroll;
import androidx.transition.Explode;
import androidx.transition.Fade;
import androidx.transition.Slide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.activity.FileChooserActivity;
import io.github.junheah.jsp.activity.MainActivity;
import io.github.junheah.jsp.adapter.PlayListNameAdapter;
import io.github.junheah.jsp.animation.DetailsTransition;
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
import io.github.junheah.jsp.ui.SlowLinearLayoutManager;

import static android.app.Activity.RESULT_OK;
import static io.github.junheah.jsp.Utils.showPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.model.song.Song.LOCAL;



public class PlayListFragment extends CustomFragment {



    PlayListNameAdapter parentadapter;

    RecyclerView recycler;

    PlayListIO playListIO;

    Song current;


    public PlayListFragment() {
        // don't do anything
    }

    public static final PlayListFragment newInstance() {
        PlayListFragment f = new PlayListFragment();
        return f;
    }

    @Override
    public void notify(Song song) {
        this.current = song;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playListIO = PlayListIO.getInstance(getContext());
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.current = ((PlayListContainerFragment)getParentFragment()).getCurrent();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new SlowLinearLayoutManager(getContext()));
        //((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);

        parentadapter = new PlayListNameAdapter(getContext(), new PlayListNameAdapter.PlayListSelectListener() {
            @Override
            public void itemClick(String key, TextView sharedElement) {
                DetailFragment fragment = DetailFragment.newInstance(key, current);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fragment.setSharedElementEnterTransition(new DetailsTransition());
                    fragment.setEnterTransition(new Fade());
                    fragment.setSharedElementReturnTransition(new DetailsTransition());
                    setReenterTransition(new Fade());
                }

                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(sharedElement, "title")
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        recycler.setAdapter(parentadapter);

    }

    @Override
    public short onBackPressed() {
        return BACK_HOME;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        System.out.println("menu selected in PL");
        switch (item.getItemId()) {
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
        inflater.inflate(R.menu.playlistlist_menu, menu);
    }






}
