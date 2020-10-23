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
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CHECK;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_STOP;

public class MainActivity extends AppCompatActivity {

    Context context;
    Button pausebtn, nextbtn, prevbtn, stopbtn, playbtn;
    TextView infotext;
    Player player;
    PlayerStatus status;
    boolean bound = false;
    private ServiceConnection connection = new ServiceConnection() {

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
            System.out.println("service unbound");
            infotext.setText("");
            toggleButtons(false);
            player = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        //play btn
        playbtn = this.findViewById(R.id.play_btn);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) {
                    player.play();
                }
                else{
                    startPlayer(ACTION_PLAYER_START);
                }
            }
        });

        //stop btn
        stopbtn = this.findViewById(R.id.stop_btn);
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) {
                    player.stop();
                }
            }
        });

        //next btn
        nextbtn = this.findViewById(R.id.next_btn);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) {
                    player.next();
                }
            }
        });

        //prev btn
        prevbtn = this.findViewById(R.id.prev_btn);
        prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) {
                    player.prev();
                }
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
                //if broadcast recieved, player is running
                toggleButtons(true);

                //if not bound, bind to service
                if(!bound){
                    bindService(new Intent(context, Player.class), connection, Context.BIND_ADJUST_WITH_ACTIVITY);
                }

                //update status
                status = new Gson().fromJson(intent.getStringExtra("status"), new TypeToken<PlayerStatus>(){}.getType());

                //update ui
                if(status.playing){
                    pausebtn.setText("||");
                }else{
                    pausebtn.setText(">");
                }

                pausebtn.setEnabled(status.loaded);

                //get info directly from bound service
                if(bound) {
                    Song current = player.getCurrent();
                    if(current == null) {
                        //current is null
                        nextbtn.setEnabled(false);
                        prevbtn.setEnabled(false);
                        pausebtn.setEnabled(false);
                    }else{
                        if (current.getNext() == null)
                            nextbtn.setEnabled(false);
                        else
                            nextbtn.setEnabled(true);

                        if (current.getPrev() == null)
                            prevbtn.setEnabled(false);
                        else
                            prevbtn.setEnabled(true);

                        infotext.setText(current.getName());
                    }
                }

                //debug
                if(bound && player.getCurrent() == null){
                    PlayList playList = new PlayList();
                    playList.add(new Song("kimi", "http://utaitebox.com/api/play/stream/v6MoDvMxyp"));
                    playList.add(new Song("asu", "http://utaitebox.com/api/play/stream/AoYIokv0dG"));
                    playList.add(new Song("koe", "http://utaitebox.com/api/play/stream/t5pEquO2Om"));
                    playList.add(new Song("sugar", "http://utaitebox.com/api/play/stream/E39T7bT1Xq"));
                    player.setPlayList(playList);
                    player.play();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        registerReceiver(receiver, filter);

        toggleButtons(false);
    }

    void toggleButtons(boolean playerIsRunning){
        prevbtn.setEnabled(false);
        nextbtn.setEnabled(false);
        stopbtn.setEnabled(playerIsRunning);
        pausebtn.setEnabled(false);
        playbtn.setEnabled(!playerIsRunning);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(context, Player.class), connection, Context.BIND_ADJUST_WITH_ACTIVITY);
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