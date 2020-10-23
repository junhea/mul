package io.github.junheah.jsp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.Song;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_NEXT;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_PAUSE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_PREV;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_STOP;

public class MainActivity extends AppCompatActivity {

    Button pausebtn, nextbtn, prevbtn;
    TextView infotext;
    Player player;
    PlayerStatus status;
    boolean bound = false;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Player.PlayerBinder binder = (Player.PlayerBinder) service;
            player = binder.getService();

            //debug
            PlayList playList = new PlayList();
            playList.add(new Song("kimi", "http://utaitebox.com/api/play/stream/v6MoDvMxyp"));
            playList.add(new Song("asu", "http://utaitebox.com/api/play/stream/AoYIokv0dG"));
            playList.add(new Song("koe", "http://utaitebox.com/api/play/stream/t5pEquO2Om"));
            playList.add(new Song("sugar", "http://utaitebox.com/api/play/stream/E39T7bT1Xq"));
            player.setPlayList(playList);


            player.broadcast();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            player = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start service
        Intent intent = new Intent(getApplicationContext(), Player.class);
        startPlayer(intent, ACTION_PLAYER_CREATE);

        //play btn
        this.findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) player.play();
            }
        });

        //stop btn
        this.findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) player.stop();
            }
        });

        //next btn
        nextbtn = this.findViewById(R.id.next_btn);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) player.next();
            }
        });

        //prev btn
        prevbtn = this.findViewById(R.id.prev_btn);
        prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) player.prev();
            }
        });

        //pause btn
        pausebtn = this.findViewById(R.id.pause_btn);
        pausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) player.pause();
            }
        });

        //info text
        infotext = this.findViewById(R.id.info_text);

        //broadcast receiver
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                //update status
                status = new Gson().fromJson(intent.getStringExtra("status"), new TypeToken<PlayerStatus>(){}.getType());

                //update ui
                if(status.isPlaying()){
                    pausebtn.setText("||");
                }else{
                    pausebtn.setText(">");
                }


                if(bound) {
                    Song current = player.getCurrent();
                    if(current.getNext() == null)
                        nextbtn.setEnabled(false);
                    else
                        nextbtn.setEnabled(true);

                    if(current.getPrev() == null)
                        prevbtn.setEnabled(false);
                    else
                        prevbtn.setEnabled(true);

                    infotext.setText(current.getName());
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, Player.class);
        bindService(intent, connection, Context.BIND_ABOVE_CLIENT);
    }

    private void startPlayer(Intent intent, String action){
        intent.setAction(action);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }

    private void startPlayer(String action){
        startPlayer(new Intent(getApplicationContext(), Player.class), action);
    }



}