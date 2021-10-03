package io.github.junheah.jsp.activity;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import io.github.junheah.jsp.animation.ZoomOutPageTransformer;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.MainFragmentAdapter;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.Song;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.github.junheah.jsp.Utils.deleteSongPopup;
import static io.github.junheah.jsp.Utils.getNavigationBarHeight;
import static io.github.junheah.jsp.Utils.getStatusBarHeight;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_HOME;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NONE;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NORMAL;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;


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
        setContentView(R.layout.content_activity_main);
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
        int navBarHeight = getNavigationBarHeight(context);
        int cb = windowInsetsCompat.getSystemWindowInsetBottom();

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)logo.getLayoutParams();

        params.setMargins(0,Math.max(cb, navBarHeight),0,0);
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