package io.github.junhea.mul.activity;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import io.github.junhea.mul.Preference;
import io.github.junhea.mul.animation.ZoomOutPageTransformer;
import io.github.junhea.mul.R;
import io.github.junhea.mul.adapter.MainFragmentAdapter;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.song.Song;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.github.junhea.mul.Utils.deleteSongPopup;
import static io.github.junhea.mul.activity.SettingsActivity.RESULT_NEED_RESTART;
import static io.github.junhea.mul.fragment.CustomFragment.BACK_HOME;
import static io.github.junhea.mul.fragment.CustomFragment.BACK_NONE;
import static io.github.junhea.mul.fragment.CustomFragment.BACK_NORMAL;


public class MainActivity extends PlayerBaseActivity {


    ViewPager2 viewPager;
    MainFragmentAdapter adapter;
    public final static int PERMISSION_CODE = 10382;
    int targetColor;


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

        if(savedInstanceState == null) {
            //initial color
            views = new View[]{panelContent, getWindow().getDecorView(), activity, findViewById(R.id.main_activity_background)};

            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int startoff = 0;
                    int duration = 500;

                    //set initial object position
                    viewPager.setTranslationY(viewPager.getHeight());
                    panel.setTranslationY(panel.getHeight());

                    //animate
                    ObjectAnimator animation = ObjectAnimator.ofFloat(viewPager, "translationY", viewPager.getHeight(), 0);
                    animation.setInterpolator(new DecelerateInterpolator(1.1f));
                    animation.setDuration(duration);

                    animation.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            reloadColorOnResume = false;
                            targetColor = color;
                            color = ContextCompat.getColor(context, R.color.colorDarkWindowBackground);
                            setColor(color);
                            for (View v : views) {
                                ObjectAnimator.ofObject(v, "backgroundColor", new ArgbEvaluator(), color, targetColor)
                                    .setDuration(duration)
                                    .start();
                            }
                            ObjectAnimator panimation = ObjectAnimator.ofFloat(panel, "translationY", panel.getHeight(), 0);
                            panimation.setInterpolator(new FastOutSlowInInterpolator());
                            panimation.setDuration(duration);
                            panimation.start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //notify home fragment for image show
                            adapter.onAnimationEnd();
                            color = targetColor;
                            setColor(color);
                            reloadColorOnResume = true;
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
                }
            });
        }else{
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if(adapter!= null)
                        adapter.onAnimationEnd();
                }
            });
        }

        //check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
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
        switch(resultCode){
            case RESULT_NEED_RESTART:
                Preference.reload(this);
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

}