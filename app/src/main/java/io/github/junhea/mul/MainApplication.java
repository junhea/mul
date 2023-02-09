package io.github.junhea.mul;

import android.content.Context;
import android.graphics.Bitmap;


import androidx.multidex.MultiDexApplication;

import io.github.junhea.mul.model.Library;
import io.github.junhea.mul.service.PlayerServiceHandler;

import static io.github.junhea.mul.Utils.getBaseScript;
import static io.github.junhea.mul.Utils.getBitmapFromVectorDrawable;

public class MainApplication extends MultiDexApplication {
    public static HttpClient client;
    public static Bitmap defaultCover;
    public static String baseScript;
    public static Library library;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PlayerServiceHandler.init(this);
        Preference.reload(this);
    }

    @Override
    public void onCreate() {
        this.client = new HttpClient();
        //load basescript
        this.defaultCover = getBitmapFromVectorDrawable(this, R.drawable.music_dark);
        this.baseScript = getBaseScript(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                library = new Library(MainApplication.this);
                MainApplication.super.onCreate();
            }
        }).start();
    }
}
