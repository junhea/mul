package io.github.junhea.mul.fragment;

import static io.github.junhea.mul.Utils.deleteSongPopup;
import static io.github.junhea.mul.service.PlayerServiceHandler.play;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.github.junhea.mul.R;
import io.github.junhea.mul.model.Library;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.glide.AudioCoverModel;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;

public class SongBottomMenu extends BottomSheetDialogFragment {

    Song song;
    PlayList playList;

    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_RENAME = "rename";
    public static final String ACTION_PLAY = "play";

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



        View view = inflater.inflate(R.layout.bottom_menu_song, container,
                false);

        view.findViewById(R.id.bottom_menu_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playList instanceof Library) deleteSongPopup(getContext(), song);
                else deleteSongPopup(getContext(), playList, song);
                getDialog().dismiss();
            }
        });

        view.findViewById(R.id.bottom_menu_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(getContext(), playList, song);
                getDialog().dismiss();
            }
        });

        ((TextView)view.findViewById(R.id.bottom_name)).setText(song.getName());
        ((TextView)view.findViewById(R.id.bottom_artist)).setText(song.getArtist());
        ((TextView)view.findViewById(R.id.bottom_path)).setText(song.getPath().toString());


        ImageView cover = view.findViewById(R.id.bottom_cover);
        //cover art
        if (song instanceof ExternalSong) {
            String url = ((ExternalSong) song).getCoverUrl();
            if (url != null && url.length() > 0)
                Glide.with(cover)
                        .load(url)
                        .placeholder(R.drawable.music_dark)
                        .fallback(R.drawable.music_dark)
                        .into(cover);
        } else {
            if(!((LocalSong)song).nocover) {
                Glide.with(cover)
                        .load(new AudioCoverModel(song.getPath()))
                        .dontTransform()
                        .placeholder(R.drawable.music_dark)
                        .fallback(R.drawable.music_dark)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                //no cover
                                ((LocalSong) song).nocover = true;
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(cover);
            }else{
                //local and no cover
                Glide.with(cover)
                        .load(R.drawable.music_dark)
                        .dontTransform()
                        .into(cover);
            }
        }

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
}
