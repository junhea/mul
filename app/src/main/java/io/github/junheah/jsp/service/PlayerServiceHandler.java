package io.github.junheah.jsp.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayerStatus;

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
}
