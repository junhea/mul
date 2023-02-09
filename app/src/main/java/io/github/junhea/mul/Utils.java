package io.github.junhea.mul;

import static io.github.junhea.mul.MainApplication.library;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_FOLDER;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_LIBRARY;
import static io.github.junhea.mul.fragment.HomeFragment.REQUEST_SELECT_SONG;
import static io.github.junhea.mul.model.song.Song.EXTERNAL;
import static io.github.junhea.mul.model.song.Song.LOCAL;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.github.junhea.mul.activity.FileChooserActivity;
import io.github.junhea.mul.activity.LibrarySelectionActivity;
import io.github.junhea.mul.fragment.DetailFragment;
import io.github.junhea.mul.interfaces.IntegerCallback;
import io.github.junhea.mul.interfaces.SongCallback;
import io.github.junhea.mul.interfaces.StringCallback;
import io.github.junhea.mul.model.PlayList;
import io.github.junhea.mul.model.PlayerStatus;
import io.github.junhea.mul.model.room.SongDatabase;
import io.github.junhea.mul.model.song.ExternalSong;
import io.github.junhea.mul.model.song.LocalSong;
import io.github.junhea.mul.model.song.Song;

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
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
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

    public static PlayList getPlayList(String name){
        PlayList playList = null;
        //add song to playlist
        if (PlayerStatus.playList != null && PlayerStatus.playList.getName().equals(name)) {
            //from player
            playList = PlayerStatus.playList;
        } else if (DetailFragment.getCurrentPlayList() != null && DetailFragment.getCurrentPlayList().getName().equals(name)) {
            //from playlist fragment
            playList = DetailFragment.getCurrentPlayList();
        }
        return playList;
    }


    public static void pickerPopup(Fragment fragment, String title, String[] options, IntegerCallback callback){
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
                callback.callback(i);
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
                            callback.notify(song);
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

    public static void singleInputPopup(Context context, String title, String hint, StringCallback callback){
        final AppCompatEditText editText = new AppCompatEditText(context);
        editText.setHint(hint);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setTitle(title)
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
                .setPositiveButton(context.getString(R.string.prompt_yes), positiveCallback)
                .setNegativeButton(context.getString(R.string.prompt_no), null)
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
        Drawable drawable;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = VectorDrawableCompat.create(context.getResources(), drawableId, context.getTheme());
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }else{
            drawable = ContextCompat.getDrawable(context, drawableId);
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void openFile(Fragment fragment) {
        Intent intent = new Intent(fragment.getContext(), FileChooserActivity.class);
        intent.putExtra("mode", REQUEST_SELECT_SONG);
        fragment.startActivityForResult(intent, REQUEST_SELECT_SONG);
    }

    public static void openDirectory(Fragment fragment) {
        Intent intent = new Intent(fragment.getContext(), FileChooserActivity.class);
        intent.putExtra("mode", REQUEST_SELECT_FOLDER);
        fragment.startActivityForResult(intent, REQUEST_SELECT_FOLDER);
    }



    public static void openLibrary(Fragment fragment) {
        Intent intent = new Intent(fragment.getContext(), LibrarySelectionActivity.class);
        intent.putExtra("mode", REQUEST_SELECT_LIBRARY);
        fragment.startActivityForResult(intent, REQUEST_SELECT_LIBRARY);
    }

    public static void snackbar(View layout, String content, String ok){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(layout, content, Snackbar.LENGTH_LONG);
                snackbar.setAction(ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }

    public static Snackbar createSnackbar(View layout, String content, String ok){
        final Snackbar snackbar = Snackbar.make(layout, content, Snackbar.LENGTH_LONG);
        snackbar.setAction(ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        return snackbar;
    }


    public static void deleteSongPopup(Context context, Song song){
        //delete song from library
        PlayListIO playListIO = PlayListIO.getInstance(context);
        YesNoPopup(context, song.getName(), context.getString(R.string.prompt_remove_song_from_library),
                new DialogInterface.OnClickListener() {
                    List<Integer> indexes = new ArrayList<>();
                    PlayList pl;
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //yes
                        long[] sid = {song instanceof LocalSong ? LOCAL : EXTERNAL, song.getSid()};
                        library.remove(song);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //apply to db
                                if(song instanceof LocalSong)
                                    SongDatabase.getInstance(context).localDao().delete((LocalSong) song);
                                else
                                    SongDatabase.getInstance(context).externalDao().delete((ExternalSong) song);

                                //remove instances from playlists

                                for(String name : playListIO.getNames()){
                                    indexes.clear();
                                    //find
                                    List<long[]> ids  = playListIO.getids(name);
                                    for(int i = ids.size()-1; i>-1; i--){
                                        long[] id = ids.get(i);
                                        if(Arrays.equals(id, sid)) {   //remove from pl
                                            ids.remove(i);
                                            indexes.add(i);
                                        }
                                    }
                                    //write
                                    if(indexes.size()>0) {
                                        pl = getPlayList(name);
                                        if(pl == null) {
                                            playListIO.writeIds(name, ids);
                                        }else{
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for(int i : indexes)
                                                        pl.remove(i);
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        }).start();
                    }
                });
    }


    public static void deleteSongPopup(Context context, PlayList list, Song song){
        YesNoPopup(context, song.getName(), context.getString(R.string.prompt_remove_song_from_playlist),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //yes
                        list.remove(song);
                    }
                });
    }

    public static String getTimeStamp(int m){
        long second = (m / 1000) % 60;
        long minute = (m / (1000 * 60)) % 60;
        long hour = (m / (1000 * 60 * 60)) % 24;
        return(String.format("%02d:%02d:%02d", hour, minute, second));
    }

    public static void toggleButtons(ViewGroup group, boolean playerIsRunning){
        for(int i=0; i<group.getChildCount(); i++){
            View view = group.getChildAt(i);
            if(view instanceof Button){
                view.setEnabled(playerIsRunning);
            }else if(view instanceof SeekBar){
                if(!playerIsRunning) ((SeekBar) view).setProgress(0);
                view.setEnabled(playerIsRunning);
            }else if(view instanceof ImageButton){
                view.setEnabled(playerIsRunning);
            }else if(view instanceof ViewGroup){
                toggleButtons((ViewGroup) view, playerIsRunning);
            }else if(view instanceof ProgressBar){
                if(!playerIsRunning)((ProgressBar) view).setProgress(0);
            }else if(view instanceof TextView){
                if(!playerIsRunning) ((TextView) view).setText("");
            }else if(view instanceof ImageView){
                if(!playerIsRunning) ((ImageView) view).setImageResource(R.drawable.music);
            }
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context)
    {
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && !hasMenuKey)
        {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }



}
