package io.github.junhea.mul.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import static io.github.junhea.mul.Utils.dpToPx;

public class ClipLinearLayout extends LinearLayoutCompat {


    Path clipPath = new Path();
    int radii = 0;

    public ClipLinearLayout(@NonNull Context context) {
        super(context);
        this.radii = dpToPx(context, 30);
    }

    public ClipLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.radii = dpToPx(context, 30);
    }

    public ClipLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.radii = dpToPx(context, 30);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clipPath.reset();
        clipPath.addRoundRect(new RectF(0f,0f,w,h), radii, radii, Path.Direction.CW);
        clipPath.addRect(new RectF(0f, h/2, w, h),Path.Direction.CW);
        clipPath.close();
    }
}
