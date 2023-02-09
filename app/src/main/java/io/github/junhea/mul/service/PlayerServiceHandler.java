package io.github.junhea.mul.service;

import static io.github.junhea.mul.service.Player.ACTION_PLAYER_CREATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.PlayerStatus;
import io.github.junhea.mul.model.song.Song;

public class PlayerServiceHandler {
    public static boolean bound;
    public static Player player;
    static Context context;

    final static ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Player.PlayerBinder binder = (Player.PlayerBinder) service;
            player = binder.getService();
            player.broadcast();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            player = null;
        }
    };

    public static void init(Context c){
        context = c;
    }

    public static void bind(){
        context.bindService(new Intent(context, Player.class), connection, Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    public static void unbind(){
        context.unbindService(connection);
    }

    public static void play(Context context, PlayList list, Song song){
        if(bound){
            player.setPlayList(list, song);
        }else if(!Player.running){  //is player.running, wait for it to bind
            //set playlist when service connected
            PlayerStatus.song = song;
            PlayerStatus.playList = list;
            startPlayer(context, ACTION_PLAYER_CREATE);
        }
    }

    private static void startPlayer(Context context, String action){
        startPlayer(context, new Intent(context, Player.class), action);
    }

    private static void startPlayer(Context context, Intent intent, String action){
        intent.setAction(action);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        }else{
            context.startService(intent);
        }
    }
}
