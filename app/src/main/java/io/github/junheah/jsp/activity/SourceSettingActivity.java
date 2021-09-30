package io.github.junheah.jsp.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.SourceIO;
import io.github.junheah.jsp.model.source.ScriptRequest;
import io.github.junheah.jsp.model.source.Source;

public class SourceSettingActivity extends PlayerBaseActivity{

    //TODO enable user input for source setup

    public static final int SOURCE_SETTING_REQUEST = 6;

    EditText input;
    Button saveBtn;
    SourceIO sourceIO;
    Source source;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_source_setting);
        panelOnCreate();

        input = this.findViewById(R.id.input);
        saveBtn = this.findViewById(R.id.save_btn);
        sourceIO = SourceIO.getInstance(context);

        source = sourceIO.getSource(getIntent().getStringExtra("source"));

        //get data object




    }
}
