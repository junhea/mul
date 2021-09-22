package io.github.junheah.jsp;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;


import androidx.multidex.MultiDexApplication;


import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import io.github.junheah.jsp.model.Library;
import io.github.junheah.jsp.service.PlayerServiceHandler;

import static io.github.junheah.jsp.Utils.getBaseScript;
import static io.github.junheah.jsp.Utils.getBitmapFromVectorDrawable;
import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.STACK_TRACE;


@AcraMailSender(mailTo = "junheah@gmail.com")
@AcraCore(reportContent = { APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, REPORT_ID})
@AcraDialog(resText=R.string.acra_dialog_text)

public class MainApplication extends MultiDexApplication {
    public static HttpClient client;
    public static Bitmap defaultCover;

    public static String baseScript;
    public static Library library;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("main app start");
        ACRA.init(this);
        PlayerServiceHandler.init(this);
    }

    @Override
    public void onCreate() {
        System.out.println("main app oncreate");
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
