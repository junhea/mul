package io.github.junhea.mul;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import io.github.junhea.mul.model.source.Source;

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

    private static SourceIO io;

    public static synchronized SourceIO getInstance(Context context){
        if(io == null) io = new SourceIO(context.getApplicationContext());
        return io;
    }

    public SourceIO(Context context){
        this.context = context;
        reader = context.getSharedPreferences("mul.source_data", Context.MODE_PRIVATE);
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
                    s.setData(read(s.getName()));
                    sources.add(s);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void remove(String name){
        try {
            if(root == null)
                root = new File(context.getExternalFilesDir(null), "srcs");
            File target = new File(root, name+".mjs");
            if(target.exists()) {
                target.delete();
                editor.remove(name);
                editor.commit();
                for(int i = sources.size()-1; i>=0; i--){
                    if(sources.get(i).getName().equals(name)){
                        sources.remove(i);
                        return;
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String[] getNames(){
        String[] names = new String[sources.size()];
        for(int i=0; i<sources.size(); i++){
            names[i] = sources.get(i).getName();
        }
        return names;
    }

    public void write(String key, String data){
        //key = source name
        editor.putString(key, data);
        editor.commit();
    }

    public String read(String key){
        //key = source name
        return reader.getString(key, "");
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


//    //debug
//    public void createExample(){
//        File path = new File(root, "test.mjs");
//        if(!path.exists()) {
//            try {
//                FileWriter myWriter = new FileWriter(path);
//                myWriter.write("// @name::test\n// @author::me\n// @homepage::google.com\n// @version::1.0\n");
//                myWriter.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
