package io.github.junhea.mul.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.AudioManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import io.github.junhea.mul.R;
import io.github.junhea.mul.interfaces.PlayListChangeCallback;
import io.github.junhea.mul.interfaces.ScriptCallback;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.PlayerStatus;
import io.github.junhea.mul.model.glide.AudioCoverModel;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_CONNECTING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static io.github.junhea.mul.MainApplication.defaultCover;
import static io.github.junhea.mul.model.PlayList.MODE_NORMAL;
import static io.github.junhea.mul.model.PlayList.MODE_REPEAT_ALL;
import static io.github.junhea.mul.model.PlayList.MODE_REPEAT_SONG;
import static io.github.junhea.mul.model.PlayList.MODE_SHUFFLE;

public class Player extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAYER_CREATE = "mul.player_create";
    public static final String ACTION_PLAYER_CHECK = "mul.player_check";
    public static final String ACTION_PLAYER_STOP = "mul.player_stop";
    public static final String ACTION_PLAYER_START = "mul.player_start";
    public static final String ACTION_PLAYER_PAUSE = "mul.player_pause";
    public static final String ACTION_PLAYER_NEXT = "mul.player_next";
    public static final String ACTION_PLAYER_PREV = "mul.player_prev";
    public static final String ACTION_PLAYER_APPEND = "mul.player_append";
    public static final String ACTION_PLAYER_BROADCAST = "mul.player_broadcast";
    public static final String ACTION_PLAYER_CREATED = "mul.player_created";
    public static final String ACTION_PLAYER_EXIT = "mul.player_exit";

    private static final String CHANNEL_ID = "mul.media_player_service";
    public static final int nid = 23542341;

    public static boolean running = false;  //used to check if player is created or not
    // (액티비티에서 서비스 생성을 명령한 시점부터 액티비티가 서비스가 생성되었음을 인지할때까지 다른 인스턴스를 만드는 일을 방지)
    PlayList playList;
    Song current;
    Bitmap currentCover;
    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;
    Intent pendingIntent;
    final IBinder binder = new PlayerBinder();
    short mode = MODE_NORMAL;

    PlayListChangeCallback playListChangeCallback;
    AudioManager audioManager;
    AudioAttributes audioAttr;
    MediaSessionCompat session;
    MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            if(PlayerStatus.loaded)
                pause();
            else
                play();
        }

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onSkipToNext() {
            next();
        }

        @Override
        public void onSkipToPrevious() {
            prev();
        }

        @Override
        public void onStop() {
            stop();
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            //System.out.println(action);
        }
    };

    public Player() {
        super();
    }

//    public Song getCurrent(){
//        return this.current;
//    }
//
//    public PlayList getPlayList(){
//        return this.playList;
//    }

    public int getCurrentPosition(){
        if(mediaPlayer != null)
            try {
                return mediaPlayer.getCurrentPosition();
            }catch (Exception e){
                return 0;
            }
        else
            return 0;
    }

    void setState(int state){
        PlaybackStateCompat playBackstate = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();
        session.setPlaybackState(playBackstate);
    }

    void setMetaData(Song song){
        MediaMetadataCompat data = new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, defaultCover)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                .build();
        session.setMetadata(data);
    }

    public void toggleShuffle(){
        short mode = playList.getMode();
        if(mode != MODE_SHUFFLE){
            playList.setMode(MODE_SHUFFLE,current);
            this.mode = MODE_SHUFFLE;
        }else{
            playList.setMode(MODE_NORMAL,current);
            this.mode = MODE_NORMAL;
        }
        broadcast();
    }

    public void toggleRepeat(){
        short mode = playList.getMode();
        switch (mode){
            case MODE_REPEAT_ALL:
                playList.setMode(MODE_NORMAL,current);
                this.mode = MODE_NORMAL;
                break;
            case MODE_REPEAT_SONG:
                playList.setMode(MODE_REPEAT_ALL,current);
                this.mode = MODE_REPEAT_ALL;
                break;
            case MODE_NORMAL:
            case MODE_SHUFFLE:
                playList.setMode(MODE_REPEAT_SONG,current);
                this.mode = MODE_REPEAT_SONG;
                break;
        }
        broadcast();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        running = true;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
        }



        session = new MediaSessionCompat(this, getPackageName());
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        session.setCallback(sessionCallback);
        setState(STATE_NONE);
        session.setActive(true);


        mediaPlayerInit();
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, getApplication().getPackageName());
        wifiLock.acquire();
        pendingIntent = new Intent();
        playListChangeCallback = new PlayListChangeCallback() {
            @Override
            public void playListRemoved() {
                stop();
            }

            @Override
            public void playListUpdated() {
                broadcast();
                showNotification();
            }

            @Override
            public void songRemoved(Song song) {
                if(current.equals(song)){
                    stop();
                }else{
                    this.playListUpdated();
                }
            }
        };
        showNotification();
    }
    void mediaPlayerInit(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }
    void showNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), io.github.junhea.mul.activity.MainActivity.class);
        int intentFlag = 0;
        if(Build.VERSION.SDK_INT >= 31){
            intentFlag = PendingIntent.FLAG_MUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, intentFlag);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mchannel = new NotificationChannel(CHANNEL_ID, "media player", NotificationManager.IMPORTANCE_LOW);
            mchannel.setDescription("media player");
            mchannel.enableLights(false);
            mchannel.enableVibration(false);
            mchannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(mchannel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.music_note)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP));

        notification.addAction(new NotificationCompat.Action(R.drawable.player_prev, "",MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        notification.addAction(new NotificationCompat.Action(PlayerStatus.loaded && mediaPlayer!=null && mediaPlayer.isPlaying() ? R.drawable.player_pause : R.drawable.player_start, "", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
        notification.addAction(new NotificationCompat.Action(R.drawable.player_next, "", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        notification.addAction(new NotificationCompat.Action(R.drawable.player_stop, "", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)));

        notification.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0,1,2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP)));


        if(current != null) {
            notification.setContentTitle(current.getName());
            notification.setContentText(current.getArtist());

            //set album art
            if(currentCover == null) {
                notification.setLargeIcon(defaultCover);
                if (current instanceof ExternalSong) {
                    String url = ((ExternalSong)current).getCoverUrl();
                    if (url != null && url.length() > 0)
                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(url)
                                .placeholder(R.drawable.music_dark)
                                .fallback(R.drawable.music_dark)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap res, Transition<? super Bitmap> t) {
                                        notification.setLargeIcon(res);
                                        startForeground(nid, notification.build());
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                } else {
                    if(!((LocalSong)current).nocover) {
                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(new AudioCoverModel(current.getPath()))
                                .dontTransform()
                                .placeholder(R.drawable.music_dark)
                                .fallback(R.drawable.music_dark)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap res, Transition<? super Bitmap> t) {
                                        notification.setLargeIcon(res);
                                        startForeground(nid, notification.build());
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                    }
                }
            }else{
                notification.setLargeIcon(currentCover);
            }
        }
        startForeground(nid, notification.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            stop();
            return START_STICKY;
        }
        MediaButtonReceiver.handleIntent(session, intent);
        switch (intent.getAction()) {
            case ACTION_PLAYER_CHECK:
                broadcast();
                break;
            case ACTION_PLAYER_CREATE:
                setPlayList(PlayerStatus.playList, PlayerStatus.song);
                break;
            case ACTION_PLAYER_START:
                play();
                break;
            case ACTION_PLAYER_STOP:
                stop();
                break;
            case ACTION_PLAYER_PAUSE:
                pause();
                break;
            case ACTION_PLAYER_NEXT:
                next();
                break;
            case ACTION_PLAYER_PREV:
                prev();
                break;

        }
        return START_STICKY;
    }

    public void setPlayList(PlayList playList){
        //remove callback from previous playlist
        if(this.playList != null){
            this.playList.setPlayListChangeCallback(null);
        }
        if(playList != null){
            this.playList = playList;

            //set callback to new playlist
            this.playList.setPlayListChangeCallback(playListChangeCallback);

            if(this.playList.size()>0){
                this.current = this.playList.get(0);
            }

            this.playList.setMode(this.mode, current);

            play();
        }


    }

    public void setPlayList(PlayList playList, Song song){
        //remove callback from previous playlist
        if(this.playList != null){
            this.playList.setPlayListChangeCallback(null);
        }

        this.playList = playList;

        this.playList.setMode(this.mode, song);

        //set callback to new playlist
        this.playList.setPlayListChangeCallback(playListChangeCallback);

        this.current = song;
        play();
    }

    public void seekTo(int pos){
        mediaPlayer.seekTo(pos);
        broadcast();
    }

    public boolean next(){
        if(playList.getNext(current) != null){
            current = playList.getNext(current);
            currentCover = null;
            play();
            return true;
        }else {
            broadcast();
            return false;
        }
    }

    public boolean prev(){
        if(mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
            seekTo(0);
            broadcast();
            return true;
        }else if(playList.getPrev(current) != null){
            current = playList.getPrev(current);
            currentCover = null;
            play();
            return true;
        }else {
            broadcast();
            return false;
        }
    }

    public void pause(){
        if(PlayerStatus.loaded) {
            if (mediaPlayer.isPlaying()) {
                setState(STATE_PAUSED);
                mediaPlayer.pause();
            } else {
                requestFocusAndPlay();
            }
            broadcast();
        }
    }

    public void stop(){
        PlayerStatus.reset();
        if(audioManager!=null)
            audioManager.abandonAudioFocus(this);
        if(playList != null){
            playList.setPlayListChangeCallback(null);
            playList = null;
        }

        current = null;
        running = false;
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        stopSelf();
        broadcast();
    }

    static int tmpidx = 0;

    public void play(){
        PlayerStatus.loaded = false;
        try {
            if(mediaPlayer == null){
                mediaPlayerInit();
            }
            mediaPlayer.reset();
            if(current instanceof ExternalSong) {
                //stop media player
                mediaPlayer.release();
                mediaPlayer = null;
                setState(STATE_CONNECTING);
                ((ExternalSong)current).fetch(getBaseContext(), new ScriptCallback() {
                    @Override
                    public void callback(Object res) {
                        try {
                            if(mediaPlayer == null) {
                                mediaPlayerInit();
                                mediaPlayer.setDataSource(current.getPath().toString());
                                mediaPlayer.prepareAsync();
                                setState(STATE_BUFFERING);
                                setMetaData(current);
                                broadcast();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }else {
                mediaPlayer.release();
                mediaPlayer = null;
                mediaPlayerInit();
                setState(STATE_NONE);
                mediaPlayer.setDataSource(getApplicationContext(), current.getPath());
                mediaPlayer.prepareAsync();
                setState(STATE_BUFFERING);
                setMetaData(current);
            }
        } catch (Exception e) {
            mediaPlayer.reset();
            e.printStackTrace();
        }
        broadcast();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if(running) {
            PlayerStatus.loaded = true;
            requestFocusAndPlay();
            broadcast();
        }
    }

    public void requestFocusAndPlay(){
        setState(STATE_PLAYING);
        int focus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttr)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            focus = audioManager.requestAudioFocus(request);
        }else{
            focus = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        if(focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mediaPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(running) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            if(!next()) {
                broadcast();
            }
        }
    }

    @Override
    public void onAudioFocusChange(int i) {
        if(i == AudioManager.AUDIOFOCUS_LOSS){
            //lost focus
            if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                broadcast();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        stop();
        return true;
    }

    public void broadcast(){
        if(running) {
            Intent intent = new Intent(ACTION_PLAYER_BROADCAST);
            if (PlayerStatus.loaded) {
                PlayerStatus.playing = mediaPlayer.isPlaying();
                PlayerStatus.duration = mediaPlayer.getDuration();
                PlayerStatus.current = mediaPlayer.getCurrentPosition();
            }else {
                PlayerStatus.playing = false;
                PlayerStatus.duration = 0;
                PlayerStatus.current = 0;
            }
            PlayerStatus.song = current;
            PlayerStatus.playList = playList;

            sendBroadcast(intent);
            showNotification();
        }else{
            sendBroadcast(new Intent(ACTION_PLAYER_EXIT));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (session != null)
            session.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class PlayerBinder extends Binder {
        public Player getService(){
            return Player.this;
        }
    }

}
