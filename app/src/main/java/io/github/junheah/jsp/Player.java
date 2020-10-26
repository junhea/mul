package io.github.junheah.jsp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import io.github.junheah.jsp.interfaces.PlayListChangeCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerIntent;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.song.Song;

public class Player extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
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
    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;
    Intent pendingIntent;
    final IBinder binder = new PlayerBinder();
    Bitmap defaultCover;
    PlayListChangeCallback playListChangeCallback;

    public Player() {
        super();
    }

    public Song getCurrent(){
        return this.current;
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

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, getApplication().getPackageName());
        wifiLock.acquire();
        defaultCover = BitmapFactory.decodeResource(this.getResources(), R.drawable.music);
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

    void showNotification(){
        Intent notificationIntent = new Intent(this, io.github.junheah.jsp.Activity.MainActivity.class);

        Bitmap largeIcon = null;

        Intent inext = new PlayerIntent(this, ACTION_PLAYER_NEXT);
        Intent iprev = new PlayerIntent(this, ACTION_PLAYER_PREV);
        Intent ipause = new PlayerIntent(this, ACTION_PLAYER_PAUSE);
        Intent istop = new PlayerIntent(this, ACTION_PLAYER_STOP);

        PendingIntent pnext = PendingIntent.getService(this, 0, inext, 0);
        PendingIntent pprev = PendingIntent.getService(this, 0, iprev, 0);
        PendingIntent ppause = PendingIntent.getService(this, 0, ipause, 0);
        PendingIntent pstop = PendingIntent.getService(this, 0, istop, 0);


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
                .setOngoing(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2));


        if(current != null) {
            notification.setContentTitle(current.getName());
            notification.setContentText(current.getArtist());
            //set album art
            if(current.getCover() == null){
                notification.setLargeIcon(defaultCover);
            }else{
                //todo implement this
                notification.setLargeIcon(defaultCover);
            }
        }

        notification.addAction(new NotificationCompat.Action(R.drawable.player_prev, "",pprev));
        notification.addAction(new NotificationCompat.Action(status.loaded && mediaPlayer!=null && mediaPlayer.isPlaying() ? R.drawable.player_pause : R.drawable.player_start, "", ppause));
        notification.addAction(new NotificationCompat.Action(R.drawable.player_next, "", pnext));
        notification.addAction(new NotificationCompat.Action(R.drawable.player_stop, "", pstop));


        startForeground(nid, notification.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        if(mediaPlayer.getCurrentPosition() > 3000) {
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
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
            broadcast();
        }
    }

    public void stop(){
        running = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        stopSelf();
    }

    public void play(){
        status.loaded = false;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), current.getUri());
            mediaPlayer.prepareAsync();
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
            mediaPlayer.start();
            broadcast();
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
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
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
