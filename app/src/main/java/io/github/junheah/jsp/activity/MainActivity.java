package io.github.junheah.jsp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.github.junheah.jsp.animation.ZoomOutPageTransformer;
import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.SongDataParser;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.MainFragmentAdapter;
import io.github.junheah.jsp.fragment.HomeFragment;
import io.github.junheah.jsp.fragment.PlayListFragment;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.github.junheah.jsp.MainApplication.playListIO;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Utils.YesNoPopup;

public class MainActivity extends AppCompatActivity {

    Context context;
    ImageButton pausebtn, nextbtn, prevbtn, mini_pausebtn;
    TextView name, artist, timestamp_cur, timestamp_dur, mini_name, mini_artist;
    ProgressBar mini_progress;
    SeekBar seekBar;
    Player player;
    boolean bound = false;
    PlayerStatus status;
    boolean seekbarTouch = false;
    ViewPager2 viewPager;
    MainFragmentAdapter adapter;
    PlayListItemClickCallback playListCallback;
    PlayList playListQueue;
    Song songQueue;
    Thread timeStampThread;
    Toolbar toolbar;
    Runnable onPlayerConnected;
    SlidingUpPanelLayout.PanelSlideListener portraitPanelListener, landscapePanelListener;
    int playerOriginalHeight, miniPlayerCoverOriginal, miniPlayerCoverMax, screenWidth;
    SlidingUpPanelLayout panel;
    View playerControl;
    ImageView miniPlayerCover;
    SongDataParser parser;
    public final static int PERMISSION_CODE = 14245;
    Song current;


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

            if(onPlayerConnected != null){
                onPlayerConnected.run();
                onPlayerConnected = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            System.out.println("service unbound");
            resetPlayer();
            timeStampThread.interrupt();
            timeStampThread = null;
        }
    };

    public void addSong(SongPlayListParcel parcel) {
        if(parcel.song instanceof ExternalSong){
            parcel.playList.add(parcel.song);
        }else {
            if (parser == null || !parser.running) {
                parser = new SongDataParser(this);
                parser.execute(parcel);
            } else {
                parser.add(parcel);
            }
        }
    }

    public void addSong(String name, Song song){
        //external song adder
        //check if playlist is currently loaded, if true, add via playlist.add if not, add through playlist io,
    }

    public void calculateDimensions(boolean portrait){
        playerOriginalHeight = Math.round(68 * getResources().getDisplayMetrics().density);
        miniPlayerCoverOriginal = Math.round(50 * getResources().getDisplayMetrics().density);
        if(portrait)
            miniPlayerCoverMax = Resources.getSystem().getDisplayMetrics().widthPixels - Math.round(20 * getResources().getDisplayMetrics().density);
        else
            miniPlayerCoverMax = Resources.getSystem().getDisplayMetrics().heightPixels - Math.round(20 * getResources().getDisplayMetrics().density);
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private void resetPlayer(){
        //reset all player controls
        toggleButtons(false);
        player = null;
        bound = false;
        miniPlayerCover.setImageResource(R.drawable.music);
    }

    private void reloadPlayerControls(boolean portrait) {
        //called on orientation change
        name = this.findViewById(portrait ? R.id.playerSongName : R.id.playerSongName_landscape);
        artist = this.findViewById(portrait ? R.id.playerArtistName : R.id.playerArtistName_landscape);
        seekBar = this.findViewById(portrait ? R.id.seekBar : R.id.seekBar_landscape);
        timestamp_cur = this.findViewById(portrait ? R.id.timestamp_current : R.id.timestamp_current_landscape);
        timestamp_dur = this.findViewById(portrait ? R.id.timestamp_duration : R.id.timestamp_duration_landscape);
        nextbtn = this.findViewById(portrait ? R.id.next_btn : R.id.next_btn_landscape);
        prevbtn = this.findViewById(portrait ? R.id.prev_btn : R.id.prev_btn_landscape);
        pausebtn = this.findViewById(portrait ? R.id.pause_btn : R.id.pause_btn_landscape);


        //listeners
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bound) {
                    player.next();
                }
            }
        });

        prevbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bound) {
                    player.prev();
                }
            }
        });

        View.OnClickListener pauseListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bound) {
                    player.pause();
                }
            }
        };

        pausebtn.setOnClickListener(pauseListener);
        mini_pausebtn.setOnClickListener(pauseListener);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
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
                if (bound)
                    player.seekTo(seekBar.getProgress());
            }
        });

        if(playerControl != null) {
            playerControl.setVisibility(View.GONE);
            panel.removePanelSlideListener(portrait ? landscapePanelListener : portraitPanelListener);
        }

        playerControl = this.findViewById(portrait ? R.id.playerControl : R.id.landscape_playerControl);
        playerControl.setAlpha(0.0f);
        playerControl.setVisibility(View.VISIBLE);

        panel.addPanelSlideListener(portrait ? portraitPanelListener : landscapePanelListener);

        if(panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            if(portrait)
                portraitPanelListener.onPanelSlide(panel,1f);
            else
                landscapePanelListener.onPanelSlide(panel, 1f);
        }

        if(bound){
            player.broadcast();
        }else{
            toggleButtons(false);
        }
    }

    public PlayListItemClickCallback getPlayListCallback() {
        return this.playListCallback;
    }

    public Player getPlayer(){
        return this.player;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        timestamp_cur = this.findViewById(R.id.timestamp_current);
        timestamp_dur = this.findViewById(R.id.timestamp_duration);
        seekBar = this.findViewById(R.id.seekBar);

        //check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
        }

        //action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //orientation
        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        //sliding up panel
        panel = this.findViewById(R.id.panel);
        View miniPlayer = this.findViewById(R.id.mini_player);
        View miniPlayerInfoContainer = this.findViewById(R.id.mini_infoContainer);
        ImageButton miniPlayerPlaybtn = this.findViewById(R.id.mini_pause_btn);
        miniPlayerCover = this.findViewById(R.id.mini_cover);
        mini_pausebtn = this.findViewById(R.id.mini_pause_btn);
        mini_name = this.findViewById(R.id.mini_name);
        mini_artist = this.findViewById(R.id.mini_artist);
        mini_progress = this.findViewById(R.id.mini_progress);
        if(portrait) {
            this.findViewById(R.id.landscape_playerControl).setVisibility(View.GONE);
            playerControl = this.findViewById(R.id.playerControl);
        }else {
            this.findViewById(R.id.playerControl).setVisibility(View.GONE);
            playerControl = this.findViewById(R.id.landscape_playerControl);
        }
        playerControl.setVisibility(View.VISIBLE);
        calculateDimensions(portrait);
        portraitPanelListener = new SlidingUpPanelLayout.PanelSlideListener() {
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
                int width = miniPlayerCoverOriginal +
                        Math.round((miniPlayerCoverMax - miniPlayerCoverOriginal)*slideOffset);
                ViewGroup.LayoutParams paramss = miniPlayerCover.getLayoutParams();
                paramss.height = width;
                paramss.width = width;
                miniPlayerCover.setLayoutParams(paramss);
                //player controls
                playerControl.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED && bound) player.broadcast();
            }
        };
        landscapePanelListener = new SlidingUpPanelLayout.PanelSlideListener() {
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
                int width = miniPlayerCoverOriginal +
                        Math.round((miniPlayerCoverMax - miniPlayerCoverOriginal)*slideOffset);
                ViewGroup.LayoutParams paramss = miniPlayerCover.getLayoutParams();
                paramss.height = width;
                paramss.width = width;
                miniPlayerCover.setLayoutParams(paramss);

                //player controls
                playerControl.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED && bound) player.broadcast();
            }
        };
        if(portrait) {
            panel.addPanelSlideListener(portraitPanelListener);
        }else {
            panel.addPanelSlideListener(landscapePanelListener);
        }
        reloadPlayerControls(portrait);

        //playlist callback
        playListCallback = new PlayListItemClickCallback() {
            @Override
            public void SongClicked(Song song, PlayList list) {
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

                                //save
                                playListIO.write(list);
                            }
                        });
            }
        };

        //broadcast receiver
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                //if broadcast received, player is running
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

                    if (bound) {
                        current = player.getCurrent();
                        if (current == null) {
                            //no song loaded
                            nextbtn.setEnabled(false);
                            prevbtn.setEnabled(false);
                            pausebtn.setEnabled(false);
                            miniPlayerCover.setImageResource(R.drawable.music);
                        } else {
                            //notify current to fragments
                            if(adapter != null)
                                adapter.callback(current);

                            if (current.getNext() == null) nextbtn.setEnabled(false);
                            else nextbtn.setEnabled(true);
                            if (current.getPrev() == null) prevbtn.setEnabled(false);
                            else prevbtn.setEnabled(true);

                            name.setText(current.getName());
                            mini_name.setText(current.getName());
                            artist.setText(current.getArtist());
                            mini_artist.setText(current.getArtist());

                            try {
                                if (current instanceof ExternalSong) {
                                    String url = ((ExternalSong) current).getCoverUrl();
                                    if (url != null && url.length() > 0)
                                        System.out.println(url);
                                    Glide.with(getApplicationContext())
                                            .load(url)
                                            .dontTransform()
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .placeholder(R.drawable.music)
                                            .into(miniPlayerCover);
                                } else {
                                    Glide.with(getApplicationContext())
                                            .load(new AudioCoverModel(current.getPath()))
                                            .dontTransform()
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .placeholder(R.drawable.music)
                                            .fallback(R.drawable.music)
                                            .into(miniPlayerCover);
                                }
                            }catch (Exception e){

                            }

                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        registerReceiver(receiver, filter);

        //viewPager
        viewPager = this.findViewById(R.id.viewPager);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(1);
        adapter = new MainFragmentAdapter(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if(positionStart > 0){
                    viewPager.setCurrentItem(positionStart-1,false);
                }
            }
        });
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        //add home fragment
        adapter.append(HomeFragment.newInstance());

        //load playlists
        onPlayerConnected = new Runnable() {
            @Override
            public void run() {
                PlayListFragment tmpfrag;
                for(String key : playListIO.getNames()){
                    if(bound && player.getPlayList().getName().equals(key)){
                        System.out.println("restore from player");
                        tmpfrag = PlayListFragment.newInstance(player.getPlayList());
                    }else {
                        tmpfrag = PlayListFragment.newInstance(key);
                    }
                    adapter.append(tmpfrag);
                }
            }
        };
        if(!Player.running){
            onPlayerConnected.run();
            onPlayerConnected = null;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        calculateDimensions(portrait);
        reloadPlayerControls(portrait);
    }

    void toggleButtons(ViewGroup group, boolean playerIsRunning){
        for(int i=0; i<group.getChildCount(); i++){
            View view = group.getChildAt(i);
            if(view instanceof Button){
                view.setEnabled(playerIsRunning);
            }else if(view instanceof SeekBar){
                if(!playerIsRunning) ((SeekBar) view).setProgress(0);
                view.setEnabled(playerIsRunning);
            }else if(view instanceof ImageButton){
                view.setEnabled(playerIsRunning);
            }else if(view instanceof ViewGroup){
                toggleButtons((ViewGroup) view, playerIsRunning);
            }else if(view instanceof ProgressBar){
                if(!playerIsRunning)((ProgressBar) view).setProgress(0);
            }else if(view instanceof TextView){
                if(!playerIsRunning) ((TextView) view).setText("");
            }
        }
    }

    void toggleButtons(boolean playerIsRunning){
        toggleButtons((ViewGroup) this.findViewById(R.id.player_panel), playerIsRunning);
    }

    public String getTimeStamp(int m){
        long second = (m / 1000) % 60;
        long minute = (m / (1000 * 60)) % 60;
        long hour = (m / (1000 * 60 * 60)) % 24;
        return(String.format("%02d:%02d:%02d", hour, minute, second));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unbind player service
        unbindService(connection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //bind service
        resetPlayer();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Song getCurrent(){
        return this.current;
    }
}