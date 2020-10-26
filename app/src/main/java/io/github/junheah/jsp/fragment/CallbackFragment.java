package io.github.junheah.jsp.fragment;

import androidx.fragment.app.Fragment;

import io.github.junheah.jsp.interfaces.FragmentAdapterCallback;

public class CallbackFragment extends Fragment {

    FragmentAdapterCallback fragmentAdapterCallback;

    public void setAdapterCallback(FragmentAdapterCallback callback){
        this.fragmentAdapterCallback = callback;
    }
}
