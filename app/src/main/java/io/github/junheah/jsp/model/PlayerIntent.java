package io.github.junheah.jsp.model;

import android.content.Context;
import android.content.Intent;

import io.github.junheah.jsp.Player;

public class PlayerIntent extends Intent {
    public PlayerIntent(Context context, String action){
        super(context, Player.class);
        super.setAction(action);
    }
}
