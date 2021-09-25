package io.github.junheah.jsp.service;

import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_CREATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.Song;

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
            System.out.println("service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            player = null;
            System.out.println("service unbound");
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
