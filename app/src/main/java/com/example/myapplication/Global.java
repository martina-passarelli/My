package com.example.myapplication;

import android.app.Application;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
/*
    Questa classe serve ad implementare il caching delle immagine di
    dello storage di firebase
 */
public class Global extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        //indicatore vicino la foto che indica dove si trova la foto al
        //momento del caricamento
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}
