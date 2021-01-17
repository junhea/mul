package io.github.junheah.jsp.model.source;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ast.Scope;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.R;
import io.github.junheah.jsp.activity.InputActivity;
import io.github.junheah.jsp.interfaces.ScriptCallback;

import static io.github.junheah.jsp.MainApplication.baseScript;
import static io.github.junheah.jsp.MainApplication.client;
import static io.github.junheah.jsp.Utils.getBaseScript;
import static io.github.junheah.jsp.Utils.readFile;

public class Source {
    String name;
    String homepage;
    String author;
    String version;
    File script;
    transient String script_str;
    transient V8Thread thread;


    public Source(File file) throws Exception{
        this.script = file;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        //parse metadata
        while ((line = reader.readLine()) != null) {
            if (!line.contains("@")) break;  //end of metadata
            line = line.split("@")[1];
            String[] pair = line.split("::");
            String key = pair[0];
            String val = pair[1];
            switch (key) {
                case "name":
                    this.name = val;
                    break;
                case "homepage":
                    this.homepage = val;
                    break;
                case "author":
                    this.author = val;
                    break;
                case "version":
                    this.version = val;
                    break;
            }
        }
        System.out.println(name +"\t"+homepage +"\t"+author +"\t"+version +"\t");
    }

    public String getName() {
        return name;
    }


    public Search getSearch(String query){
        return new Search(this, query);
    }

    public void runScript(ScriptRequest request){
        if(thread == null || !thread.running) {
            thread = new V8Thread();
            thread.execute(request);
        }else{
            thread.runScript(request);
        }
    }

    void runOnUiThread(Context context, Runnable r){
        if(context instanceof Activity)
            ((Activity)context).runOnUiThread(r);
        else if(context instanceof FragmentActivity){
            ((FragmentActivity)context).runOnUiThread(r);
        }
    }

    public void close(){
        if(thread != null) {
            thread.forceStop();
            thread = null;
        }
    }

    public class V8Thread extends Thread{
        // thread that has v8 runtime

        org.mozilla.javascript.Context rhino;
        List<ScriptRequest> queue;
        boolean running = true;
        ScriptableObject scope;

        @Override
        public void run() {
            while(queue.size()>0){
                if(rhino == null) {
                    init();
                }
                ScriptRequest r = queue.get(0);
                if(r!= null) {
                    try {
                        //run script and get result
                        Function fct = (Function)scope.get(r.getFunction(), scope);
                        Object obj = fct.call(rhino, scope, scope, r.getArgs());

                        //callback
                        if (r.getCallback() != null) {
                            if(r.getContext() != null)
                                runOnUiThread(r.getContext(), new Runnable() {
                                    @Override
                                    public void run() {
                                        r.getCallback().callback(obj);
                                    }
                                });
                            else
                                r.getCallback().callback(obj);
                        }
                    } catch (Exception e) {
                        if(r.getContext() != null)
                            runOnUiThread(r.getContext(), new Runnable() {
                                @Override
                                public void run() {
                                    r.getCallback().onError(e);
                                }
                            });
                        else
                            r.getCallback().onError(e);
                        e.printStackTrace();
                    }
                }
                queue.remove(0);
            }
            //queue empty : stop thread
            if(rhino != null) {
                System.out.println("rhino exit! ");
                rhino = null;
                org.mozilla.javascript.Context.exit();
                running = false;
            }
        }

        public void forceStop(){
            this.running = false;
        }

        public void runScript(ScriptRequest r){
            queue.add(r);
        }

        public synchronized void execute(ScriptRequest request) {
            queue = new ArrayList<>();
            queue.add(request);
            super.start();
        }

        public void init(){
            try {
                System.out.println("rhino init!");
                rhino = org.mozilla.javascript.Context.enter();
                rhino.setOptimizationLevel(-1);
                scope = new ImporterTopLevel(rhino);
                //pass client
                Object wrappedClient = org.mozilla.javascript.Context.javaToJS(client, scope);
                ScriptableObject.putProperty(scope, "httpClient", wrappedClient);
                //for debugging
                Object wrappedOut = org.mozilla.javascript.Context.javaToJS(System.out, scope);
                ScriptableObject.putProperty(scope, "out", wrappedOut);
                //base script
                rhino.evaluateString(scope, baseScript, "base", 1, null);
                //script
                rhino.evaluateString(scope, readFile(script), "JavaScript", 1, null);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
