package io.github.junheah.jsp;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("main app start");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("main app oncreate");
    }


}
