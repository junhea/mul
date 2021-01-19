package io.github.junheah.jsp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.model.source.Source;

import static io.github.junheah.jsp.Utils.getBaseScript;

public class SourceIO {
    File root;
    List<Source> sources;
    SharedPreferences reader;
    SharedPreferences.Editor editor;
    Context context;
    FilenameFilter scriptFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return s.endsWith(".mjs");
        }
    };

    public SourceIO(Context context){
        this.context = context;
        reader = context.getSharedPreferences("JSPlayer.source_data", Context.MODE_PRIVATE);
        editor = reader.edit();

    }
    public void load(){
        try {
            root = new File(context.getExternalFilesDir(null), "srcs");
            if(!root.exists())
                root.mkdir();
            sources = new ArrayList<>();

            for(File f : root.listFiles(scriptFilter)){
                try {
                    Source s = new Source(f);
                    sources.add(s);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void write(String key, String data){
        editor.putString(key, data);
        editor.commit();
    }

    public List<Source> getSources(){
        return this.sources;
    }

    public Source getSource(String name){
        for(Source s : sources){
            if(name.equals(s.getName()))
                return s;
        }
        return null;
    }


    //debug
    public void createExample(){
        File path = new File(root, "test.mjs");
        if(!path.exists()) {
            try {
                FileWriter myWriter = new FileWriter(path);
                myWriter.write("// @name::test\n// @author::me\n// @homepage::google.com\n// @version::1.0\n");
                myWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
