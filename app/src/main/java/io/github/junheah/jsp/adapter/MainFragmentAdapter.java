package io.github.junheah.jsp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.fragment.HomeFragment;
import io.github.junheah.jsp.fragment.PlayListFragment;
import io.github.junheah.jsp.fragment.SearchFragment;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.model.song.SongPlayListParcel;

public class MainFragmentAdapter extends FragmentStateAdapter{

    Fragment[] fragments = new Fragment[3];

    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments[0] = SearchFragment.newInstance();
        fragments[1] = HomeFragment.newInstance();
        fragments[2] = PlayListFragment.newInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public long getItemId(int position) {
        return fragments[position].hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for(Fragment f : fragments){
            if(f.hashCode() == itemId)
                return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public void notify(Song song) {
        //set now playing
        ((PlayListFragment)fragments[2]).notify(song);
    }
}
