package io.github.junheah.jsp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;

public class Player extends Service {
    public static final String ACTION_PLAYER_CREATE = "jsp.player_create";
    public static final String ACTION_PLAYER_CHECK = "jsp.player_check";
    public static final String ACTION_PLAYER_STOP = "jsp.player_stop";
    public static final String ACTION_PLAYER_START = "jsp.player_start";
    public static final String ACTION_PLAYER_PAUSE = "jsp.player_pause";
    public static final String ACTION_PLAYER_BROADCAST = "jsp.player_broadcast";

    public static boolean running = false;
    PlayerStatus status;
    PlayList playList;



    public Player() {
        super();
        status = new PlayerStatus();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch(intent.getAction()){
            case ACTION_PLAYER_CHECK:
                break;
            case ACTION_PLAYER_CREATE:
                playList = new Gson().fromJson(intent.getStringExtra("playlist"),  new TypeToken<PlayList>(){}.getType());
                break;
            case ACTION_PLAYER_START:
                //play

                break;
            case ACTION_PLAYER_STOP:
                //stop
                break;
            case ACTION_PLAYER_PAUSE:
                //pause
                break;
        }
        broadcast();
        return START_STICKY;
    }

    public void play(){

    }



    public void broadcast(){
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYER_BROADCAST);
        intent.putExtra("status", new Gson().toJson(status));
        sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
