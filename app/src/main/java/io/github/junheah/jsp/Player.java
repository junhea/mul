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
    public static final String ACTION_PLAYER_CREATED = "jsp.player_created";
    public static final int sid = 31525694;

    public static boolean running = false;
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

    public int getCurrentPosition(){
        if(mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
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
        switch (intent.getAction()) {
            case ACTION_PLAYER_CHECK:
                broadcast();
            case ACTION_PLAYER_CREATE:
                sendBroadcast(new Intent().setAction(ACTION_PLAYER_CREATED));
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
        this.playList = playList;
        if(this.playList != null && this.playList.size()>0){
            this.current = this.playList.get(0);
        }
    }

    public void seekTo(int pos){
        mediaPlayer.seekTo(pos);
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
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }else {
            mediaPlayer.start();
        }
        broadcast();
    }

    public void stop(){
        mediaPlayer.stop();
        stopSelf();
    }

    public void play(){
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(current.getUrl());
            mediaPlayer.prepareAsync();
            status.loaded = false;
        } catch (Exception e) {
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
            if(!next()) {
                seekTo(0);
                broadcast();
            }
        }
    }

    public void broadcast(){
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYER_BROADCAST);
        status.playing = mediaPlayer.isPlaying();
        if(status.loaded)
            status.duration = mediaPlayer.getDuration();
        else
            status.duration = 0;
        intent.putExtra("status", new Gson().toJson(status));
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        running = false;
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
