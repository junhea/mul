package io.github.junheah.jsp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_CHECK;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;

public class MainActivity extends AppCompatActivity {

    boolean bound = false;
    Player player;


    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Player.PlayerBinder binder = (Player.PlayerBinder) iBinder;
            player = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //bind to service
        Intent intent = new Intent(this, Player.class);
        bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check service
        Intent player = new Intent(getApplicationContext(), Player.class);
        startPlayer(player);


        //play btn
        this.findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent player = new Intent(getApplicationContext(), Player.class);
                player.setAction(ACTION_PLAYER_START);
                startPlayer(player);
            }
        });

        //broadcast receiver

    }


    private void startPlayer(Intent intent){
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }
}