package io.github.junheah.jsp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import io.github.junheah.jsp.Player;
import io.github.junheah.jsp.R;

import static io.github.junheah.jsp.Player.ACTION_PLAYER_START;

public class MainActivity extends AppCompatActivity {

    boolean bound = false;
    Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check service
        Intent player = new Intent(getApplicationContext(), Player.class);
        startPlayer(player);


        //play btn
        this.findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent player = new Intent(getApplicationContext(), Player.class);
                player.setAction(ACTION_PLAYER_START);
                startPlayer(player);
            }
        });

        //broadcast receiver

    }


    private void startPlayer(Intent intent){
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
    }
}