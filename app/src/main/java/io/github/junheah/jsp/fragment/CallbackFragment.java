package io.github.junheah.jsp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.FragmentAdapterCallback;


public class CallbackFragment extends Fragment {

    FragmentAdapterCallback fragmentAdapterCallback;

    public void setAdapterCallback(FragmentAdapterCallback callback){
        this.fragmentAdapterCallback = callback;
    }

    public void setTheme(){
        getContext().getTheme().applyStyle(R.style.FragmentTheme, true);
    }
}
