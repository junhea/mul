package io.github.junheah.jsp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.Iterator;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.SourceIO;

public class InputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        Intent intent = getIntent();
        EditText input = this.findViewById(R.id.userdata_input);
        input.setText(intent.getStringExtra("data"));
        String name = intent.getStringExtra("name");
        SourceIO sourceIO = new SourceIO(this);
        this.findViewById(R.id.userdata_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: check value before writing
                sourceIO.write(name, input.getText().toString());
                Intent res = new Intent();
                res.putExtra("data", input.getText().toString());
                setResult(0, res);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        //
    }
}