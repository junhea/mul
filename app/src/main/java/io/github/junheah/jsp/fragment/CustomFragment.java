package io.github.junheah.jsp.fragment;

import androidx.fragment.app.Fragment;

import io.github.junheah.jsp.R;


public class CustomFragment extends Fragment {
    public void setTheme(){
        getContext().getTheme().applyStyle(R.style.FragmentTheme, true);
    }

    public boolean onBackPressed() {
        return false;
    }
}
