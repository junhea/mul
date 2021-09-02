package io.github.junheah.jsp.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import io.github.junheah.jsp.R;

public class NowPlayingIcon {
    static AnimatedVectorDrawableCompat d;
    public static AnimatedVectorDrawableCompat getInstance(Context context){
        if(d == null) {
            d = AnimatedVectorDrawableCompat.create(context, R.drawable.nowplaying);
            d.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    super.onAnimationEnd(drawable);
                    d.start();
                }
            });
        }
        return d;
    }
}
