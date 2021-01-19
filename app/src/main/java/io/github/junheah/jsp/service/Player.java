package io.github.junheah.jsp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.interfaces.BitmapCallback;
import io.github.junheah.jsp.interfaces.PlayListChangeCallback;
import io.github.junheah.jsp.interfaces.ScriptCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerIntent;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_CONNECTING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static io.github.junheah.jsp.MainApplication.defaultCover;

public class Player extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAYER_CREATE = "jsp.player_create";
    public static final String ACTION_PLAYER_CHECK = "jsp.player_check";
    public static final String ACTION_PLAYER_STOP = "jsp.player_stop";
    public static final String ACTION_PLAYER_START = "jsp.player_start";
    public static final String ACTION_PLAYER_PAUSE = "jsp.player_pause";
    public static final String ACTION_PLAYER_NEXT = "jsp.player_next";
    public static final String ACTION_PLAYER_PREV = "jsp.player_prev";
    public static final String ACTION_PLAYER_APPEND = "jsp.player_append";
    public static final String ACTION_PLAYER_BROADCAST = "jsp.player_broadcast";
    public static final String ACTION_PLAYER_CREATED = "jsp.player_created";

    private static final String CHANNEL_ID = "jsp.media_player_service";
    public static final int nid = 31525694;

    public static boolean running = false;  //used to check if player is created or not
    // (액티비티에서 서비스 생성을 명령한 시점부터 액티비티가 서비스가 생성되었음을 인지할때까지 다른 인스턴스를 만드는 일을 방지)
    PlayerStatus status;
    PlayList playList;
    Song current;
    Bitmap currentCover;
    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;
    Intent pendingIntent;
    final IBinder binder = new PlayerBinder();

    PlayListChangeCallback playListChangeCallback;
    AudioManager audioManager;
    AudioAttributes audioAttr;
    MediaSessionCompat session;
    MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            if(status.loaded)
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
            System.out.println(action);
        }
    };

    //todo: https://developer.android.com/guide/topics/media-apps/working-with-a-media-session

    public Player() {
        super();
    }

    public Song getCurrent(){
        return this.current;
    }

    public PlayList getPlayList(){
        return this.playList;
    }

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

    public PlayerStatus getStatus(){
        return this.status;
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
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        session.setCallback(sessionCallback);
        setState(STATE_NONE);
        session.setActive(true);


        mediaPlayerInit();
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, getApplication().getPackageName());
        wifiLock.acquire();
        pendingIntent = new Intent();
        status = new PlayerStatus();
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
                if(current.hashCode() == song.hashCode()){
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
        Intent notificationIntent = new Intent(getApplicationContext(), io.github.junheah.jsp.activity.MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
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


        if(current != null) {
            notification.setContentTitle(current.getName());
            notification.setContentText(current.getArtist());

            //set album art
            currentCover = current.getCover();
            if(currentCover == null) {
                notification.setLargeIcon(defaultCover);
                current.loadCover(this, new BitmapCallback() {
                    @Override
                    public void resourceLoaded(Bitmap bitmap) {
                        currentCover = bitmap;
                        showNotification();
                    }
                });
            }else{
                notification.setLargeIcon(currentCover);
            }
        }

        notification.addAction(new NotificationCompat.Action(R.drawable.player_prev, "",MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        notification.addAction(new NotificationCompat.Action(status.loaded && mediaPlayer!=null && mediaPlayer.isPlaying() ? R.drawable.player_pause : R.drawable.player_start, "", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
        notification.addAction(new NotificationCompat.Action(R.drawable.player_next, "", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        notification.addAction(new NotificationCompat.Action(R.drawable.player_stop, "", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)));

        notification.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0,1,2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP)));


        startForeground(nid, notification.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            stop();
        MediaButtonReceiver.handleIntent(session, intent);
        switch (intent.getAction()) {
            case ACTION_PLAYER_CHECK:
                broadcast();
                break;
            case ACTION_PLAYER_CREATE:
                //sendBroadcast(new Intent(ACTION_PLAYER_CREATED));
                broadcast();
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

        this.playList = playList;

        //set callback to new playlist
        this.playList.setPlayListChangeCallback(playListChangeCallback);

        if(this.playList != null && this.playList.size()>0){
            this.current = this.playList.get(0);
        }

        play();
    }

    public void setPlayList(PlayList playList, Song song){
        //remove callback from previous playlist
        if(this.playList != null){
            this.playList.setPlayListChangeCallback(null);
        }

        this.playList = playList;

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
        if(current.getNext() != null){
            current = current.getNext();
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
        }else if(current.getPrev() != null){
            current = current.getPrev();
            play();
            return true;
        }else {
            broadcast();
            return false;
        }
    }

    public void pause(){
        if(status.loaded) {
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
        if(audioManager!=null)
            audioManager.abandonAudioFocus(this);
        if(playList != null) playList.setPlayListChangeCallback(null);
        running = false;
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSelf();
    }

    public void play(){
        status.loaded = false;
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
                ((ExternalSong)current).fetch(getApplicationContext(), new ScriptCallback() {
                    @Override
                    public void callback(Object res) {
                        try {
                            if(mediaPlayer == null) {
                                mediaPlayerInit();
                                mediaPlayer.setDataSource(((ExternalSong) current).getPath());
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
                mediaPlayer.setDataSource(getApplicationContext(), current.getUri());
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
            status.loaded = true;
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
            if (status.loaded) {
                status.playing = mediaPlayer.isPlaying();
                status.duration = mediaPlayer.getDuration();
            }else {
                status.duration = 0;
                status.playing = false;
            }
            intent.putExtra("status", new Gson().toJson(status));
            sendBroadcast(intent);
            showNotification();
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
    };

}
