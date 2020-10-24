package io.github.junheah.jsp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.fragment.PlayListFragment;
import io.github.junheah.jsp.model.Song;

public class MainFragmentAdapter extends FragmentStateAdapter {

    List<Fragment> fragments;
    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity, Fragment fragment) {
        super(fragmentActivity);
        fragments = new ArrayList<>();
        fragments.add(fragment);
    }

    public void append(Fragment fragment){
        fragments.add(fragment);
        notifyItemInserted(fragments.size()-1);
    }

    public void addSong(int index, Song song){
        Fragment fragment = fragments.get(index);
        if(fragment instanceof PlayListFragment){
            ((PlayListFragment)fragment).addSong(song);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
