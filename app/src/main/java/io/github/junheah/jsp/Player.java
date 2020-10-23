package io.github.junheah.jsp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.PlayerStatus;
import io.github.junheah.jsp.model.Song;

public class Player extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
    public static final String ACTION_PLAYER_CREATE = "jsp.player_create";
    public static final String ACTION_PLAYER_CHECK = "jsp.player_check";
    public static final String ACTION_PLAYER_STOP = "jsp.player_stop";
    public static final String ACTION_PLAYER_START = "jsp.player_start";
    public static final String ACTION_PLAYER_PAUSE = "jsp.player_pause";
    public static final String ACTION_PLAYER_NEXT = "jsp.player_next";
    public static final String ACTION_PLAYER_PREV = "jsp.player_prev";
    public static final String ACTION_PLAYER_APPEND = "jsp.player_append";
    public static final String ACTION_PLAYER_BROADCAST = "jsp.player_broadcast";
    public static final int sid = 31525694;

    public static boolean running = false;
    boolean busy = false;
    boolean playing = false;
    PlayerStatus status;
    PlayList playList;
    Song current;
    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;
    Intent pendingIntent;
    final IBinder binder = new PlayerBinder();


    public Player() {
        super();
        status = new PlayerStatus();
    }

    public Song getCurrent(){
        return this.current;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, getApplication().getPackageName());
        wifiLock.acquire();
        pendingIntent = new Intent();
        showNotification();
    }

    void showNotification(){
        Intent notificationIntent = new Intent(this, io.github.junheah.jsp.Activity.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mchannel = new NotificationChannel(getApplication().getPackageName(), "media player", NotificationManager.IMPORTANCE_DEFAULT);
            mchannel.setDescription("media player");
            mchannel.enableLights(false);
            mchannel.enableVibration(false);
            mchannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, getApplication().getPackageName())
                .setContentText("media player running")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true);
        startForeground(sid, notification.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch(intent.getAction()){
            case ACTION_PLAYER_CHECK:
                break;
            case ACTION_PLAYER_CREATE:
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
        broadcast();
        return START_STICKY;
    }

    public void setPlayList(PlayList playList){
        this.playList = playList;
        if(this.playList != null && this.playList.size()>0){
            this.current = this.playList.get(0);
        }
    }

    public void next(){
        if(current.getNext() != null){
            current = current.getNext();
            play();
        }
        broadcast();
    }

    public void prev(){
        if(current.getPrev() != null){
            current = current.getPrev();
            play();
        }
        broadcast();
    }

    public void pause(){
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            status.setPlaying(false);
        }else {
            mediaPlayer.start();
            status.setPlaying(true);
        }
        broadcast();
    }

    public void stop(){
        mediaPlayer.stop();
        status.setPlaying(false);
        stopSelf();
    }

    public void play(){
        try {
            status.setPlaying(false);
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(current.getUrl());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
            //showNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
        broadcast();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        status.setPlaying(true);
        broadcast();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        status.setPlaying(false);
        broadcast();
    }

    public void broadcast(){
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYER_BROADCAST);
        status.setPlaying(mediaPlayer.isPlaying());
        intent.putExtra("status", new Gson().toJson(status));
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcast();
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
