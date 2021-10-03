package io.github.junheah.jsp.fragment;

import static io.github.junheah.jsp.Utils.YesNoPopup;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.Utils.snackbar;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;

public class PlayListBottomMenu extends BottomSheetDialogFragment {

    PlayList playList;
    PlayListNameChangeNotifier notifier;
    int pos;


    public static PlayListBottomMenu newInstance(PlayList playList, PlayListNameChangeNotifier notifier, int pos){
        PlayListBottomMenu menu = new PlayListBottomMenu();
        menu.setParent(playList);
        menu.setNotifier(notifier, pos);
        return menu;
    }

    public void setNotifier(PlayListNameChangeNotifier notifier, int pos){
        this.notifier = notifier;
        this.pos = pos;
    }


    public PlayListBottomMenu() {
        super();
    }

    public void setParent(PlayList playList){
        this.playList = playList;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PlayListIO playListIO = PlayListIO.getInstance(getContext());

        View view = inflater.inflate(R.layout.bottom_menu_playlist, container,
                false);

        view.findViewById(R.id.bottom_menu_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YesNoPopup(getContext(), playList.getName(), getContext().getString(R.string.msg_delete_playlist), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlayListIO.getInstance(getContext()).delete(playList.getName());
                        notifier.itemRemoved(pos);
                    }
                });
                getDialog().dismiss();
            }
        });

        view.findViewById(R.id.bottom_menu_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleInputPopup(getContext(), getContext().getString(R.string.msg_rename_playlist), playList.getName(), new StringCallback() {
                    @Override
                    public void callback(String data) {
                        if(data.length()>0 && playListIO.getNames().indexOf(data) == -1) {
                            playListIO.rename(playList, data);
                            notifier.itemChanged(pos, data);
                        }else{
                            snackbar(getParentFragment().getView(), getContext().getString(R.string.msg_rename_playlist_fail),getContext().getString(R.string.msg_ok));
                        }
                    }
                });
                getDialog().dismiss();
            }
        });

        ((TextView)view.findViewById(R.id.bottom_name)).setText(playList.getName());

        ((TextView)view.findViewById(R.id.bottom_size)).setText(String.valueOf(playListIO.getids(playList.getName()).size()));
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
        FragmentManager fragmentManager = getFragmentManager();
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

    public interface PlayListNameChangeNotifier{
        void itemChanged(int pos, String newName);
        void itemRemoved(int pos);
    }
}
