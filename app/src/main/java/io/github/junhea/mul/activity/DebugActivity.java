package io.github.junhea.mul.activity;

import static io.github.junhea.mul.MainApplication.library;
import static io.github.junhea.mul.model.song.Song.LOCAL;

import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import io.github.junhea.mul.PlayListIO;
import io.github.junhea.mul.R;
import io.github.junhea.mul.model.room.LocalSongDao;
import io.github.junhea.mul.model.room.SongDatabase;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;


public class DebugActivity extends PlayerBaseActivity {
    TextView output;
    Context context;
    ScrollView scroll;
    PlayListIO playListIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_debug);
        panelOnCreate();

        Button pref = this.findViewById(R.id.debug_pref);
        output = this.findViewById(R.id.debug_out);
        context = this;

        playListIO = PlayListIO.getInstance(context);
        scroll = this.findViewById(R.id.debug_scroll);
        pref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.setText(playListIO.getRaw());
            }
        });

        Button clear = this.findViewById(R.id.debug_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.setText("");
            }
        });
        final EditText editor = this.findViewById(R.id.debug_edit);
        Button editPref = this.findViewById(R.id.debug_pref_edit);
        final Button save = this.findViewById(R.id.debug_save);
        final Button cancel = this.findViewById(R.id.debug_cancel);
        editPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.setVisibility(View.VISIBLE);
                save.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                editor.setText(playListIO.getRaw());
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //save changes
                        writeToPref(editor.getText());
                        editor.setVisibility(View.GONE);
                        save.setVisibility(View.GONE);
                        cancel.setVisibility(View.GONE);
                        editor.setText(playListIO.getRaw());
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //discard
                        editor.setVisibility(View.GONE);
                        save.setVisibility(View.GONE);
                        cancel.setVisibility(View.GONE);
                        editor.setText("");
                    }
                });
            }
        });


        this.findViewById(R.id.debug_only_album_Art).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Map<String, List<long[]>> data = playListIO.getRawObject();
                        for(Song s : library){
                            if(s instanceof LocalSong){
                                if(((LocalSong) s).nocover){
                                    long sid = s.getSid();
                                    LocalSongDao ld = SongDatabase.getInstance(context).localDao();
                                    ld.delete((LocalSong) s);

                                    for(String k : data.keySet()){
                                        for(int i=data.get(k).size()-1; i>-1; i--){
                                            long[] o = data.get(k).get(i);
                                            if(o[0] == LOCAL && o[1] == sid){
                                                data.get(k).remove(i);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        playListIO.writeRawObj(data);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "done!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    void writeToPref(Editable edit){
        playListIO.writeRaw(edit.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.debug_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.debug_up) {
            scroll.fullScroll(View.FOCUS_UP);
        }else if (id == R.id.debug_down){
            scroll.fullScroll(View.FOCUS_DOWN);
        }
        return super.onOptionsItemSelected(item);
    }

}
