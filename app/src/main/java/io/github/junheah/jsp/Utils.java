package io.github.junheah.jsp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import io.github.junheah.jsp.gson.PlayListDeserializer;
import io.github.junheah.jsp.gson.PlayListSerializer;
import io.github.junheah.jsp.gson.RuntimeTypeAdapterFactory;
import io.github.junheah.jsp.interfaces.SongCallback;
import io.github.junheah.jsp.interfaces.StringCallback;
import io.github.junheah.jsp.model.PlayList;
import io.github.junheah.jsp.model.song.ExternalSong;
import io.github.junheah.jsp.model.song.ExternalSongContainer;
import io.github.junheah.jsp.model.song.LocalSong;
import io.github.junheah.jsp.model.song.Song;

public class Utils {
    // static functions

    public static String getPathFromUri(Context context, Uri uri){
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();
        return path;
    }

    public static void lockuiRecursive(View view, boolean lock){
        view.setEnabled(!lock);
        if(view instanceof ViewGroup && !(view instanceof RecyclerView)) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                View v = ((ViewGroup)view).getChildAt(i);
                lockuiRecursive(v, lock);
            }
        }
    }

    public static String getBaseScript(Context context){
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("base.mjs")));

            // do reading, usually loop until end of file reading
            String line = null;
            while ((line = reader.readLine()) != null) {
                //process line
                builder.append(line+"\n");
            }
        } catch (Exception e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //log the exception
                }
            }
        }
        return builder.toString();
    }


    public static Gson playListSerializer(){
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<PlayList>() {}.getType(), new PlayListSerializer())
                .create();
    }

    public static Gson playListDeserializer(){
        RuntimeTypeAdapterFactory<Song> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Song.class, "type")
                .registerSubtype(Song.class, "SONG")
                .registerSubtype(LocalSong.class, "LOCAL")
                .registerSubtype(ExternalSong.class, "EXTERNAL")
                .registerSubtype(ExternalSongContainer.class, "EXTERNAL.CONTAINER");
        return new GsonBuilder()
                .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
                .registerTypeAdapter(new TypeToken<PlayList>() {}.getType(), new PlayListDeserializer())
                .create();
    }

    public static Gson songListDeserializer() {
        RuntimeTypeAdapterFactory<Song> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
                .of(Song.class, "type")
                .registerSubtype(Song.class, "SONG")
                .registerSubtype(LocalSong.class, "LOCAL")
                .registerSubtype(ExternalSong.class, "EXTERNAL")
                .registerSubtype(ExternalSongContainer.class, "EXTERNAL.CONTAINER");
        return new GsonBuilder()
                .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
                .create();
    }

    public static void pickerPopup(Fragment fragment, String title, String[] options, StringCallback callback){
        View layout = fragment.getLayoutInflater().inflate(R.layout.content_picker_popup, null);
        ListView list = layout.findViewById(R.id.picker_content);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(fragment.getContext(), android.R.layout.simple_list_item_1, options);
        list.setAdapter(adapter);


        Dialog dialog = new AlertDialog.Builder(fragment.getContext(), R.style.AlertDialogTheme)
                .setView(layout)
                .setTitle(title)
                .create();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                callback.callback(options[i]);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void songAdderPopup(Context context, SongCallback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        final ScrollView scrollView = new ScrollView(context);
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

        scrollView.addView(layout);

        builder.setTitle("곡 정보 입력")
                .setView(scrollView)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Song song = new ExternalSong(nameInput.getText().toString(),
                                    nameInput.getText().toString(),
                                    artistInput.getText().toString(),
                                    urlInput.getText().toString(),
                                    coverInput.getText().toString(),
                                    new HashMap<>());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("확인", null)
                .show();
    }

    public static void singleInputPopup(Context context, StringCallback callback){
        final EditText editText = new EditText(context);
        editText.setHint("플레이리스트 이름");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
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
        new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("예", positiveCallback)
                .setNegativeButton("아니오", null)
                .show();
    }

    public static String readFile(File f) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringBuilder builder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n');
        }
        return builder.toString();
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }



}
