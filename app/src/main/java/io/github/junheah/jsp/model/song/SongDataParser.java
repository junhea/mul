package io.github.junheah.jsp.model.song;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.model.viewHolder.PlayListViewHolder;

import static io.github.junheah.jsp.MainApplication.defaultCover;


public class SongDataParser extends Thread {
    List<Song> queue;
    Context context;
    public static boolean running;
    public SongDataParser(Context context){
        this.queue = new ArrayList<>();
        this.context = context;
    }

    public synchronized void execute(Song song){
        if(queue.indexOf(song) == -1){
            //prevent duplicates
            queue.add(song);
            running = true;
            super.start();
        }
    }
    public synchronized void add(Song song){
        if(queue.indexOf(song) == -1){
            //prevent duplicates
            queue.add(song);
        }
    }

    @Override
    public void run() {
        while(queue.size()>0 && running) {
            Song target = queue.get(0);
            System.out.println(target.getName());
            queue.remove(0);
            //parse data
            target.loadCover(this.context, null);
        }
        running = false;
    }

    public void forceStop() {
        this.running = false ;
    }
}
