package io.github.junheah.jsp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.fragment.PlayListFragment;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.model.song.Song;

public class MainFragmentAdapter extends FragmentStateAdapter{

    List<Fragment> fragments;

    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new ArrayList<>();
    }

    public void append(Fragment fragment){
        fragments.add(fragment);
        notifyItemInserted(fragments.size()-1);
    }

    public void insert(int index, Fragment fragment){
        fragments.add(index, fragment);
        notifyItemInserted(index);
    }

    public void remove(int index){
        fragments.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public long getItemId(int position) {
        return fragments.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for(Fragment f : fragments){
            if(f.hashCode() == itemId)
                return true;
        }
        return false;
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

    public void notify(Song song) {
        //set now playing
        for(Fragment f : fragments){
            if(f instanceof PlayListFragment){
                ((PlayListFragment)f).notify(song);
            }
        }
    }
}
