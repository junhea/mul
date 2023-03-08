package io.github.junhea.mul.fragment;

import android.content.Intent;
import android.os.Build;
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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.activity.DebugActivity;
import io.github.junhea.mul.activity.SettingsActivity;
import io.github.junhea.mul.adapter.PlayListNameAdapter;
import io.github.junhea.mul.animation.DetailsTransition;
import io.github.junhea.mul.interfaces.StringCallback;
import io.github.junhea.mul.R;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.song.Song;
import io.github.junhea.mul.ui.SlowLinearLayoutManager;

import static io.github.junhea.mul.Utils.getPlayList;
import static io.github.junhea.mul.Utils.singleInputPopup;
import static io.github.junhea.mul.Utils.snackbar;
import static io.github.junhea.mul.activity.SettingsActivity.REQUEST_SETTINGS;


public class PlayListFragment extends CustomFragment implements PlayListBottomMenu.PlayListNameChangeNotifier {

    PlayListNameAdapter parentadapter;

    RecyclerView recycler;

    PlayListIO playListIO;

    public PlayListFragment() {
        // don't do anything
    }

    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    @Override
    public void notify(String pl, Song song) {
        this.pl = pl;
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
        this.pl = ((PlayListContainerFragment)getParentFragment()).getCurrentPl();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.playlistlist_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_addPlayList:
                        singleInputPopup(getContext(),getString(R.string.msg_create_playlist),getString(R.string.msg_create_playlist_name), new StringCallback() {
                            @Override
                            public void callback(String data) {
                                //playlist io
                                if(data.length() == 0 || playListIO.getNames().contains(data)){
                                    //duplicate
                                    snackbar(getView(),getString(R.string.msg_create_playlist_already_exists),getString(R.string.msg_ok));
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
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new SlowLinearLayoutManager(getContext()));
        //((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);

        parentadapter = new PlayListNameAdapter(getContext(), new PlayListNameAdapter.PlayListSelectListener() {
            @Override
            public void itemClick(String key, TextView sharedElement) {
                DetailFragment fragment = DetailFragment.newInstance(key, pl, current);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fragment.setSharedElementEnterTransition(new DetailsTransition());
                    fragment.setEnterTransition(new Fade());
                    fragment.setSharedElementReturnTransition(new DetailsTransition());
                    setReenterTransition(new Fade());
                }

                getParentFragmentManager()
                        .beginTransaction()
                        .addSharedElement(sharedElement, "title")
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void itemLongClick(String key, int pos) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                PlayList pl = getPlayList(key);
                if(pl == null) pl = playListIO.get(key);
                ft.add(PlayListBottomMenu.newInstance(pl, PlayListFragment.this, pos), "bottom_menu");
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        recycler.setAdapter(parentadapter);

    }

    @Override
    public short onBackPressed() {
        return BACK_HOME;
    }

    @Override
    public void itemRemoved(int pos) {
        parentadapter.remove(pos);
    }

    @Override
    public void itemChanged(int pos, String newName) { parentadapter.rename(pos, newName);}
}
