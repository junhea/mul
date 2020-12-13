package io.github.junheah.jsp.model.source;

import android.content.pm.ShortcutManager;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.junheah.jsp.MainApplication.client;
import static io.github.junheah.jsp.Utils.readFile;

public class Source {
    String name;
    String homepage;
    String author;
    String version;
    File script;
    V8Thread thread;

    public Source(File file) throws Exception{
        this.script = file;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        //parse metadata
        while ((line = reader.readLine()) != null) {
            if (!line.contains("@")) break;  //end of metadata
            line = line.split("@")[1];
            String[] pair = line.split("\t");
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

    public void init(){
        thread = new V8Thread();
        thread.start();
    }


    public Search getSearch(String query){
        return new Search(this, query);
    }

    private class Console {
        public void log(final String message) {
            System.out.println("[INFO] " + message);
        }
        public void err(final String message) {
            System.out.println("[ERROR] " + message);
        }
    }

    public class V8Thread extends Thread{
        V8 runtime;
        List<V8Request> queue;
        boolean running = true;

        @Override
        public void run() {
            if(runtime == null)
                init();
            while(running){
                if(queue.size()>0){
                    V8Request r = queue.get(0);
                    Object obj = runtime.executeObjectScript(queue.get(0).getScript());
                    if(r.getCallback() != null)
                        r.getCallback().callback(obj);
                    queue.remove(0);
                }
            }
        }

        public void runScript(V8Request r){
            queue.add(r);
        }

        @Override
        public synchronized void start() {
            super.start();
            queue = new ArrayList<>();
        }

        public void init(){
            try {
                Class[] classes = { String.class };
                runtime = V8.createV8Runtime();
                // register callbacks
                // console for debugging
                Console console = new Console();
                V8Object v8Console = new V8Object(runtime);
                runtime.add("console", v8Console);
                v8Console.registerJavaMethod(console, "log", "log", classes);
                v8Console.registerJavaMethod(console, "err", "err", classes);
                v8Console.close();

                // http
                runtime.registerJavaMethod(this, "httpGet", "httpGet", new Class[] {V8Object.class});

                // filesystem

                // run base script

                // initially run script
                runtime.executeScript(readFile(script));

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public Response httpGet(final V8Object request){
            return client.httpget(runtime, request);
        }
    }
}
