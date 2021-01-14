package io.github.junheah.jsp.model.source;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import io.github.junheah.jsp.activity.InputActivity;
import io.github.junheah.jsp.interfaces.V8Callback;
import io.github.junheah.jsp.soup.V8soup;

import static io.github.junheah.jsp.MainApplication.client;
import static io.github.junheah.jsp.Utils.readFile;

public class Source {
    String name;
    String homepage;
    String author;
    String version;
    File script;
    transient V8Thread thread;
    final static String parseUserData = "JSON.stringify(user_data)";
    final static String loadUserData = "";
    public final static int USER_DATA_REQUEST = 92;
    String udata;


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


    public String getName() {
        return name;
    }

    public void setUserData(String udata){
        this.udata = udata;
    }

    public boolean init(Fragment fragment){
        if(udata == null || udata.length() == 0) {
        //if(udata == null || udata != null){
            String userdata;
            //get initial user data
            try {
                V8 runtime = V8.createV8Runtime();
                runtime.executeScript(readFile(script));
                userdata = runtime.executeStringScript(parseUserData);
                runtime.release(false);
            } catch (Exception e) {
                //no user data
                userdata = null;
                e.printStackTrace();
            }

            if (userdata != null) {
                Intent intent = new Intent(fragment.getContext(), InputActivity.class);
                intent.putExtra("data", userdata);
                intent.putExtra("name", this.name);
                fragment.startActivityForResult(intent, USER_DATA_REQUEST);
                return false;
            } else {
                return true;
            }
        }else
            return true;
    }

    public void initThread(V8Callback callback, Context context, String udata){
        if(udata != null)
            this.udata = udata;
        if(thread == null) {
            thread = new V8Thread();
            thread.execute(callback, context);
        }
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

    public void runScript(V8Request request){
        this.thread.runScript(request);
    }

    void runOnUiThread(Context context, Runnable r){
        if(context instanceof Activity)
            ((Activity)context).runOnUiThread(r);
        else if(context instanceof FragmentActivity){
            ((FragmentActivity)context).runOnUiThread(r);
        }
    }

    public class V8Thread extends Thread{
        // thread that has v8 runtime

        V8 runtime;
        List<V8Request> queue;
        boolean running = true;
        V8Callback initialCallback;
        Context context;

        @Override
        public void run() {
            if(runtime == null) {
                init();
                if(initialCallback != null && context != null) {
                    runOnUiThread(context, new Runnable() {
                        @Override
                        public void run() {
                            initialCallback.callback(null);
                        }
                    });
                }
            }
            while(running){
                if(queue.size()>0){
                    V8Request r = queue.get(0);
                    try {
                        //run script and get result
                        Object obj = runtime.executeScript(r.getScript());

                        //stringify result if needed
                        if(obj instanceof V8Object){
                            V8Object json = runtime.getObject("JSON");
                            V8Array parameters = new V8Array(runtime).push(obj);
                            String result = json.executeStringFunction("stringify", parameters);
                            parameters.close();
                            json.close();
                            ((V8Object)obj).close();
                            obj = result;
                        }

                        //callback
                        final String res = String.valueOf(obj);
                        if (r.getCallback() != null)
                            runOnUiThread(context, new Runnable() {
                                @Override
                                public void run() {
                                    r.getCallback().callback(res);
                                }
                            });
                    }catch (Exception e){
                        e.printStackTrace();
                        runOnUiThread(context, new Runnable() {
                            @Override
                            public void run() {
                                r.getCallback().error(e);
                            }
                        });
                    }
                    queue.remove(0);
                }
            }
        }

        public void runScript(V8Request r){
            queue.add(r);
        }

        public synchronized void execute(V8Callback callback, Context context) {
            queue = new ArrayList<>();
            this.initialCallback = callback;
            this.context = context;
            super.start();
        }

        public void init(){
            try {
                runtime = V8.createV8Runtime();
                // register callbacks
                // console for debugging
                Class[] consolecls = { String.class };
                Console console = new Console();
                V8Object v8Console = new V8Object(runtime);
                runtime.add("console", v8Console);
                v8Console.registerJavaMethod(console, "log", "log", new Class[]{String.class});
                v8Console.registerJavaMethod(console, "err", "err", new Class[]{String.class});
                v8Console.close();

                // http
                runtime.registerJavaMethod(this, "httpGet", "httpGet", new Class[] {V8Object.class});

                // jsoup
                V8soup soup = new V8soup(runtime);
                V8Object v8soup = new V8Object(runtime);
                runtime.add("jsoup", v8soup);
                v8soup.registerJavaMethod(soup, "parse", "parse", new Class[]{String.class});
                v8soup.registerJavaMethod(soup, "selectFirst", "selectFirst", new Class[]{Integer.class,String.class});
                v8soup.registerJavaMethod(soup, "select", "select", new Class[]{Integer.class,String.class});
                v8soup.registerJavaMethod(soup, "ownText", "ownText", new Class[]{Integer.class});
                v8soup.registerJavaMethod(soup, "attr", "attr", new Class[]{Integer.class,String.class});
                v8soup.close();

                // filesystem

                // run base script

                // initially run script
                runtime.executeScript(readFile(script));

                // set udata
                if(udata!= null && udata.length()>0)
                    runtime.executeScript("var user_data = " + udata + ";");

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public Response httpGet(final V8Object request){
            return client.httpget(runtime, request);
        }

    }
}
