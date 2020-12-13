package io.github.junheah.jsp;

import android.content.Context;
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

public class SourceIO {
    File root;
    List<Source> sources;
    FilenameFilter scriptFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return s.endsWith(".mjs");
        }
    };

    public SourceIO(Context context){
        try {
            root = new File(context.getExternalFilesDir(null), "srcs");
            if(!root.exists())
                root.mkdir();
            sources = new ArrayList<>();

            for(File f : root.listFiles(scriptFilter)){
                try {
                    sources.add(new Source(f));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Source> getSources(){
        return this.sources;
    }

    //debug
    public void createExample(){
        File path = new File(root, "test.mjs");
        if(!path.exists()) {
            try {
                FileWriter myWriter = new FileWriter(path);
                myWriter.write("// @name\ttest\n// @author\tme\n// @homepage\tgoogle.com\n// @version\t1.0\n");
                myWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
