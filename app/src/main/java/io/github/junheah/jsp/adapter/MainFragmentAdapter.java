package io.github.junheah.jsp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.fragment.CallbackFragment;
import io.github.junheah.jsp.interfaces.FragmentAdapterCallback;

public class MainFragmentAdapter extends FragmentStateAdapter {

    List<Fragment> fragments;
    FragmentAdapterCallback callback;

    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new ArrayList<>();
        callback = new FragmentAdapterCallback() {
            @Override
            public void addItem(Object obj) {
                append((Fragment)obj);
            }

            @Override
            public void removeItem(Object obj) {
                int i = fragments.indexOf(obj);
                if(i>-1){
                    remove(i);
                }
            }

            @Override
            public void insertItem(int index, Object obj) {
                insert(index, (Fragment) obj);
            }
        };
    }

    public void append(Fragment fragment){
        if(fragment instanceof CallbackFragment)
            ((CallbackFragment) fragment).setAdapterCallback(callback);
        fragments.add(fragment);
        notifyItemInserted(fragments.size()-1);
    }

    public void insert(int index, Fragment fragment){
        if(fragment instanceof CallbackFragment)
            ((CallbackFragment) fragment).setAdapterCallback(callback);
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
}
