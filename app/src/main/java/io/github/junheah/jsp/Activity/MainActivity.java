package io.github.junheah.jsp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.Song;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_PAUSE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;

public class MainActivity extends AppCompatActivity {

    Button playbtn, pausebtn;
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
        playbtn = this.findViewById(R.id.play_btn);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent player = new Intent(getApplicationContext(), Player.class);
                player.setAction(ACTION_PLAYER_START);
                startPlayer(player);
            }
        });

        pausebtn = this.findViewById(R.id.pause_btn);
        pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent player = new Intent(getApplicationContext(), Player.class);
                player.setAction(ACTION_PLAYER_PAUSE);
                startPlayer(player);
            }
        });

        //broadcast receiver

        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                status = new Gson().fromJson(intent.getStringExtra("status"), new TypeToken<PlayerStatus>(){}.getType());
                if(status.isPlaying()){
                    pausebtn.setText("pause");
                }else{
                    pausebtn.setText("resume");
                }
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