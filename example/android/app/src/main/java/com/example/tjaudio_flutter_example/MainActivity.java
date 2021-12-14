package com.example.tjaudio_flutter_example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tojoy.musicplayer.constants.MusicConstants;
import com.tojoy.musicplayer.listener.MusicPlayerInfoListener;
import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.manager.MusicWindowManager;
import com.tojoy.musicplayer.model.AudioInfo;
import com.tojoy.musicplayer.model.MusicPlayerConfig;
import com.tojoy.musicplayer.utils.BaseLibKit;
import com.tojoy.musicplayer.utils.MusicUtils;
import com.tojoy.musicplayer.utils.NetworkUtil;
import com.tojoy.musicplayer.view.TJMakeSureDialog;

import io.flutter.embedding.android.FlutterActivity;

public class MainActivity extends FlutterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
