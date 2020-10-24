package io.github.junheah.jsp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.Song;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;

public class MainActivity extends AppCompatActivity {

    Context context;
    ImageButton pausebtn, nextbtn, prevbtn, mini_pausebtn;
    Button stopbtn, playbtn;
    TextView name, artist, timestamp_cur, timestamp_dur, mini_name, mini_artist;
    ProgressBar mini_progress;
    SeekBar seekBar;
    Player player;
    PlayerStatus status;
    boolean bound = false, seekbarTouch = false;

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
            name.setText("");
            artist.setText("");
            mini_name.setText("");
            mini_artist.setText("");
            toggleButtons(false);
            seekBar.setProgress(0);
            seekBar.setEnabled(false);
            timestamp_cur.setText("");
            timestamp_dur.setText("");
            player = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = this;

        timestamp_cur = this.findViewById(R.id.timestamp_current);
        timestamp_dur = this.findViewById(R.id.timestamp_duration);
        seekBar = this.findViewById(R.id.seekBar);

        //sliding up panel
        SlidingUpPanelLayout panel = this.findViewById(R.id.panel);
        LinearLayoutCompat miniPlayer = this.findViewById(R.id.mini_player);
        LinearLayoutCompat miniPlayerInfoContainer = this.findViewById(R.id.mini_infoContainer);
        ImageButton miniPlayerPlaybtn = this.findViewById(R.id.mini_pause_btn);
        ImageView miniPlayerCover = this.findViewById(R.id.mini_cover);
        ConstraintLayout playerControl = this.findViewById(R.id.playerControl);
        mini_progress = this.findViewById(R.id.mini_progress);
        int playerOriginalHeight = Math.round(68 * getResources().getDisplayMetrics().density);
        int miniPlayerCoverOriginalWidth = Math.round(50 * getResources().getDisplayMetrics().density);
        int miniPlayerCoverMaxWidth = Resources.getSystem().getDisplayMetrics().widthPixels - Math.round(20 * getResources().getDisplayMetrics().density);
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        panel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                ViewGroup.LayoutParams params = miniPlayer.getLayoutParams();
                int height = playerOriginalHeight+Math.round((screenWidth-playerOriginalHeight)*slideOffset);
                miniPlayer.getLayoutParams().height = height;
                miniPlayer.setLayoutParams(params);

                //info container
                miniPlayerInfoContainer.setAlpha(1-slideOffset*5);

                //play button
                miniPlayerPlaybtn.setAlpha(1-slideOffset*5);

                //cover image
                int width = miniPlayerCoverOriginalWidth +
                        Math.round((miniPlayerCoverMaxWidth - miniPlayerCoverOriginalWidth)*slideOffset);
                ViewGroup.LayoutParams paramss = miniPlayerCover.getLayoutParams();
                paramss.height = width;
                paramss.width = width;
                miniPlayerCover.setLayoutParams(paramss);
                //player controls
                playerControl.setAlpha(slideOffset);


            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
//                if(newState== SlidingUpPanelLayout.PanelState.EXPANDED) updatePlayer(playerCurrentSong);
            }
        });

        //play btn
        playbtn = this.findViewById(R.id.start_btn);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound) {
                    player.play();
                }else{
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
        View.OnClickListener pauseListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bound){
                    player.pause();
                }
            }
        };
        pausebtn = this.findViewById(R.id.pause_btn);
        pausebtn.setOnClickListener(pauseListener);
        mini_pausebtn = this.findViewById(R.id.mini_pause_btn);
        mini_pausebtn.setOnClickListener(pauseListener);

        //info text
        name = this.findViewById(R.id.playerSongName);
        artist = this.findViewById(R.id.playerArtistName);
        mini_name = this.findViewById(R.id.mini_name);
        mini_artist = this.findViewById(R.id.mini_artist);


        //seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    timestamp_cur.setText(getTimeStamp(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarTouch = false;
                if(bound)
                    player.seekTo(seekBar.getProgress());
            }
        });

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
                    pausebtn.setImageResource(R.drawable.player_pause);
                    mini_pausebtn.setImageResource(R.drawable.player_pause);
                }else{
                    pausebtn.setImageResource(R.drawable.player_start);
                    mini_pausebtn.setImageResource(R.drawable.player_start);
                }

                //manual seekbar & timestamp
                if(bound && !status.playing){
                    seekBar.setProgress(player.getCurrentPosition());
                    timestamp_cur.setText(getTimeStamp(player.getCurrentPosition()));
                }

                //update ui depending on status.loaded
                pausebtn.setEnabled(status.loaded);
                seekBar.setEnabled(status.loaded);

                if(status.loaded){
                    timestamp_dur.setText(getTimeStamp(status.duration));
                    seekBar.setMax(status.duration);
                    mini_progress.setMax(status.duration);
                }else{
                    seekBar.setProgress(0);
                    mini_progress.setProgress(0);
                    timestamp_cur.setText("");
                    timestamp_dur.setText("");
                }

                //get info directly from bound service
                if(bound) {
                    Song current = player.getCurrent();
                    if(current == null) {
                        //no song loaded
                        nextbtn.setEnabled(false);
                        prevbtn.setEnabled(false);
                        pausebtn.setEnabled(false);
                        miniPlayerCover.setImageResource(R.drawable.music);
                    }else{
                        if (current.getNext() == null) nextbtn.setEnabled(false);
                        else nextbtn.setEnabled(true);
                        if (current.getPrev() == null) prevbtn.setEnabled(false);
                        else prevbtn.setEnabled(true);

                        name.setText(current.getName());
                        mini_name.setText(current.getName());
                        artist.setText(current.getArtist());
                        mini_artist.setText(current.getArtist());
                    }
                }

                //debug
                if(bound && player.getCurrent() == null){
                    PlayList playList = new PlayList();
                    playList.add(new Song("kimi", "test", "http://utaitebox.com/api/play/stream/v6MoDvMxyp"));
                    playList.add(new Song("asu", "test", "http://utaitebox.com/api/play/stream/AoYIokv0dG"));
                    playList.add(new Song("koe", "test", "http://utaitebox.com/api/play/stream/t5pEquO2Om"));
                    playList.add(new Song("sugar", "test", "http://utaitebox.com/api/play/stream/E39T7bT1Xq"));
                    player.setPlayList(playList);
                    player.play();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        registerReceiver(receiver, filter);

        //timestamp thread
        new Thread(new TimeStampThread()).start();

        toggleButtons(false);
    }

    void toggleButtons(boolean playerIsRunning){
        seekBar.setEnabled(false);
        prevbtn.setEnabled(false);
        nextbtn.setEnabled(false);
        stopbtn.setEnabled(playerIsRunning);
        pausebtn.setEnabled(false);
        playbtn.setEnabled(!playerIsRunning);
    }

    public String getTimeStamp(int m){
        long second = (m / 1000) % 60;
        long minute = (m / (1000 * 60)) % 60;
        long hour = (m / (1000 * 60 * 60)) % 24;
        return(String.format("%02d:%02d:%02d", hour, minute, second));
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

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if(!prevbtn.isEnabled() && msg.arg1>3000)
                prevbtn.setEnabled(true);
            String timestamp = getTimeStamp(msg.arg1);
            timestamp_cur.setText(timestamp);
            seekBar.setProgress(msg.arg1);
            mini_progress.setProgress(msg.arg1);
        }
    };

    public class TimeStampThread implements Runnable{
        @Override
        public void run() {
            while(true){
                try {
                    Thread.sleep(10);
                }catch (Exception e){
                    return;
                }
                if(bound) {
                    status = player.getStatus();
                    if (status != null && status.playing && status.loaded && !seekbarTouch) {
                        Message msg = new Message();
                        msg.arg1 = player.getCurrentPosition();
                        handler.sendMessage(msg);
                    }
                }
            }
        }
    };
}