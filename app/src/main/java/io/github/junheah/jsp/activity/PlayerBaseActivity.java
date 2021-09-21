package io.github.junheah.jsp.activity;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.appcompat.app.AppCompatActivity;

import io.github.junheah.jsp.service.Player;

public class PlayerBaseActivity extends AppCompatActivity {
    /*
    Base Activity for every activity that requires access to Player service

    Binds service onStart, unbinds onStop
     */
    public static boolean bound = false;
    private static Player player;
    BroadcastReceiver receiver;
    IntentFilter filter;
}
