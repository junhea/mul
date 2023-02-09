package io.github.junhea.mul.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.os.Bundle;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.R;
import io.github.junhea.mul.SourceIO;
import io.github.junhea.mul.adapter.SourceAdapter;
import io.github.junhea.mul.interfaces.SourceOnClickCallback;
import io.github.junhea.mul.model.viewHolder.SourceItem;
import okhttp3.Request;
import okhttp3.Response;

import static io.github.junhea.mul.MainApplication.client;
import static io.github.junhea.mul.adapter.SourceAdapter.AVAILABLE;
import static io.github.junhea.mul.adapter.SourceAdapter.INSTALLED;

public class SourceManagerActivity extends PlayerBaseActivity{
    SourceAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_source_manager);
        panelOnCreate();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recycler = findViewById(R.id.source_recycler);
        ((SimpleItemAnimator) recycler.getItemAnimator()).setSupportsChangeAnimations(false);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SourceAdapter(this, new SourceOnClickCallback() {
            @Override
            public void download(SourceItem item) {
                new Thread(){
                    @Override
                    public void run() {
                        Response r = client.getRaw(new Request.Builder().url(item.url).build());

                        try {
                            InputStream is = r.body().byteStream();

                            BufferedInputStream input = new BufferedInputStream(is);

                            File root = new File(getExternalFilesDir(null), "srcs");
                            if (!root.exists())
                                root.mkdir();

                            OutputStream output = new FileOutputStream(new File(root, item.name + ".mjs"));

                            byte[] data = new byte[1024];

                            long total = 0;
                            int count;

                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            input.close();

                            item.status = INSTALLED;

                            //reload
                            SourceIO.getInstance(SourceManagerActivity.this).load();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyItemChanged(item);
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            @Override
            public void delete(SourceItem item) {
                SourceIO.getInstance(SourceManagerActivity.this).remove(item.name);
                item.status = AVAILABLE;
                adapter.notifyItemChanged(item);
            }
        });
        recycler.setAdapter(adapter);
        //background
        new Thread(){
            @Override
            public void run() {
                List<SourceItem> res = fetch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(SourceItem i : res){
                            adapter.addAvailable(i);
                        }
                    }
                });
            }
        }.start();
    }

    public List<SourceItem> fetch(){
        //get available sources
        List<SourceItem> res = new ArrayList<>();
        try {
            String raw = client.get(new Request.Builder().url("https://api.github.com/repos/junhea/mul-scripts/contents/scripts").build()).getBody();
            JSONArray d = new JSONArray(raw);
            for(int i=0; i<d.length(); i++){
                JSONObject o = d.getJSONObject(i);
                res.add(new SourceItem(o.getString("name").split(".mjs")[0], o.getString("download_url")));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}