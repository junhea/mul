package io.github.junheah.jsp.Animation2;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

public class MarginPageTransformer implements ViewPager2.PageTransformer {
    private static final float SCALE = 0.95f;
    private static final int MARGIN = 8;

    public void transformPage(View view, float position) {
        int pagerWidth = view.getWidth();
        int pageWidth = Math.round(SCALE*pagerWidth);

        int offsetPx = pagerWidth - MARGIN - pageWidth;

        float scaleFactor = SCALE - (Math.abs(position)*0.1f);
        view.setTranslationX(position*-offsetPx);


        // Scale the page down (between MIN_SCALE and 1)
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);

    }
}
