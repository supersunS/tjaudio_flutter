package com.example.tjaudio_flutter;


import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tojoy.musicplayer.constants.MusicConstants;
import com.tojoy.musicplayer.listener.MusicPlayerEventListener;
import com.tojoy.musicplayer.listener.MusicPlayerInfoListener;
import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.model.AudioInfo;
import com.tojoy.musicplayer.model.MusicPlayerConfig;
import com.tojoy.musicplayer.utils.BaseLibKit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMessageCodec;

/**
 * TjaudioFlutterPlugin
 */
public class TjaudioFlutterPlugin implements FlutterPlugin, MethodCallHandler {

    final static String CHANNEL_NAME = "com.flutterplugin.tj/flutter_audioPlay";
    final static String CHANNEL_NAME_message = "com.flutterplugin.tj/flutter_audioPlay_message";
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private BasicMessageChannel _messagechannel;
    private EventChannel _eventChannel;
    private EventChannel.EventSink _eventSink;
    private TimerTask _timerTask;
    private Timer _timer;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        //音乐播放器配置
        MusicPlayerConfig config = MusicPlayerConfig.Build()
                //设置默认的闹钟定时关闭模式，优先取用户设置
                .setDefaultAlarmModel(MusicConstants.MUSIC_ALARM_MODEL_0)
                //设置默认的循环模式，优先取用户设置
                .setDefaultPlayModel(MusicConstants.MUSIC_MODEL_LOOP);
        //音乐播放器初始化
        MusicPlayerManager.getInstance()
                //内部存储初始化
                .init(BaseLibKit.getContext())
                //应用播放器配置
                .setMusicPlayerConfig(config)
                //通知栏交互，默认开启
                .setNotificationEnable(true)
                //常驻进程开关，默认开启
                .setLockForeground(true)
                .setPlayInfoListener(new MusicPlayerInfoListener() {
                    //此处自行存储播放记录
                    @Override
                    public void onPlayMusiconInfo(AudioInfo musicInfo, int position) {
                    }
                })
                //重载方法，初始化音频媒体服务,成功之后如果系统还在播放音乐，则创建一个悬浮窗承载播放器
                .initialize(BaseLibKit.getContext());
        _messagechannel = new BasicMessageChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL_NAME_message, StandardMessageCodec.INSTANCE);
        _messagechannel.setMessageHandler(new BasicMessageChannel.MessageHandler() {
            @Override
            public void onMessage(@Nullable Object message, @NonNull BasicMessageChannel.Reply reply) {
                if (!(message instanceof Map)) {
                    return;
                }
                Log.d("message", message.toString());
                Map<String, String> map = (Map<String, String>) message;
                String methode = "";
                Object arguments = "";
                if (map.containsKey("methode")) {
                    methode = map.get("methode");
                }
                if (map.containsKey("arguments")) {
                    arguments = map.get("arguments");
                }
                Log.d("Method Name", methode);
                Log.d("arguments value", arguments.toString());
                if (methode.equals("imageName")) {
                    if (arguments instanceof String) {
                        Bitmap bitmap = tj_imageName(arguments.toString());
                        byte[] resByte = getBitmapByte(bitmap);
                        if (resByte.length > 0) {
                            Map<String, byte[]> resultMap = new HashMap<>();
                            resultMap.put("image", resByte);
                            reply.reply(resultMap);
                        }
                    }

                } else if (methode.equals("openBackGround")) {

                } else if (methode.equals("playWithIndex")) {
                    MusicPlayerManager.getInstance().startPlayMusic(Integer.valueOf(arguments.toString()));
                    Map<String, Boolean> resultMap = new HashMap<>();
                    resultMap.put("result", true);
                    reply.reply(resultMap);
                    startTimer();
                } else if (methode.equals("audioSourceData")) {
                    if (arguments instanceof List) {
                        Gson gson = new Gson();
                        List<AudioInfo> audioInfoList = gson.fromJson(gson.toJson(arguments), new TypeToken<List<AudioInfo>>() {
                        }.getType());
                        MusicPlayerManager.getInstance().updateMusicPlayerData(audioInfoList);
                        MusicPlayerManager.getInstance().copyCacheMusicList();
                    }
                    Map<String, Boolean> resultMap = new HashMap<>();
                    resultMap.put("result", true);
                    reply.reply(resultMap);
                } else if (methode.equals("getAudioIsPlaying")) {
                    boolean res = MusicPlayerManager.getInstance().isPlaying();
                    Map<String, Boolean> resultMap = new HashMap<>();
                    resultMap.put("result", res);
                    reply.reply(resultMap);
                } else if (methode.equals("resume")) {
                    MusicPlayerManager.getInstance().play();
                    startTimer();
                } else if (methode.equals("pause")) {
                    MusicPlayerManager.getInstance().pause();
                    cancelTimer();
                } else if (methode.equals("destoryView")) {
                    MusicPlayerManager.getInstance().onStop();
                    cancelTimer();
                } else if (methode.equals("autoNextAudio")) {

                } else if (methode.equals("nextAudio")) {
                    MusicPlayerManager.getInstance().playNextMusic();
                    startTimer();
                } else if (methode.equals("setAudioPlayStateChangeListener")) {
                    MusicPlayerManager.getInstance().addOnPlayerEventListener(new MusicPlayerEventListener() {
                        @Override
                        public void onMusicPlayerState(int playerState, String message) {
                            Map<String, Boolean> resultMap = new HashMap<>();
                            resultMap.put("audioState", playerState == 4);

                            Message message1 = new Message();
                            message1.obj = resultMap;
                            handler.sendMessage(message1);
                        }

                        @Override
                        public void onPrepared(long totalDurtion) {

                        }

                        @Override
                        public void onBufferingUpdate(int percent) {

                        }

                        @Override
                        public void onPlayMusiconInfo(AudioInfo musicInfo, int position) {

                        }

                        @Override
                        public void onMusicPathInvalid(AudioInfo musicInfo, int position) {

                        }

                        @Override
                        public void onTaskRuntime(long totalDurtion, long currentDurtion, int bufferProgress) {

                            Float progress = 0f;
                            if (totalDurtion > 0) {
                                progress = Float.valueOf(currentDurtion) / Float.valueOf(totalDurtion);
                            }
                            Map<String, Float> resultMap = new HashMap<>();
                            resultMap.put("progress", progress);

                            Message message1 = new Message();
                            message1.obj = resultMap;
                            handler.sendMessage(message1);

                        }
                    });
                }
            }
        });

        _eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL_NAME);
        _eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                _eventSink = events;
            }

            @Override
            public void onCancel(Object arguments) {

            }
        });
    }

    // 子线程
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            _eventSink.success(msg.obj);
        }
    };

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        _messagechannel.setMessageHandler(null);
        _eventChannel.setStreamHandler(null);
    }

    public Bitmap tj_imageName(String imageName) {
        ApplicationInfo appInfo = BaseLibKit.getContext().getApplicationInfo();
        int resID = BaseLibKit.getContext().getResources().getIdentifier(imageName, "drawable", appInfo.packageName);
        Bitmap imageBitmap = BitmapFactory.decodeResource(BaseLibKit.getContext().getResources(), resID);
        return imageBitmap;
    }

    public byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private void startTimer() {
        if (_timer != null || _timerTask != null) {
            cancelTimer();
        }

        _timer = new Timer();
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (MusicPlayerManager.getInstance().isPlaying()) {
                    MusicPlayerManager.getInstance().onCheckedCurrentPlayTask();
                }
            }
        };
        _timer.schedule(_timerTask, 500L, 500L);

    }

    private void cancelTimer() {
        if (_timer != null || _timerTask != null) {
            _timerTask.cancel();
            _timer.cancel();
            _timerTask = null;
            _timer = null;
        }
    }

}

