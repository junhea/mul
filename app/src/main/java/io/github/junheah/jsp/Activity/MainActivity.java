package io.github.junheah.jsp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.Song;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CHECK;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_PAUSE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_STOP;

public class MainActivity extends AppCompatActivity {

    boolean bound = false;
    Player player;
    PlayerStatus status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //debug
        PlayList playList = new PlayList();
        playList.add(new Song("test", "http://utaitebox.com/api/play/stream/v6MoDvMxyp"));

        //check service
        Intent player = new Intent(getApplicationContext(), Player.class);
        player.setAction(ACTION_PLAYER_CREATE);
        player.putExtra("playlist", new Gson().toJson(playList));
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

        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                status = new Gson().fromJson(intent.getStringExtra("status"), new TypeToken<PlayerStatus>(){}.getType());
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        registerReceiver(receiver, filter);
    }


    private void startPlayer(Intent intent){
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }
}