package io.github.junhea.mul.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

import io.github.junhea.mul.R;
import io.github.junhea.mul.model.song.Song;

public class PlayListContainerFragment extends CustomFragment{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setTheme();
        return inflater.inflate(R.layout.fragment_container, container,false);
    }

    public static PlayListContainerFragment newInstance() {
        PlayListContainerFragment fragment = new PlayListContainerFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, PlayListFragment.newInstance())
                    .commit();
        }
    }

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    @Override
    public short onBackPressed() {
        Fragment current = getVisibleFragment();
        if(current != null){
            return ((CustomFragment)current).onBackPressed();
        }
        return BACK_HOME;
    }

    public Song getCurrent(){
        return this.current;
    }

    public String getCurrentPl(){return this.pl;}

    @Override
    public void notify(String pl, Song song) {
        this.pl = pl;
        this.current = song;
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for(Fragment f : fragments){
            if(f instanceof CustomFragment)
                ((CustomFragment)f).notify(pl, song);
        }
    }
}
