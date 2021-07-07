package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;



public class SongDataParser extends Thread {
    List<SongPlayListParcel> queue;
    Context context;
    public static boolean running;
    public SongDataParser(Context context){
        this.queue = new ArrayList<>();
        this.context = context;
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
        while(queue.size()>0 && running) {
            SongPlayListParcel parcel = queue.get(0);
            queue.remove(0);
            //parse data
            parcel.song.fetchData();
            parcel.playList.add(parcel.song);
            System.out.println(parcel.song.path);
        }
        running = false;
    }

    public void forceStop() {
        this.running = false ;
    }
}
