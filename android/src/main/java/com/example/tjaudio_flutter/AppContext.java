package com.example.tjaudio_flutter;

import android.app.Application;

import com.tojoy.musicplayer.utils.BaseLibKit;

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BaseLibKit.init(this, "1.0.0");
    }
}
