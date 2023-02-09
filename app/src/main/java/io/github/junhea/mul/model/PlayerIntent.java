package io.github.junhea.mul.model;

import android.content.Context;
import android.content.Intent;

import io.github.junhea.mul.service.Player;

public class PlayerIntent extends Intent {
    public PlayerIntent(Context context, String action){
        super(context, Player.class);
        super.setAction(action);
    }
}
