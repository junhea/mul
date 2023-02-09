package io.github.junhea.mul;


import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpClient {
    OkHttpClient client;
    public HttpClient(){
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            List<CipherSuite> cipherSuites = new ArrayList<>();
            cipherSuites.addAll(ConnectionSpec.MODERN_TLS.cipherSuites());
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA);
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA);

            ConnectionSpec legacyTls = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .cipherSuites(cipherSuites.toArray(new CipherSuite[0]))
                    .build();

            this.client = new OkHttpClient.Builder()
                    .connectionSpecs(Arrays.asList(legacyTls, ConnectionSpec.CLEARTEXT))
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
        } else {
            this.client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
        }
    }

    public io.github.junhea.mul.model.source.Response get(Request r){
        try (okhttp3.Response res = client.newCall(r).execute()) {
            return new io.github.junhea.mul.model.source.Response(res);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Response getRaw(Request r){
        try {
            return client.newCall(r).execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
