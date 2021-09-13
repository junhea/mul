package io.github.junheah.jsp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.animation.ZoomOutPageTransformer;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.SongDataParser;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.MainFragmentAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.Song;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_HOME;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NONE;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NORMAL;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Utils.YesNoPopup;

public class MainActivity extends AppCompatActivity {

    Context context;
    ImageButton pausebtn, nextbtn, prevbtn, mini_pausebtn;
    TextView name, artist, timestamp_cur, timestamp_dur, mini_name, mini_artist;
    ProgressBar mini_progress;
    SeekBar seekBar;
    private static Player player;
    boolean bound = false;
    PlayerStatus status;
    boolean seekbarTouch = false;
    ViewPager2 viewPager;
    MainFragmentAdapter adapter;
    PlayListItemClickCallback playListCallback;
    PlayList playListQueue;
    Song songQueue;
    Toolbar toolbar;
    Runnable onPlayerConnected;
    SlidingUpPanelLayout.PanelSlideListener panelListener;
    int playerOriginalHeight, miniPlayerCoverOriginal, miniPlayerCoverMax, screenWidth;
    SlidingUpPanelLayout panel;
    View playerControl;
    ImageView miniPlayerCover;
    SongDataParser parser;
    public final static int PERMISSION_CODE = 14245;
    Song current;
    PlayList playList;
    BroadcastReceiver receiver;
    PlayListIO playListIO;
    IntentFilter filter;
    View miniPlayerInfoContainer, miniPlayer;
    ImageButton miniPlayerPlaybtn;
    ViewSwitcher viewSwitcher;


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
            Handler timestampHandler = new Handler(Looper.getMainLooper());
            final Runnable tr = new Runnable() {
                @Override
                public void run() {
                    if (bound && player != null) {
                        status = player.getStatus();
                        if (status != null && status.playing && status.loaded && !seekbarTouch) {
                            int t = player.getCurrentPosition();
                            if (!prevbtn.isEnabled() && t > 3000)
                                prevbtn.setEnabled(true);
                            String timestamp = getTimeStamp(t);
                            timestamp_cur.setText(timestamp);
                            seekBar.setProgress(t);
                            mini_progress.setProgress(t);
                        }
                        timestampHandler.postDelayed(this, 60);
                    }
                }
            };
            timestampHandler.postDelayed(tr,100);


            if(onPlayerConnected != null){
                onPlayerConnected.run();
                onPlayerConnected = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            System.out.println("service unbound");
            resetPlayer();
            //notify current to fragments
            if(adapter != null)
                adapter.notify("", null);
        }
    };

    public synchronized void addSong(SongPlayListParcel parcel) {
        if(parcel.songs.get(0) instanceof ExternalSong){
            for(Song s : parcel.songs) {
                parcel.playList.add(s);
                s.setSid(SongDatabase.getInstance(context).externalDao().insert((ExternalSong) s));
            }
        }else {
            if (parser == null || !parser.running) {
                parser = new SongDataParser(this);
                parser.execute(parcel);
            } else {
                parser.add(parcel);
            }
        }
    }

    public void calculateDimensions(boolean portrait){
        playerOriginalHeight = Math.round(68 * getResources().getDisplayMetrics().density);
        miniPlayerCoverOriginal = Math.round(50 * getResources().getDisplayMetrics().density);
        if(portrait)
            miniPlayerCoverMax = Resources.getSystem().getDisplayMetrics().widthPixels - Math.round(20 * getResources().getDisplayMetrics().density);
        else
            miniPlayerCoverMax = Resources.getSystem().getDisplayMetrics().heightPixels - Math.round(40 * getResources().getDisplayMetrics().density);
        screenWidth = portrait ? Resources.getSystem().getDisplayMetrics().widthPixels : Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private void resetPlayer(){
        //reset all player controls
        toggleButtons(false);
        player = null;
        bound = false;
        miniPlayerCover.setImageResource(R.drawable.music);
    }

    private void reloadPlayerControls(View container, boolean portrait) {

        miniPlayer = container.findViewById(R.id.mini_player);
        miniPlayerInfoContainer = container.findViewById(R.id.mini_infoContainer);
        miniPlayerPlaybtn = container.findViewById(R.id.mini_pause_btn);
        miniPlayerCover = container.findViewById(R.id.mini_cover);
        mini_pausebtn = container.findViewById(R.id.mini_pause_btn);
        mini_name = container.findViewById(R.id.mini_name);
        mini_artist = container.findViewById(R.id.mini_artist);
        mini_progress = container.findViewById(R.id.mini_progress);
        playerControl = container.findViewById(R.id.playerControl);

        //called on orientation change
        name = container.findViewById(R.id.playerSongName);
        artist = container.findViewById(R.id.playerArtistName);
        seekBar = container.findViewById(R.id.seekBar);
        timestamp_cur = container.findViewById(R.id.timestamp_current);
        timestamp_dur = container.findViewById(R.id.timestamp_duration);
        nextbtn = container.findViewById(R.id.next_btn);
        prevbtn = container.findViewById(R.id.prev_btn);
        pausebtn = container.findViewById(R.id.pause_btn);


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

        playerControl.setAlpha(0.0f);
        playerControl.setVisibility(View.VISIBLE);

        if(panelListener != null) {
            panel.addPanelSlideListener(panelListener);

            if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                panelListener.onPanelSlide(panel, 1f);
            }
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

    public static synchronized Player getPlayer(){
        return player;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        playListIO = PlayListIO.getInstance(context);

        timestamp_cur = this.findViewById(R.id.timestamp_current);
        timestamp_dur = this.findViewById(R.id.timestamp_duration);
        seekBar = this.findViewById(R.id.seekBar);
        panel = this.findViewById(R.id.panel);

        viewPager = this.findViewById(R.id.viewPager);

        //reveal animation
        ImageView logo = this.findViewById(R.id.logo);
        if(savedInstanceState == null) {
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ObjectAnimator animation = ObjectAnimator.ofFloat(viewPager, "translationY", viewPager.getHeight(), 0);
                    animation.setInterpolator(new FastOutSlowInInterpolator());
                    animation.setDuration(500);
                    animation.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //notify home fragment for image show
                            adapter.onAnimationEnd();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animation.start();
                    logo.animate()
                            .translationY(-viewPager.getHeight())
                            .setDuration(500)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    logo.setVisibility(View.GONE);
                                }
                            });
                    ObjectAnimator panimation = ObjectAnimator.ofFloat(panel, "translationY", panel.getHeight(), 0);
                    panimation.setInterpolator(new FastOutSlowInInterpolator());
                    panimation.setDuration(500);
                    panimation.start();
                }
            });
        }else{
            logo.setVisibility(View.GONE);
            adapter.onAnimationEnd();
        }



        //check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
        }

        //action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //orientation
        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        calculateDimensions(portrait);
        //sliding up panel

        viewSwitcher = this.findViewById(R.id.viewSwitcher);
        if(!portrait)
            viewSwitcher.showNext();

        reloadPlayerControls(viewSwitcher.getCurrentView(), portrait);
        System.out.println("ppppp"+miniPlayerCover.getScaleX());


        panelListener = new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                System.out.println(slideOffset);
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
//        landscapePanelListener = new SlidingUpPanelLayout.PanelSlideListener() {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//                ViewGroup.LayoutParams params = miniPlayer.getLayoutParams();
//                int height = playerOriginalHeight+Math.round((screenWidth-playerOriginalHeight)*slideOffset);
//                miniPlayer.getLayoutParams().height = height;
//                miniPlayer.setLayoutParams(params);
//
//                //info container
//                miniPlayerInfoContainer.setAlpha(1-slideOffset*5);
//
//                //play button
//                miniPlayerPlaybtn.setAlpha(1-slideOffset*5);
//
//                //cover image
//                int width = miniPlayerCoverOriginal +
//                        Math.round((miniPlayerCoverMax - miniPlayerCoverOriginal)*slideOffset);
//                ViewGroup.LayoutParams paramss = miniPlayerCover.getLayoutParams();
//                paramss.height = width;
//                paramss.width = width;
//                miniPlayerCover.setLayoutParams(paramss);
//
//                //player controls
//                playerControl.setAlpha(slideOffset);
//            }
//
//            @Override
//            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
//                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED && bound) player.broadcast();
//            }
//        };
        panel.addPanelSlideListener(panelListener);
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panelListener.onPanelSlide(panel, 1f);
        }

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
        receiver = new BroadcastReceiver() {
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
                        playList = player.getPlayList();
                        //notify current to fragments
                        if(adapter != null)
                            adapter.notify(playList.getName(), current);
                        if (current == null) {
                            //no song loaded
                            nextbtn.setEnabled(false);
                            prevbtn.setEnabled(false);
                            pausebtn.setEnabled(false);
                            miniPlayerCover.setImageResource(R.drawable.music);
                        } else {
                            if (playList.getNext(current) == null) nextbtn.setEnabled(false);
                            else nextbtn.setEnabled(true);
                            if (playList.getPrev(current) == null) prevbtn.setEnabled(false);
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
                                    Glide.with(miniPlayerCover)
                                            .load(url)
                                            .dontTransform()
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .placeholder(R.drawable.music)
                                            .into(miniPlayerCover);
                                } else {
                                    Glide.with(miniPlayerCover)
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
        filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);


        //viewPager
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(1);
        adapter = new MainFragmentAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        viewPager.setCurrentItem(1,false);

        if(onPlayerConnected != null){
            onPlayerConnected.run();
            onPlayerConnected = null;
        }
    }

    @Override
    public void onBackPressed() {
        switch(adapter.onBackPressed(viewPager.getCurrentItem())){
            case BACK_NONE:
                break;
            case BACK_HOME:
                viewPager.setCurrentItem(1);
                break;
            case BACK_NORMAL:
                super.onBackPressed();
                break;
        }
    }




    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        calculateDimensions(portrait);
        viewSwitcher.showNext();
        reloadPlayerControls(viewSwitcher.getCurrentView(), portrait);
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
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //bind service
        resetPlayer();
        bindService(new Intent(context, Player.class), connection, Context.BIND_ADJUST_WITH_ACTIVITY);
        registerReceiver(receiver, filter);
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





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Song getCurrent(){
        return this.current;
    }
}