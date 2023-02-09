package io.github.junhea.mul.activity;

import static android.widget.Toast.LENGTH_SHORT;

import static io.github.junhea.mul.Utils.dpToPx;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import io.github.junhea.mul.R;
import io.github.junhea.mul.SourceIO;
import io.github.junhea.mul.interfaces.ScriptCallback;
import io.github.junhea.mul.model.source.ScriptRequest;
import io.github.junhea.mul.model.source.Source;

public class SourceSettingActivity extends PlayerBaseActivity{


    public static final int SOURCE_SETTING_REQUEST = 6;

    LinearLayoutCompat container;
    Button saveBtn;
    SourceIO sourceIO;
    Source source;
    Map<String, Map<String, String>> data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_source_setting);
        panelOnCreate();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_source_setting)+" - "+getIntent().getStringExtra("source"));

        container = this.findViewById(R.id.settings_container);
        saveBtn = this.findViewById(R.id.save_btn);
        saveBtn.setVisibility(View.GONE);
        sourceIO = SourceIO.getInstance(context);

        source = sourceIO.getSource(getIntent().getStringExtra("source"));

        //get data object
        source.runScript(new ScriptRequest(context, "getData", new Object[]{}, new ScriptCallback() {
            @Override
            public void callback(Object res) {
                data = (Map<String, Map<String, String>>) res;
                // populate ui
                populate();
            }

            @Override
            public void onError(Exception e) {

            }
        }));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //read values and update data obj
                int i = 0;
                Map<String, Map<String,String>> newData = new HashMap<>();
                for(String key : data.keySet()){
                    Map<String, String> d = new HashMap<>();
                    d.put("type", data.get(key).get("type"));
                    if(data.get(key).get("type").equals("msg")){
                        d.put("value", data.get(key).get("value"));
                    }else {
                        d.put("value", ((EditText) container.getChildAt((i * 2) + 1)).getText().toString());
                    }
                    newData.put(key, d);
                    i++;
                }
                data = newData;
                String datas = new Gson().toJson(data);
                //save
                sourceIO.write(source.getName(), datas);
                //apply to source obj
                source.setData(datas);
                Toast.makeText(context, getString(R.string.msg_apply_success), LENGTH_SHORT).show();
                finish();
            }
        });

        this.findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset
                source.setData("");
                sourceIO.write(source.getName(),"");
                //reopen
                finish();
                Intent intent = new Intent(context, SourceSettingActivity.class);
                intent.putExtra("source", source.getName());
                startActivity(intent);
            }
        });
    }

    public void populate(){
        for(String key : data.keySet()){
            Map<String,String> d = data.get(key);
            TextView title = new TextView(context);
            title.setText(key);
            container.addView(title);

            if(d.get("type").equals("msg")) {
                TextView text = new TextView(context);
                text.setPadding(dpToPx(context, 10),0,0,0);
                text.setText(d.get("value"));
                container.addView(text);
            }else{
                EditText input = new EditText(context);
                input.setText(d.get("value"));
                switch (d.get("type")) {
                    case "str":
                        input.setHint("String");
                        break;
                    case "int":
                        input.setHint("Integer");
                        break;
                }
                container.addView(input);
            }
        }
        saveBtn.setVisibility(View.VISIBLE);
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
