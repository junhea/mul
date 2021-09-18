package io.github.junheah.jsp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.github.junheah.jsp.PlayListIO;
import io.github.junheah.jsp.animation.ZoomOutPageTransformer;
import io.github.junheah.jsp.model.glide.AudioCoverModel;
import io.github.junheah.jsp.model.room.SongDatabase;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.SongDataParser;
import io.github.junheah.jsp.model.song.SongPlayListParcel;
import io.github.junheah.jsp.model.viewHolder.LibraryViewHolder;
import io.github.junheah.jsp.service.Player;
import io.github.junheah.jsp.R;
import io.github.junheah.jsp.adapter.MainFragmentAdapter;
import io.github.junheah.jsp.interfaces.PlayListItemClickCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.Song;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static io.github.junheah.jsp.MainApplication.library;
import static io.github.junheah.jsp.Utils.getPlayList;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_HOME;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NONE;
import static io.github.junheah.jsp.fragment.CustomFragment.BACK_NORMAL;
import static io.github.junheah.jsp.model.PlayList.MODE_NORMAL;
import static io.github.junheah.jsp.model.PlayList.MODE_REPEAT_ALL;
import static io.github.junheah.jsp.model.PlayList.MODE_REPEAT_SONG;
import static io.github.junheah.jsp.model.PlayList.MODE_SHUFFLE;
import static io.github.junheah.jsp.model.song.Song.EXTERNAL;
import static io.github.junheah.jsp.model.song.Song.LOCAL;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_CREATE;
import static io.github.junheah.jsp.Utils.YesNoPopup;
import static io.github.junheah.jsp.service.Player.ACTION_PLAYER_EXIT;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Context context;
    ImageButton pausebtn, nextbtn, prevbtn, mini_pausebtn, shufflebtn, repeatbtn;
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
    SlidingUpPanelLayout panel;
    View panelContent;
    ImageView miniPlayerCover;
    SongDataParser parser;
    public final static int PERMISSION_CODE = 14245;
    Song current;
    PlayList playList;
    BroadcastReceiver receiver;
    PlayListIO playListIO;
    IntentFilter filter;
    View miniPlayerInfoContainer, miniPlayer, activity;
    ImageButton miniPlayerPlaybtn;
    ViewSwitcher viewSwitcher;
    float miniCoverHeight;
    RequestListener<Bitmap> requestListener;
    int color;
    boolean forceUpdate = false;


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Player.PlayerBinder binder = (Player.PlayerBinder) service;
            player = binder.getService();
            player.broadcast();
            bound = true;
            System.out.println("service bound");
            panel.setPanelHeight((int)getResources().getDimension(R.dimen.panel_height));

            if(playListQueue != null){
                if(songQueue == null)
                    player.setPlayList(playListQueue);
                else
                    player.setPlayList(playListQueue, songQueue);
                playListQueue = null;
                songQueue = null;
            }
            //timestamp thread
            Handler uiHandler = new Handler(Looper.getMainLooper());
            new Thread(new Runnable() {
                int t = -1;
                String timestamp;
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    while(true) {
                        if (bound && player != null) {
                            status = player.getStatus();
                            if (status != null && status.playing && status.loaded && !seekbarTouch) {
                                int nt = player.getCurrentPosition();
                                if(nt/1000 != t/1000){
                                    t = nt;
                                    timestamp = getTimeStamp(t);
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!prevbtn.isEnabled() && t >= 3000) prevbtn.setEnabled(true);
                                            timestamp_cur.setText(timestamp);
                                            seekBar.setProgress(t);
                                            mini_progress.setProgress(t);
                                        }
                                    });
                                }
                            }
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                }
            }).start();

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
            playList = null;
            current = null;
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    private void resetPlayer(){
        //hide player
        panel.setPanelHeight(0);
        panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        //reset color palette
        setColorPalette(null);
        //reset all player controls
        toggleButtons(false);
        player = null;
        bound = false;
        miniPlayerCover.setImageResource(R.drawable.music);
    }

    public void setColorPalette(Palette p){
        View[] views = {panelContent, getWindow().getDecorView(), activity};
        int newcolor = ContextCompat.getColor(context, R.color.colorDarkWindowBackground);
        if(p != null){
            newcolor = p.getDarkMutedColor(ContextCompat.getColor(context, R.color.colorDarkWindowBackground));
        }
        if(newcolor != color) {
            for (View v : views) {
                ObjectAnimator.ofObject(v, "backgroundColor" /*view attribute name*/, new ArgbEvaluator(), color, newcolor)
                        .setDuration(500)
                        .start();
            }
            color = newcolor;
        }
    }

    public void setColor(int c){
        View[] views = {panelContent, getWindow().getDecorView(), activity};
        for (View v : views) {
            v.setBackgroundColor(c);
        }
    }

    private void reloadPlayerControls(View container, boolean portrait) {
        forceUpdate = true;

        miniPlayer = container.findViewById(R.id.mini_player);
        miniPlayerInfoContainer = container.findViewById(R.id.mini_infoContainer);
        miniPlayerPlaybtn = container.findViewById(R.id.mini_pause_btn);
        miniPlayerCover = container.findViewById(R.id.mini_cover);
        mini_pausebtn = container.findViewById(R.id.mini_pause_btn);
        mini_name = container.findViewById(R.id.mini_name);
        mini_artist = container.findViewById(R.id.mini_artist);
        mini_progress = container.findViewById(R.id.mini_progress);
        panelContent = container.findViewById(R.id.panel_content);

        //called on orientation change
        name = container.findViewById(R.id.playerSongName);
        artist = container.findViewById(R.id.playerArtistName);
        seekBar = container.findViewById(R.id.seekBar);
        timestamp_cur = container.findViewById(R.id.timestamp_current);
        timestamp_dur = container.findViewById(R.id.timestamp_duration);
        nextbtn = container.findViewById(R.id.next_btn);
        prevbtn = container.findViewById(R.id.prev_btn);
        pausebtn = container.findViewById(R.id.pause_btn);
        shufflebtn = container.findViewById(R.id.shuffle_btn);
        repeatbtn = container.findViewById(R.id.repeat_btn);

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

        shufflebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bound) {
                    player.toggleShuffle();
                }
            }
        });

        repeatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bound){
                    player.toggleRepeat();
                }
            }
        });

        miniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        container.findViewById(R.id.mini_cover_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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

//        playerControl.setAlpha(0.0f);
//        playerControl.setVisibility(View.VISIBLE);

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

    public void setTopPadding(int px){

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
        activity = this.findViewById(R.id.activity_content);

        viewPager = this.findViewById(R.id.viewPager);

        color = ContextCompat.getColor(context, R.color.colorDarkWindowBackground);

        //hide player
        panel.setPanelHeight(0);
        panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        //reveal animation
        ImageView logo = this.findViewById(R.id.logo);
        if(savedInstanceState == null) {
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //set initial
                    miniCoverHeight = miniPlayerCover.getMeasuredHeight();
                    refreshPanel();

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
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //set initial
                    miniCoverHeight = miniPlayerCover.getMeasuredHeight();
                    refreshPanel();

                    logo.setVisibility(View.GONE);
                    if(adapter!= null)
                        adapter.onAnimationEnd();
                }
            });
        }

        //fullscreen
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                //This is where you get DisplayCutoutCompat
                int statusBarHeight = getStatusBarHeight();
                int ci;
                if(windowInsetsCompat.getDisplayCutout() == null) ci = 0;
                else ci = windowInsetsCompat.getDisplayCutout().getSafeInsetTop();

                logo.setPadding(0,(ci > statusBarHeight ? ci : statusBarHeight)*2,0,0);

                activity.setPadding(0,ci > statusBarHeight ? ci : statusBarHeight,0,0);
                view.setPadding(windowInsetsCompat.getStableInsetLeft(),0,windowInsetsCompat.getStableInsetRight(),windowInsetsCompat.getStableInsetBottom());
                return windowInsetsCompat;
            }
        });



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

        //sliding up panel

        viewSwitcher = this.findViewById(R.id.viewSwitcher);
        if(!portrait)
            viewSwitcher.showNext();

        reloadPlayerControls(viewSwitcher.getCurrentView(), portrait);

        panelListener = new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                miniPlayer.setVisibility(slideOffset == 1 ? View.GONE : View.VISIBLE);
                miniPlayerPlaybtn.setVisibility(slideOffset == 1 ? View.GONE : View.VISIBLE);

                miniPlayer.setAlpha(1-slideOffset*5);

//                playerControl.setAlpha(slideOffset);
                miniPlayerCover.setAlpha(0.25f + slideOffset);

                miniPlayerCover.setTranslationY((-miniCoverHeight/2)*(1-slideOffset));
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED && bound)
                    player.broadcast();
                else if(newState == SlidingUpPanelLayout.PanelState.ANCHORED){
                    ((SlidingUpPanelLayout)panel).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            }
        };

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
                YesNoPopup(context, song.getName(), getString(R.string.prompt_remove_song_from_playlist),
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

            @Override
            public void SongLongClicked(Song song) {
                //delete song from library
                YesNoPopup(context, song.getName(), getString(R.string.prompt_remove_song_from_library),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //yes
                                long[] sid = {song instanceof LocalSong ? LOCAL : EXTERNAL, song.getSid()};
                                library.remove(song);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //apply to db
                                        if(song instanceof LocalSong)
                                            SongDatabase.getInstance(context).localDao().delete((LocalSong) song);
                                        else
                                            SongDatabase.getInstance(context).externalDao().delete((ExternalSong) song);

                                        //todo: apply to playlists
//                                        List<String> names = new ArrayList<>();
//                                        for(String name : playListIO.getNames()){
//                                            for(long[] i : playListIO.getids(name)){
//                                                if(Arrays.equals(i, sid))   //remove from pl
//                                                    playListIO
//                                            }
//                                        }
                                    }
                                }).start();
                            }
                        });
            }
        };

        //broadcast receiver
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                System.out.println("received!");
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

                    //update ui depending on status.loaded
                    pausebtn.setEnabled(status.loaded);
                    seekBar.setEnabled(status.loaded);

                    if (!status.loaded) {
                        seekBar.setProgress(0);
                        mini_progress.setProgress(0);
                        timestamp_cur.setText("");
                        timestamp_dur.setText("");
                    }else{
                        timestamp_cur.setText(getTimeStamp(0));
                        timestamp_dur.setText(getTimeStamp(status.duration));
                        seekBar.setMax(status.duration);
                        mini_progress.setMax(status.duration);
                    }


                    if (bound) {
                        //if now playing is new : update text (song name, artist, total duration)
                        if(current != player.getCurrent() || playList != player.getPlayList() || forceUpdate) {

                            forceUpdate = false;

                            current = player.getCurrent();
                            playList = player.getPlayList();
                            //notify current to fragments
                            if (adapter != null)
                                adapter.notify(playList == null ? null : playList.getName(), current);

                            //show player
                            panel.setPanelHeight((int) getResources().getDimension(R.dimen.panel_height));
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
                                            .asBitmap()
                                            .load(url)
                                            .dontTransform()
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .placeholder(R.drawable.music)
                                            .addListener(requestListener)
                                            .into(miniPlayerCover);
                                } else {
                                    Glide.with(miniPlayerCover)
                                            .asBitmap()
                                            .load(new AudioCoverModel(current.getPath()))
                                            .dontTransform()
                                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .placeholder(R.drawable.music)
                                            .fallback(R.drawable.music)
                                            .addListener(requestListener)
                                            .into(miniPlayerCover);
                                }
                            } catch (Exception e) {

                            }
                        }

                        if (playList.getMode() == MODE_SHUFFLE) {
                            shufflebtn.setImageResource(R.drawable.player_shuffle);
                            repeatbtn.setImageResource(R.drawable.player_repeat_off);
                        } else if (playList.getMode() == MODE_REPEAT_ALL) {
                            shufflebtn.setImageResource(R.drawable.player_shuffle_off);
                            repeatbtn.setImageResource(R.drawable.player_repeat);
                        } else if (playList.getMode() == MODE_REPEAT_SONG) {
                            shufflebtn.setImageResource(R.drawable.player_shuffle_off);
                            repeatbtn.setImageResource(R.drawable.player_repeat_song);
                        } else {
                            //normal
                            shufflebtn.setImageResource(R.drawable.player_shuffle_off);
                            repeatbtn.setImageResource(R.drawable.player_repeat_off);
                        }
                    }
                }else if(intent.getAction().equals(ACTION_PLAYER_EXIT)){
                    //disconnect if bound
                    if(bound){
                        unbindService(connection);
                    }
                }
            }
        };
        filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        filter.addAction(ACTION_PLAYER_EXIT);


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

        //palette
        requestListener = new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                setColorPalette(null);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                if (resource != null) {
                    Palette p = Palette.from(resource).generate();
                    setColorPalette(p);
                }
                return false;
            }
        };
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
        viewSwitcher.showNext();
        reloadPlayerControls(viewSwitcher.getCurrentView(), portrait);
        setColor(color);

        //set initial player state
        refreshPanel();
    }

    public void refreshPanel(){
        panelListener.onPanelSlide(panel, panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ? 1 : 0);
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
            }else if(view instanceof ImageView){
                if(!playerIsRunning) ((ImageView) view).setImageResource(R.drawable.music);
            }
        }
    }

    void toggleButtons(boolean playerIsRunning){
        toggleButtons((ViewGroup) viewSwitcher.getCurrentView(), playerIsRunning);
        toggleButtons((ViewGroup) viewSwitcher.getNextView(), playerIsRunning);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Song getCurrent(){
        return this.current;
    }
}