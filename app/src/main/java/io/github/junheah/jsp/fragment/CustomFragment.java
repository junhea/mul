package io.github.junheah.jsp.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.junheah.jsp.R;


public class CustomFragment extends Fragment {
    public static final short BACK_NONE = 0;
    public static final short BACK_NORMAL = 1;
    public static final short BACK_HOME = 2;
    TextView title;

    public void setTheme(){
        getContext().getTheme().applyStyle(R.style.FragmentTheme, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.title);
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public short onBackPressed() {
        return BACK_NORMAL;
    }
}
