package io.github.junhea.mul;

import static io.github.junhea.mul.service.PlayerServiceHandler.player;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import io.github.junhea.mul.model.PlayerStatus;
import io.github.junhea.mul.service.PlayerServiceHandler;

public class TimerThread extends Thread{
    //is singleton
    static TimerThread thread;

    Runnable r;
    int pt;

    public static void init(TimerCallback callback){
        if(thread == null || !thread.isAlive()){
            thread = new TimerThread();
            thread.start();
        }
        thread.setCallback(callback);
    }

    public void setCallback(TimerCallback callback) {
        r = new Runnable() {
            @Override
            public void run() {
                callback.tick(player.getCurrentPosition());
            }
        };
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Handler uiHandler = new Handler(Looper.getMainLooper());
        while(true) {
            if (PlayerServiceHandler.bound && PlayerStatus.playing && PlayerStatus.loaded && r != null) {
                int t = player.getCurrentPosition();
                //tick is run on ui thread
                if(pt/1000 != t/1000) {
                    pt = t;
                    uiHandler.post(r);
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public interface TimerCallback{
        void tick(int t);
    }
}
