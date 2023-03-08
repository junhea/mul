package io.github.junhea.mul.fragment;

import static io.github.junhea.mul.Utils.openDirectory;
import static io.github.junhea.mul.Utils.openFile;
import static io.github.junhea.mul.Utils.openLibrary;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import io.github.junhea.mul.R;
import io.github.junhea.mul.model.PlayerStatus;
import io.github.junhea.mul.model.song.Song;


public class CustomFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    public static final short BACK_NONE = 0;
    public static final short BACK_NORMAL = 1;
    public static final short BACK_HOME = 2;
    TextView title;
    Song current;
    String pl;

    public void setTheme(){
        Context context = getContext();
        if(context != null)
            context.getTheme().applyStyle(R.style.FragmentTheme, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.title);
    }

    @Override
    public void onResume() {
        super.onResume();
        //get Now playing from playerstatus
        notify(PlayerStatus.playList == null ? null : PlayerStatus.playList.getName(), PlayerStatus.song);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public short onBackPressed() {
        return BACK_NORMAL;
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addLocalSong:
                openFile(CustomFragment.this);
                break;
            case R.id.menu_addLocalFolder:
                openDirectory(CustomFragment.this);
                break;
            case R.id.menu_addFromLibrary:
                openLibrary(CustomFragment.this);
                break;
        }
        return true;
    }

    public void notify(String pl, Song song){}

    public void onAnimationEnd(){}
}
