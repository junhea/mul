package io.github.junheah.jsp;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.github.junheah.jsp.gson.PlayListDeserializer;
import io.github.junheah.jsp.gson.PlayListSerializer;
import io.github.junheah.jsp.gson.RuntimeTypeAdapterFactory;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

public class Utils {
    // static functions

    public static Gson playListSerializer(){
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<PlayList>() {}.getType(), new PlayListSerializer())
                .create();
    }

    public static Gson playListDeserializer(){
        RuntimeTypeAdapterFactory<Song> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Song.class, "TYPE")
                .registerSubtype(Song.class, "SONG")
                .registerSubtype(LocalSong.class, "LOCAL")
                .registerSubtype(ExternalSong.class, "EXTERNAL");
        return new GsonBuilder()
                .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
                .registerTypeAdapter(new TypeToken<PlayList>() {}.getType(), new PlayListDeserializer())
                .create();
    }

    public static void songAdderPopup(Context context, SongCallback callback){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme);
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
                        try {
                            Song song = new Song(nameInput.getText().toString(),
                                    artistInput.getText().toString(),
                                    urlInput.getText().toString(),
                                    coverInput.getText().toString());
                            callback.callback(song);
                        }catch (Exception e){
                            e.printStackTrace();
                            showPopup(context, "오류", "error in song info");
                        }
                    }
                })
                .show();
    }

    public static void showPopup(Context context, String title, String content){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("확인", null)
                .show();
    }

    public static void singleInputPopup(Context context, StringCallback callback){
        final EditText editText = new EditText(context);
        editText.setHint("플레이리스트 이름");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme);
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

    public static void YesNoPopup(Context context, String title, String content, DialogInterface.OnClickListener positiveCallback){
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("예", positiveCallback)
                .setNegativeButton("아니오", null)
                .show();
    }

}
