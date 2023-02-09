package io.github.junhea.mul.model.song;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.R;
import io.github.junhea.mul.model.room.LocalSongDao;
import io.github.junhea.mul.model.room.SongDatabase;

import static io.github.junhea.mul.MainApplication.library;
import static io.github.junhea.mul.Utils.snackbar;


public class SongDataParser extends Thread {
    static SongDataParser instance;

    List<SongPlayListParcel> queue;
    Context context;
    LocalSongDao dao;
    public static boolean running;
    boolean exists;

    public static synchronized void parse(Context context, SongPlayListParcel parcel){
        if(instance == null || !instance.running){
            instance = new SongDataParser(context);
            instance.execute(parcel);
        }else{
            instance.add(parcel);
        }
    }



    public SongDataParser(Context context){
        this.queue = new ArrayList<>();
        this.context = context;
        this.dao = SongDatabase.getInstance(context).localDao();
    }

    public void execute(SongPlayListParcel parcel){
        queue.add(parcel);
        running = true;
        super.start();
    }
    public void add(SongPlayListParcel parcel){
        queue.add(parcel);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while(queue.size()>0 && running) {
            //pop one item
            SongPlayListParcel parcel = queue.get(0);
            queue.remove(0);

            boolean success = true;
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
                //check if addable
                boolean addable = parcel.playList != null ? parcel.playList.addable(finalS) : false;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(!exists)
                            library.addWithSort(finalS);
                        if(addable)
                            parcel.playList.forceAdd(finalS);
                    }
                });

                if(!addable)
                    success = false;
            }
            if(parcel.playList != null)
                parcel.playList.forcesave();

            snackbar(((Activity)context).findViewById(android.R.id.content),
                    context.getString(success ? R.string.msg_add_success : R.string.msg_add_err_duplicate),
                    context.getString(R.string.msg_ok));
        }
        running = false;
    }
    public void forceStop() {
        this.running = false ;
    }
}
