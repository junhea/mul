package io.github.junheah.jsp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.github.junheah.jsp.fragment.PlayListContainerFragment;
import io.github.junheah.jsp.fragment.CustomFragment;
import io.github.junheah.jsp.fragment.HomeFragment;
import io.github.junheah.jsp.fragment.PlayListFragment;
import io.github.junheah.jsp.fragment.SearchFragment;
import io.github.junheah.jsp.model.song.Song;

public class MainFragmentAdapter extends FragmentStateAdapter {

    Fragment[] fragments = new Fragment[3];

    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments[0] = SearchFragment.newInstance();
        fragments[1] = HomeFragment.newInstance();
        fragments[2] = PlayListContainerFragment.newInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }



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
        for(Fragment f : fragments){
            if (f.isAdded())
                ((CustomFragment)f).notify(song);
        }
    }

    public short onBackPressed(int index){
        return ((CustomFragment) fragments[index]).onBackPressed();
    }
}
