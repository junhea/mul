package io.github.junhea.mul.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.junhea.mul.R;
import io.github.junhea.mul.fragment.SettingsFragment;

public class SettingsActivity extends PlayerBaseActivity{
    //todo preference activity

    public static final int REQUEST_SETTINGS = 44;
    public static final int RESULT_NEED_RESTART = 67;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity_settings);
        panelOnCreate();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();

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
