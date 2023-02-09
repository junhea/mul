package io.github.junhea.mul.fragment;

import static io.github.junhea.mul.activity.SettingsActivity.RESULT_NEED_RESTART;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import io.github.junhea.mul.R;
import io.github.junhea.mul.activity.WatchListManagerActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findPreference("setting_point_color").setOnPreferenceChangeListener((preference, newValue) -> {
            getActivity().setResult(RESULT_NEED_RESTART);
            return true;
        });

        findPreference("setting_library_opacity").setOnPreferenceChangeListener((preference, newValue) -> {
            getActivity().setResult(RESULT_NEED_RESTART);
            return true;
        });

        findPreference("setting_watch_list").setOnPreferenceClickListener((preference) -> {
            startActivity(new Intent(getContext(), WatchListManagerActivity.class));
            return true;
        });

    }
}
