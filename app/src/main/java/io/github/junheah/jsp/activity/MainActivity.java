package io.github.junheah.jsp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.TimerThread;
import io.github.junheah.jsp.animation.ZoomOutPageTransformer;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.SongDataParser;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.MainFragmentAdapter;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.Song;
import io.github.junheah.jsp.service.PlayerServiceHandler;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.github.junheah.jsp.Utils.deleteSongPopup;
import static io.github.junheah.jsp.Utils.getStatusBarHeight;
import static io.github.junheah.jsp.Utils.getTimeStamp;
import static io.github.junheah.jsp.Utils.toggleButtons;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_HOME;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NONE;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NORMAL;
import static io.github.junheah.jsp.model.PlayList.MODE_REPEAT_ALL;
import static io.github.junheah.jsp.model.PlayList.MODE_REPEAT_SONG;
import static io.github.junheah.jsp.model.PlayList.MODE_SHUFFLE;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_EXIT;
import static io.github.junheah.jsp.service.PlayerServiceHandler.bound;
import static io.github.junheah.jsp.service.PlayerServiceHandler.player;


public class MainActivity extends PlayerBaseActivity {


    ViewPager2 viewPager;
    MainFragmentAdapter adapter;
    ImageView logo;
    public final static int PERMISSION_CODE = 14245;

    @Override
    public void notifySongChanged(PlayList playList, Song song){
        //custom notifier actions here
        if(adapter != null){
            adapter.notify(playList == null ? null : playList.getName(), song);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        base_layout_id = R.layout.activity_main;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        panelOnCreate();

        //reveal animation
        viewPager = this.findViewById(R.id.viewPager);

        logo = this.findViewById(R.id.logo);
        if(savedInstanceState == null) {
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int startoff = 100;

                    //set initial object position
                    viewPager.setTranslationY(viewPager.getHeight());
                    panel.setTranslationY(panel.getHeight());

                    //animate
                    ObjectAnimator animation = ObjectAnimator.ofFloat(viewPager, "translationY", viewPager.getHeight(), 0);
                    animation.setInterpolator(new FastOutSlowInInterpolator());
                    animation.setDuration(500);
                    animation.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //notify home fragment for image show
                            adapter.onAnimationEnd();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animation.setStartDelay(startoff);
                    animation.start();

                    logo.animate()
                            .translationY(-viewPager.getHeight())
                            .setDuration(500)
                            .setStartDelay(startoff)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    logo.setVisibility(View.GONE);
                                }
                            });
                    ObjectAnimator panimation = ObjectAnimator.ofFloat(panel, "translationY", panel.getHeight(), 0);
                    panimation.setInterpolator(new FastOutSlowInInterpolator());
                    panimation.setStartDelay(startoff);
                    panimation.setDuration(500);
                    panimation.start();
                }
            });
        }else{
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    logo.setVisibility(View.GONE);
                    if(adapter!= null)
                        adapter.onAnimationEnd();
                }
            });
        }

        //check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
        }

        //action bar
        getSupportActionBar().setTitle("");

        //viewPager
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(1);
        adapter = new MainFragmentAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        viewPager.setCurrentItem(1,false);
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        int statusBarHeight = getStatusBarHeight(context);
        int ci;

        if(windowInsetsCompat.getDisplayCutout() == null) ci = 0;
        else ci = windowInsetsCompat.getDisplayCutout().getSafeInsetTop();

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)logo.getLayoutParams();
        params.setMargins(0,ci > statusBarHeight ? ci : statusBarHeight,0,0);
        logo.setLayoutParams(params);
        return super.onApplyWindowInsets(view, windowInsetsCompat);
    }

    @Override
    public void onBackPressed() {
        switch(adapter.onBackPressed(viewPager.getCurrentItem())){
            case BACK_NONE:
                break;
            case BACK_HOME:
                viewPager.setCurrentItem(1);
                break;
            case BACK_NORMAL:
                super.onBackPressed();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}