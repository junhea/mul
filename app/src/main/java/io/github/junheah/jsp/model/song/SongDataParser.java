package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.fragment.HomeFragment;
import io.github.junheah.jsp.model.room.LocalSongDao;
import io.github.junheah.jsp.model.room.SongDatabase;

import static io.github.junheah.jsp.MainApplication.library;


public class SongDataParser extends Thread {
    List<SongPlayListParcel> queue;
    Context context;
    LocalSongDao dao;
    public static boolean running;
    boolean exists;

    public SongDataParser(Context context){
        this.queue = new ArrayList<>();
        this.context = context;
        this.dao = SongDatabase.getInstance(context).localDao();
    }

    public synchronized void execute(SongPlayListParcel parcel){
        queue.add(parcel);
        running = true;
        super.start();
    }
    public synchronized void add(SongPlayListParcel parcel){
        queue.add(parcel);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while(queue.size()>0 && running) {
            //pop one item
            SongPlayListParcel parcel = queue.get(0);
            queue.remove(0);

            for(Song s : parcel.songs){
                exists = false;
                try {
                    s.setSid(dao.insert((LocalSong) s));
                    s.fetchData();
                    dao.replace((LocalSong) s);
                }catch (SQLiteConstraintException e){
                    //already exists
                    exists = true;
                    s = dao.findWithPath(s.path);
                }
                Song finalS = s;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(!exists)
                            library.addWithSort(finalS);
                        if(parcel.playList != null)
                            parcel.playList.add(finalS);
                    }
                });
            }
            if(parcel.playList != null)
                parcel.playList.forcesave();

        }
        running = false;
    }
    public void forceStop() {
        this.running = false ;
    }
}
