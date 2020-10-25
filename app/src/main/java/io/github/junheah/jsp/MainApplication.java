package io.github.junheah.jsp;

import android.app.Application;
import android.content.Context;


import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.STACK_TRACE;

@AcraMailSender(mailTo = "junheah@gmail.com")
@AcraCore(reportContent = { APP_VERSION_NAME, ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, REPORT_ID})
@AcraDialog(resText=R.string.acra_dialog_text)

public class MainApplication extends Application {
    public static Preference p;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("main app start");
        ACRA.init(this);
        p = new Preference(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("main app oncreate");

    }


}
