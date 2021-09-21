package io.github.junheah.jsp.fragment;

import static io.github.junheah.jsp.Utils.deleteSongPopup;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.Song;

public class SongBottomMenu extends BottomSheetDialogFragment {

    Song song;
    PlayList playList;

    public static final String ACTION_DELETE = "delete";

    //todo :  finish this : add click listeners

    public static SongBottomMenu newInstance(Song song) {
        SongBottomMenu f = new SongBottomMenu();
        f.setParentItem(song);
        return f;
    }

    public static SongBottomMenu newInstance(PlayList playList, Song song) {
        SongBottomMenu f = new SongBottomMenu();
        f.setParentItem(song);
        f.setParentList(playList);
        return f;
    }

    public void setParentItem(Song song){this.song = song;}
    public void setParentList(PlayList playList){this.playList = playList;}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_bottom_menu, container,
                false);

        // get the views and attach the listener

        view.findViewById(R.id.bottom_sheet_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playList != null) deleteSongPopup(getContext(), playList, song);
                else deleteSongPopup(getContext(), song);
                getDialog().dismiss();
            }
        });

        return view;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setSolidNavigationBar(dialog);
        }

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragmentManager.getBackStackEntryCount() > 0)
            fragmentManager.popBackStack();
        fragmentTransaction.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setSolidNavigationBar(@NonNull Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            window.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            GradientDrawable dimDrawable = new GradientDrawable();

            GradientDrawable navigationBarDrawable = new GradientDrawable();
            navigationBarDrawable.setShape(GradientDrawable.RECTANGLE);
            navigationBarDrawable.setColor(getContext().getColor(R.color.colorDarkWindowBackground));

            Drawable[] layers = {dimDrawable, navigationBarDrawable};

            LayerDrawable windowBackground = new LayerDrawable(layers);
            windowBackground.setLayerInsetTop(1, metrics.heightPixels);

            window.setBackgroundDrawable(windowBackground);
        }
    }
}
