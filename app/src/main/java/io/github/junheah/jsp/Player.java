package io.github.junheah.jsp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class Player extends Service {
    public static final String ACTION_PLAYER_CREATE = "jsp.player_create";
    public static final String ACTION_PLAYER_CHECK = "jsp.player_check";
    public static final String ACTION_PLAYER_STOP = "jsp.player_stop";
    public static final String ACTION_PLAYER_START = "jsp.player_start";
    public static final String ACTION_PLAYER_PAUSE = "jsp.player_pause";

    public static boolean running = false;
    private final IBinder binder = new PlayerBinder();



    public Player() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch(intent.getAction()){
            case ACTION_PLAYER_CHECK:
                break;
            case ACTION_PLAYER_CREATE:
                break;
            case ACTION_PLAYER_START:
                break;
            case ACTION_PLAYER_STOP:
                break;
            case ACTION_PLAYER_PAUSE:
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class PlayerBinder extends Binder{
        public Player getService(){
            return Player.this;
        }
    }
}
