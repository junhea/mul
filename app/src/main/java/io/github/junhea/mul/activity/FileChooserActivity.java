package io.github.junhea.mul.activity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.junhea.mul.R;

import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_FOLDER;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_SONG;

public class FileChooserActivity extends PlayerBaseActivity {
    int mode;
    File currentDir;
    Context context;
    ListView list;
    TextView path;
    ArrayList<String> files;
    ArrayAdapter<String> adapter;
    List<File> history;
    Map<String,Integer> indexHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_file_chooser);
        panelOnCreate();

        mode = getIntent().getIntExtra("mode", REQUEST_SELECT_SONG);
        context = this;
        history = new ArrayList<>();
        indexHistory = new HashMap<>();

        getSupportActionBar().setTitle(mode == REQUEST_SELECT_SONG ? R.string.file_chooser_file : R.string.file_chooser_folder);

        currentDir = Environment.getExternalStorageDirectory();

        list = this.findViewById(R.id.file_list);
        path = this.findViewById(R.id.file_path);

        View selectContainer = this.findViewById(R.id.select_btn_container);

        if(mode == REQUEST_SELECT_FOLDER)
            selectContainer.setVisibility(View.VISIBLE);
        else
            selectContainer.setVisibility(View.GONE);

        CheckBox recursive = this.findViewById(R.id.select_recursive_check);

        this.findViewById(R.id.select_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("path", currentDir.getAbsolutePath()+"/");
                data.putExtra("recursive", recursive.isChecked());
                setResult(RESULT_OK, data);
                finish();
            }
        });

        path.setText(currentDir.getAbsolutePath());

        files = refresh();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>0){
                    // is directory
                    if(files.get(i).indexOf('/')>-1) {
                        history.add(currentDir);
                        currentDir = new File(currentDir, files.get(i));
                        populate();
                    }else{
                        // is file
                        if(mode == REQUEST_SELECT_SONG){
                            Intent data = new Intent();
                            data.putExtra("path", new File(currentDir, files.get(i)).getAbsolutePath());
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    }
                }else{
                    //parent
                    if(currentDir.getAbsolutePath().length()>1) currentDir = currentDir.getParentFile();
                    populate();
                }
            }
        });

        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(i == SCROLL_STATE_IDLE){
                    indexHistory.put(currentDir.getAbsolutePath(), list.getFirstVisiblePosition());
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        list.setAdapter(adapter);
    }

    public ArrayList<String> refresh(){
        File[] files = currentDir.listFiles();
        ArrayList<String> tmp = new ArrayList<>();
        try {
            for (File f : files) {
                if (f.isDirectory()) {
                    tmp.add(f.getName() + '/');
                }else {
                    tmp.add(f.getName());
                }
            }

            Collections.sort(tmp, new Comparator<String>() {
                // dir goes top file goes bottom
                @Override
                public int compare(String t, String t1) {
                    if(t.indexOf('/')>-1 && t1.indexOf('/')>-1){
                        return t.compareTo(t1);
                    }else if(t.indexOf('/')>-1){
                        return -1;
                    }else if(t1.indexOf('/')>-1){
                        return 1;
                    }else{
                        return t.compareTo(t1);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        tmp.add(0, "..");

        //change actionbar text
        path.setText(currentDir.getAbsolutePath());
        path.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        path.setMarqueeRepeatLimit(-1);
        path.setSingleLine(true);
        path.setSelected(true);
        return tmp;
    }

    @Override
    public void onBackPressed() {
        if(history.size() == 0)
            super.onBackPressed();
        else{
            // history stack
            currentDir = history.get(history.size()-1);
            history.remove(history.size()-1);
            populate();
        }
    }

    public void populate(){
        try {
            files.clear();
            adapter.notifyDataSetChanged();
            files.addAll(refresh());
            adapter.notifyDataSetChanged();

            Integer index = indexHistory.get(currentDir.getAbsolutePath());
            if(index != null){
                list.setSelection(index);
            }else{
                list.setSelection(0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.explorer_close:
                finish();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.explorer_menu, menu);
        return true;
    }
}