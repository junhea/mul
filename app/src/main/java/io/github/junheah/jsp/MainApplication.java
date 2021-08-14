package io.github.junheah.jsp;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;


import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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

public class MainApplication extends Application {
    public static HttpClient client;
    public static Bitmap defaultCover;

    public static String baseScript;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("main app start");
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("main app oncreate");
        defaultCover = getBitmapFromVectorDrawable(this, R.drawable.music_dark);

        this.client = new HttpClient();
        //load basescript
        this.baseScript = getBaseScript(this);
    }


}
