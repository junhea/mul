package io.github.junheah.jsp;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;

import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.Song;

public class Utils {
    // static functions
    public static void songAdderPopup(Context context, SongCallback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LinearLayoutCompat layout = new LinearLayoutCompat(context);
        layout.setOrientation(LinearLayoutCompat.VERTICAL);
        final EditText nameInput = new EditText(context);
        final TextView nameLabel = new TextView(context);
        nameLabel.setText("제목");
        final EditText artistInput = new EditText(context);
        final TextView artistLabel = new TextView(context);
        artistLabel.setText("아티스트명");
        final EditText urlInput = new EditText(context);
        final TextView urlLabel = new TextView(context);
        urlLabel.setText("주소");
        final EditText coverInput = new EditText(context);
        final TextView coverLabel = new TextView(context);
        coverLabel.setText("커버 주소");
        layout.addView(nameLabel);
        layout.addView(nameInput);
        layout.addView(artistLabel);
        layout.addView(artistInput);
        layout.addView(urlLabel);
        layout.addView(urlInput);
        layout.addView(coverLabel);
        layout.addView(coverInput);


        builder.setTitle("곡 정보 입력")
                .setView(layout)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Song song = new Song(nameInput.getText().toString(),
                                artistInput.getText().toString(),
                                urlInput.getText().toString(),
                                coverInput.getText().toString());
                        callback.callback(song);
                    }
                })
                .show();
    }

    public static void showPopup(Context context, String title, String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("확인", null)
                .show();
    }

    public static void singleInputPopup(Context context, StringCallback callback){
        final EditText editText = new EditText(context);
        editText.setHint("플레이리스트 이름");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("플레이리스트 생성")
                .setView(editText)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.callback(editText.getText().toString());
                    }
                })
                .show();
    }

}
