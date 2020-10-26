package io.github.junheah.jsp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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

import io.github.junheah.jsp.Animation.ZoomOutPageTransformer;
import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.MainFragmentAdapter;
import io.github.junheah.jsp.fragment.PlayListFragment;
import io.github.junheah.jsp.gson.PlayListSerializer;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.Song;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Utils.YesNoPopup;
import static io.github.junheah.jsp.Utils.playListDeserializer;
import static io.github.junheah.jsp.Utils.playListSerializer;
import static io.github.junheah.jsp.Utils.singleInputPopup;
import static io.github.junheah.jsp.Utils.songAdderPopup;

public class MainActivity extends AppCompatActivity {

    Context context;
    ImageButton pausebtn, nextbtn, prevbtn, mini_pausebtn;
    TextView name, artist, timestamp_cur, timestamp_dur, mini_name, mini_artist;
    ProgressBar mini_progress;
    SeekBar seekBar;
    Player player;
    PlayerStatus status;
    boolean bound = false, seekbarTouch = false;
    ViewPager2 viewPager;
    MainFragmentAdapter adapter;
    PlayListItemClickCallback playListCallback;
    PlayList playListQueue;
    Song songQueue;
    Thread timeStampThread;
    PlayListIO playListIO;
    Toolbar toolbar;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Player.PlayerBinder binder = (Player.PlayerBinder) service;
            player = binder.getService();
            player.broadcast();
            bound = true;
            System.out.println("service bound");
            if(playListQueue != null){
                if(songQueue == null)
                    player.setPlayList(playListQueue);
                else
                    player.setPlayList(playListQueue, songQueue);
                playListQueue = null;
                songQueue = null;
            }
            //timestamp thread
            timeStampThread = new Thread(new TimeStampThread());
            timeStampThread.start();
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
            timeStampThread.interrupt();
            timeStampThread = null;
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

        //action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        //playlist callback
        playListCallback = new PlayListItemClickCallback() {
            @Override
            public void SongClicked(Song song, PlayList list) {
                System.out.println("song clicked!");
                if(bound){
                    player.setPlayList(list, song);
                }else if(!Player.running){  //is player.running, wait for it to bind
                    //set playlist when service connected
                    playListQueue = list;
                    songQueue = song;
                    startPlayer(ACTION_PLAYER_CREATE);
                }
            }

            @Override
            public void SongLongClicked(Song song, PlayList list) {
                //delete song
                //todo: add more options (use menu popup)
                YesNoPopup(context, song.getName(), "이 곡을 플레이리스트에서 삭제하겠습니까?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //yes
                                list.remove(song);
                                if(bound) {
                                    //check if player is playing target song
                                    if(player.getCurrent().equals(song)){
                                        player.stop();
                                    }else
                                        player.broadcast();
                                }
                                playListIO.write(list);
                            }
                        });
            }
        };

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
                if(intent.getAction().equals(ACTION_PLAYER_BROADCAST)) {
                    toggleButtons(true);

                    //if not bound, bind to service
                    if (!bound) {
                        bindService(new Intent(context, Player.class), connection, Context.BIND_ADJUST_WITH_ACTIVITY);
                    }

                    //update status
                    status = new Gson().fromJson(intent.getStringExtra("status"), new TypeToken<PlayerStatus>() {
                    }.getType());

                    //update ui
                    if (status.playing) {
                        pausebtn.setImageResource(R.drawable.player_pause);
                        mini_pausebtn.setImageResource(R.drawable.player_pause);
                    } else {
                        pausebtn.setImageResource(R.drawable.player_start);
                        mini_pausebtn.setImageResource(R.drawable.player_start);
                    }

                    //manual seekbar & timestamp
                    if (bound && !status.playing) {
                        seekBar.setProgress(player.getCurrentPosition());
                        timestamp_cur.setText(getTimeStamp(player.getCurrentPosition()));
                    }

                    //update ui depending on status.loaded
                    pausebtn.setEnabled(status.loaded);
                    seekBar.setEnabled(status.loaded);

                    if (status.loaded) {
                        timestamp_dur.setText(getTimeStamp(status.duration));
                        seekBar.setMax(status.duration);
                        mini_progress.setMax(status.duration);
                    } else {
                        seekBar.setProgress(0);
                        mini_progress.setProgress(0);
                        timestamp_cur.setText("");
                        timestamp_dur.setText("");
                    }

                    //get info directly from bound service
                    if (bound) {
                        Song current = player.getCurrent();
                        if (current == null) {
                            //no song loaded
                            nextbtn.setEnabled(false);
                            prevbtn.setEnabled(false);
                            pausebtn.setEnabled(false);
                            miniPlayerCover.setImageResource(R.drawable.music);
                        } else {
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
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        registerReceiver(receiver, filter);

        toggleButtons(false);

        //viewPager
        viewPager = this.findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(3);
        adapter = new MainFragmentAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        //load playlists
        playListIO = new PlayListIO(context);
        PlayListFragment tmpfrag;
        for(PlayList pl : playListIO.get()){
            tmpfrag = new PlayListFragment(pl, playListCallback);
            adapter.append(tmpfrag);
        }


    }

    void toggleButtons(boolean playerIsRunning){
        seekBar.setEnabled(false);
        prevbtn.setEnabled(false);
        nextbtn.setEnabled(false);
        pausebtn.setEnabled(false);
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
                    //interrupted
                    e.printStackTrace();
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