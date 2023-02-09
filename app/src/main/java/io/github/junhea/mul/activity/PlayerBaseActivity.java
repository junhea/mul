package io.github.junhea.mul.activity;

import static io.github.junhea.mul.Preference.pointColor;
import static io.github.junhea.mul.Utils.getStatusBarHeight;
import static io.github.junhea.mul.Utils.getTimeStamp;
import static io.github.junhea.mul.Utils.toggleButtons;
import static io.github.junhea.mul.model.PlayList.MODE_REPEAT_ALL;
import static io.github.junhea.mul.model.PlayList.MODE_REPEAT_SONG;
import static io.github.junhea.mul.model.PlayList.MODE_SHUFFLE;
import static io.github.junhea.mul.service.Player.ACTION_PLAYER_BROADCAST;
import static io.github.junhea.mul.service.Player.ACTION_PLAYER_EXIT;
import static io.github.junhea.mul.service.PlayerServiceHandler.bound;
import static io.github.junhea.mul.service.PlayerServiceHandler.player;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.R;
import io.github.junhea.mul.TimerThread;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.PlayerStatus;
import io.github.junhea.mul.model.glide.AudioCoverModel;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.Song;
import io.github.junhea.mul.service.Player;
import io.github.junhea.mul.service.PlayerServiceHandler;

public class PlayerBaseActivity extends AppCompatActivity implements OnApplyWindowInsetsListener, TimerThread.TimerCallback {
    /*
    Base for every activity that contains player panel
     */

    Context context;
    ImageButton pausebtn, nextbtn, prevbtn, mini_pausebtn, shufflebtn, repeatbtn;
    TextView name, artist, timestamp_cur, timestamp_dur, mini_name, mini_artist;
    ProgressBar mini_progress;
    SeekBar seekBar;
    boolean seekbarTouch = false;
    Song current;
    PlayList playList;
    BroadcastReceiver receiver;
    PlayListIO playListIO;
    View miniPlayerInfoContainer, miniPlayer, activity;
    ImageButton miniPlayerPlaybtn;
    ViewSwitcher viewSwitcher;
    float miniCoverHeight;
    RequestListener<Bitmap> requestListener;
    static int color = -1;
    SlidingUpPanelLayout.PanelSlideListener panelListener;
    SlidingUpPanelLayout panel;
    View panelContent;
    ImageView miniPlayerCover;
    Toolbar toolbar;
    FrameLayout frame;
    int base_layout_id = R.layout.activity_player_base;
    View[] views;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        context = this;
        if(color == -1 || !bound){
            color = ContextCompat.getColor(context, R.color.colorDarkWindowBackground);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(base_layout_id);
        frame = this.findViewById(R.id.player_base_activity_content);
        frame.addView(getLayoutInflater().inflate(layoutResID, null, false));
    }

    public void panelOnCreate(){
        context = this;
        playListIO = PlayListIO.getInstance(context);
        timestamp_cur = this.findViewById(R.id.timestamp_current);
        timestamp_dur = this.findViewById(R.id.timestamp_duration);
        seekBar = this.findViewById(R.id.seekBar);
        panel = this.findViewById(R.id.panel);
        activity = this.findViewById(R.id.player_base_activity_content);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        //hide player
        panel.setPanelHeight(0);
        panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        //action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //orientation
        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

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

        //broadcast receiver
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                //if broadcast received, player is running
                if(intent.getAction().equals(Player.ACTION_PLAYER_BROADCAST)) {
                    togglePlayerButtons(true);

                    //if not bound, bind to service
                    if (!bound) {
                        PlayerServiceHandler.bind();
                    }

                    //update ui
                    updatePanel();
                }else if(intent.getAction().equals(Player.ACTION_PLAYER_EXIT)){
                    //disconnect if bound
                    if(bound){
                        PlayerServiceHandler.unbind();
                    }
                    resetPlayer();
                }
            }
        };

        panel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                panel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //set initial
                miniCoverHeight = miniPlayerCover.getMeasuredHeight();
                refreshPanel();
            }
        });
        if (Build.VERSION.SDK_INT >= 21) {
            ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView().getRootView(), this);
        }else{
            //set top padding to statusbar height
            activity.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    activity.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    activity.setPadding(0,getStatusBarHeight(context),0,0);
                }
            });
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
                    if(reloadColorOnResume) setColorPalette(p);
                }
                return false;
            }
        };


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

    private void reloadPlayerControls(View container, boolean portrait) {
        PlayerStatus.forceUpdate = true;

        miniPlayer = container.findViewById(R.id.mini_player);
        miniPlayerInfoContainer = container.findViewById(R.id.mini_infoContainer);
        miniPlayerPlaybtn = container.findViewById(R.id.mini_pause_btn);
        miniPlayerCover = container.findViewById(R.id.mini_cover);
        mini_pausebtn = container.findViewById(R.id.mini_pause_btn);
        mini_name = container.findViewById(R.id.mini_name);
        mini_artist = container.findViewById(R.id.mini_artist);
        mini_progress = container.findViewById(R.id.mini_progress);
        mini_progress.setBackgroundColor(Color.TRANSPARENT);
        mini_progress.getProgressDrawable().setColorFilter(pointColor, android.graphics.PorterDuff.Mode.SRC_IN);
        panelContent = container.findViewById(R.id.panel_content);

        //called on orientation change
        name = container.findViewById(R.id.playerSongName);
        artist = container.findViewById(R.id.playerArtistName);
        seekBar = container.findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(pointColor, android.graphics.PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(pointColor, android.graphics.PorterDuff.Mode.SRC_IN);
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
            togglePlayerButtons(false);
        }
    }

    void togglePlayerButtons(boolean playerIsRunning){
        toggleButtons((ViewGroup) viewSwitcher.getCurrentView(), playerIsRunning);
        toggleButtons((ViewGroup) viewSwitcher.getNextView(), playerIsRunning);
    }

    private void resetPlayer(){
        notifySongChanged(null, null);
        playList = null;
        current = null;
        //hide player
        panel.setPanelHeight(0);
        panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        //reset color palette
        setColorPalette(null);
        //reset all player controls
        togglePlayerButtons(false);
        player = null;
        bound = false;
        miniPlayerCover.setImageResource(R.drawable.music);
    }

    public void setColorPalette(Palette p){
        if(views == null)
            views = new View[]{panelContent, getWindow().getDecorView(), activity};
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
        if(views == null)
            views = new View[]{panelContent, getWindow().getDecorView(), activity};
        for (View v : views) {
            v.setBackgroundColor(c);
        }
    }


    public void updatePanel(){
        if (PlayerStatus.playing) {
            pausebtn.setImageResource(R.drawable.player_pause);
            mini_pausebtn.setImageResource(R.drawable.player_pause);
        } else {
            pausebtn.setImageResource(R.drawable.player_start);
            mini_pausebtn.setImageResource(R.drawable.player_start);
        }

        //update ui depending on status.loaded
        pausebtn.setEnabled(PlayerStatus.loaded);
        seekBar.setEnabled(PlayerStatus.loaded);

        if (!PlayerStatus.loaded) {
            seekBar.setProgress(0);
            mini_progress.setProgress(0);
            timestamp_cur.setText("");
            timestamp_dur.setText("");
        }else{
            timestamp_cur.setText(getTimeStamp(PlayerStatus.current));
            timestamp_dur.setText(getTimeStamp(PlayerStatus.duration));
            seekBar.setMax(PlayerStatus.duration);
            mini_progress.setMax(PlayerStatus.duration);
        }

        if (bound) {
            //if now playing is new : update text (song name, artist, total duration)
            if(current != PlayerStatus.song || playList != PlayerStatus.playList || PlayerStatus.forceUpdate) {

                PlayerStatus.forceUpdate = false;

                current = PlayerStatus.song;
                playList = PlayerStatus.playList;

                //notify current to fragments
                notifySongChanged(playList, current);

                //show player
                panel.setPanelHeight((int) getResources().getDimension(R.dimen.panel_height));
                if(panel.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED)
                    panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbind player service
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAYER_BROADCAST);
        filter.addAction(ACTION_PLAYER_EXIT);
        registerReceiver(receiver, filter);
    }

    boolean reloadColorOnResume = true;

    @Override
    protected void onResume() {
        super.onResume();
        //refresh
        if(bound){
            player.broadcast();
        }else{
            resetPlayer();
        }
        //color
        if(reloadColorOnResume)
            setColor(color);

        //timer tick
        TimerThread.init(this);
    }

    @Override
    public void tick(int t) {
        String timestamp = getTimeStamp(t);
        if (!prevbtn.isEnabled() && t >= 3000) prevbtn.setEnabled(true);
        if(!seekbarTouch) {
            timestamp_cur.setText(timestamp);
            seekBar.setProgress(t);
        }
        mini_progress.setProgress(t);
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
        //This is where you get DisplayCutoutCompat
        int ci = Math.max(getStatusBarHeight(context), windowInsetsCompat.getSystemWindowInsetTop());
        activity.setPadding(0, ci,0,0);
        view.setPadding(windowInsetsCompat.getStableInsetLeft(),0,windowInsetsCompat.getStableInsetRight(),windowInsetsCompat.getStableInsetBottom());
        return windowInsetsCompat;
    }


    public void notifySongChanged(PlayList playList, Song song){
        //custom notifier actions here

    }
}
