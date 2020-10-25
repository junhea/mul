package io.github.junheah.jsp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

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

    public Fragment getItemAt(int index){
        return fragments.get(index);
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
