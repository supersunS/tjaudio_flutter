package com.tojoy.musicplayer.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tojoy.musicplayer.constants.MusicConstants;
import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.model.AudioInfo;

import uk.co.senab.photoview.R;

/**
 * 返回通知栏音乐窗口工具类
 *
 * @author 侯宪军
 * @date 2021/04/14
 */
public class MusicNotificationUtils {
    //通知栏
    private NotificationManager mNotificationManager;
    //前台进程对象ID
    private int NOTIFICATION_ID = 10099;
    //前台进程默认是开启的,通知交互默认是开启的
    private boolean mForegroundEnable = true, mNotificationEnable = true;
    private Context mContext;
    private boolean isShowMusicNotification = false;
    Notification notification;
    //构造通知栏
    NotificationCompat.Builder builder;
    //默认布局
    RemoteViews defaultRemoteViews;
    //扩展布局
    RemoteViews bigRemoteViews;

    //正在创建通知栏如果
    public static boolean isCreating = false;
    //当前音频或视频播放状态
    int currentPlayerState;
    Service service;

    public boolean isShowMusicNotification() {
        return isShowMusicNotification;
    }

    public void setShowMusicNotification(boolean showMusicNotification) {
        isShowMusicNotification = showMusicNotification;
    }

    private static volatile MusicNotificationUtils mInstance = null;

    public static MusicNotificationUtils getInstance(Context context, boolean notificationEnable) {
        if (null == mInstance) {
            synchronized (MusicPlayerManager.class) {
                if (null == mInstance) {
                    mInstance = new MusicNotificationUtils(context, notificationEnable);
                }
            }
        }
        return mInstance;
    }

    private MusicNotificationUtils(Context context, boolean notificationEnable) {
        mContext = context;
        mNotificationEnable = notificationEnable;
        isCreating = false;
    }

    /**
     * 创建并显示一个通知栏
     *
     * @param notification     通知栏
     * @param notifiid         通知栏ID
     * @param foregroundEnable 是否常驻进程
     */
    public void showNotification(Notification notification, int notifiid, boolean foregroundEnable, Service s) {
        if (null != notification) {
            this.service = s;
            NOTIFICATION_ID = notifiid;
            this.mForegroundEnable = foregroundEnable;
            //通知栏显示中
            this.isShowMusicNotification = true;
            getNotificationManager(mContext).notify(notifiid, notification);
            isCreating = false;
            if (mForegroundEnable) {
                s.startForeground(NOTIFICATION_ID, notification);
            }
        }
    }

    /**
     * 构造一个默认的通知栏并显示
     */
    public  void showNotification(AudioInfo tmpaudioInfo, Service s) {
        if (isCreating || MusicPlayerManager.getInstance().getPlayerState() == MusicConstants.MUSIC_PLAYER_STOP) {
            return;
        }
        if (mNotificationEnable) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(mContext.getApplicationContext());
            boolean isOpen = manager.areNotificationsEnabled();
            if (isOpen) {
                isCreating = true;
                AudioInfo audioInfo = tmpaudioInfo;
                if (null != audioInfo && !TextUtils.isEmpty(audioInfo.getMediaUrl())) {
                    //先准备好歌曲封面Bitmap
                    if (audioInfo.getMediaUrl().startsWith("http:") || audioInfo.getMediaUrl().startsWith("https:")) {
                        Glide.with(mContext.getApplicationContext())
                                .asBitmap()
                                .load(TextUtils.isEmpty(audioInfo.getCoverUrl()) ? audioInfo.getMediaUrl() : audioInfo.getCoverUrl())
                                .into(new SimpleTarget<Bitmap>(120, 120) {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                                        if (null == bitmap) {
                                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_audio_logo);
                                        }
                                        notification = buildNotifyInstance(mContext, audioInfo, bitmap);
                                        showNotification(notification, NOTIFICATION_ID, mForegroundEnable, s);
                                    }
                                });
                    } else {
                        //File
                        Bitmap bitmap;
                        bitmap = MusicImageCache.getInstance().getBitmap(audioInfo.getMediaUrl());
                        //缓存为空，获取音频文件自身封面
                        if (null == bitmap) {
                            bitmap = MusicImageCache.getInstance().createBitmap(audioInfo.getMediaUrl());
                        }
                        //封面为空，使用默认
                        if (null == bitmap) {
                            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_audio_logo);
                        }
                        notification = buildNotifyInstance(mContext, tmpaudioInfo, bitmap);
                        showNotification(notification, NOTIFICATION_ID, mForegroundEnable, s);
                    }
                } else {
                    isCreating = false;
                    LogUtil.d(MusicPlayerManager.TAG, "音乐地址为空");
                }
            } else {
                isCreating = false;
                LogUtil.d(MusicPlayerManager.TAG, "通知栏权限未开启");
                //toSet();
            }
        }
    }
    /**
     * 去设置开启推送通知页面
     */
    private void toSet() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", mContext.getPackageName());
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.0
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", mContext.getPackageName());
            intent.putExtra("app_uid", mContext.getApplicationInfo().uid);
        } else {
            // 其他
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", mContext.getPackageName(), null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }
    /**
     * 构建一个前台进程通知
     *
     * @param audioInfo 播放器正在处理的多媒体对象
     * @param cover     封面
     * @return 通知对象
     */
    private Notification buildNotifyInstance(Context context, AudioInfo audioInfo, Bitmap cover) {
        if (null == audioInfo) {
            return null;
        }
        String name = MusicUtils.getInstance().getAppName(context);
        Log.d(MusicPlayerManager.TAG, "AudioInfo="+audioInfo.getTitle()+MusicPlayerManager.getInstance().getPlayerState());
        //8.0及以上系统需创建通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(MusicConstants.CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setBypassDnd(true); //设置绕过免打扰模式
            channel.canBypassDnd(); //检测是否绕过免打扰模式
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知

            getNotificationManager(context).createNotificationChannel(channel);
        }
        //通知栏根部点击意图
        Intent rootIntent = new Intent(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW);
        rootIntent.putExtra(MusicConstants.MUSIC_KEY_MEDIA_ID, audioInfo.getMediaId());
        PendingIntent pendClickIntent = PendingIntent.getBroadcast(context, 1,
                rootIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deletIntent = new Intent(MusicConstants.MUSIC_INTENT_ACTION_CLICK_DELECT);
        deletIntent.putExtra(MusicConstants.MUSIC_KEY_MEDIA_ID, audioInfo.getMediaId());
        PendingIntent pendDeletIntent = PendingIntent.getBroadcast(context, 1,
                deletIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        String appName = MusicUtils.getInstance().getAppName(context);
      //  defaultRemoteViews       = getDefaultCoustomRemoteView(context,audioInfo,cover);
        bigRemoteViews = getBigCoustomRemoteView(context, audioInfo, cover);
        builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(pendClickIntent)
                .setDeleteIntent(pendDeletIntent)
                .setTicker(name)
                .setSmallIcon(R.drawable.default_audio_logo)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
               // .setOnlyAlertOnce(true)
             //    .setCustomContentView(defaultRemoteViews)
                .setCustomBigContentView(bigRemoteViews)
                .setChannelId(MusicConstants.CHANNEL_ID)
                .setPriority(Notification.PRIORITY_HIGH);
        if (MusicRomUtil.getInstance().isMiui()) {
            builder.setFullScreenIntent(pendClickIntent, false);//禁用悬挂
        } else {
            builder.setFullScreenIntent(null, false);//禁用悬挂
        }
        return builder.build();
    }
//    public synchronized void updatePlayOrPause(int state,int index){
//        if(defaultRemoteViews!=null&&bigRemoteViews!=null){
//            AudioInfo audioInfo =MusicPlayerManager.getInstance().getCurrentPlayerMusic(index);
//            defaultRemoteViews.setImageViewResource(R.id.music_notice_def_btn_pause,getPauseIcon(state));
//            bigRemoteViews.setImageViewResource(R.id.music_notice_def_btn_pause,getPauseIcon(state));
//            bigRemoteViews.setTextViewText(R.id.music_notice_def_title, audioInfo.getAudioName());
//            bigRemoteViews.setTextViewText(R.id.music_notice_def_subtitle, audioInfo.getNickname());
//            defaultRemoteViews.setTextViewText(R.id.music_notice_def_title, audioInfo.getAudioName());
//
//            defaultRemoteViews.setTextViewText(R.id.music_notice_def_subtitle, audioInfo.getNickname());
//
//        }
//        if(notification!=null){
//            showNotification(notification,NOTIFICATION_ID,mForegroundEnable,service);
//        }
//
//    }

    /**
     * 生成并绑定大通知栏View点击事件的默认RemoteView
     *
     * @param audioInfo 音频对象
     * @param cover     封面
     * @return RemoteView
     */
    private RemoteViews getBigCoustomRemoteView(Context context, AudioInfo audioInfo, Bitmap cover) {
        RemoteViews bigRemoteViews = new RemoteViews(context.getPackageName(), R.layout.music_notify_big_controller);
        bigRemoteViews.setImageViewBitmap(R.id.music_notice_def_cover, cover);
        bigRemoteViews.setImageViewResource(R.id.music_notice_def_btn_pause, getPauseIcon(MusicPlayerManager.getInstance().getPlayerState()));
        bigRemoteViews.setTextViewText(R.id.music_notice_def_title, audioInfo.getTitle());
        bigRemoteViews.setTextViewText(R.id.music_notice_def_subtitle, audioInfo.getAuther());
        //上一首
        bigRemoteViews.setOnClickPendingIntent(R.id.music_notice_def_btn_last,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST));
        //下一首
        bigRemoteViews.setOnClickPendingIntent(R.id.music_notice_def_btn_next,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT));
        //暂停、开始
        bigRemoteViews.setOnClickPendingIntent(R.id.music_notice_def_btn_pause,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE));
        //关闭
        bigRemoteViews.setOnClickPendingIntent(R.id.music_notice_def_btn_close,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE));
        return bigRemoteViews;
    }

    /**
     * 生成并绑定点击事件的默认RemoteView
     *
     * @param audioInfo 音频对象
     * @param cover     封面
     * @return RemoteView
     */
    private RemoteViews getDefaultCoustomRemoteView(Context context, AudioInfo audioInfo, Bitmap cover) {
        RemoteViews defaultremoteviews = new RemoteViews(context.getPackageName(), R.layout.music_notify_default_controller);
        defaultremoteviews.setImageViewBitmap(R.id.music_notice_def_cover, cover);
        defaultremoteviews.setImageViewResource(R.id.music_notice_def_btn_pause, getPauseIcon(MusicPlayerManager.getInstance().getPlayerState()));
        defaultremoteviews.setTextViewText(R.id.music_notice_def_title, audioInfo.getTitle());
        defaultremoteviews.setTextViewText(R.id.music_notice_def_subtitle, audioInfo.getAuther());
        //上一首
        defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_last,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST));
        //下一首
        defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_next,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT));
        //暂停、开始
        defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_pause,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE));
        //关闭
        defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_close,
                getClickPending(context, MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE));
        return defaultremoteviews;
    }

    /**
     * 生成待处理广播意图
     *
     * @param action 事件
     * @return 点击意图
     */
    private PendingIntent getClickPending(Context context, String action) {
        Intent lastIntent = new Intent(action);
        PendingIntent lastPendIntent = PendingIntent.getBroadcast(context,
                1, lastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return lastPendIntent;
    }

    /**
     * 通知栏开始、暂停 按钮
     *
     * @param playerState 播放状态
     * @return 播放状态对应的RES
     */
    private int getPauseIcon(int playerState) {

        switch (playerState) {
            case MusicConstants.MUSIC_PLAYER_PREPARE:
            case MusicConstants.MUSIC_PLAYER_PLAYING:
            case MusicConstants.MUSIC_PLAYER_BUFFER:
            case MusicConstants.VEDIO_PLAYER_PLAYING:
                return R.drawable.audio_icon_pause;
            case MusicConstants.MUSIC_PLAYER_STOP:
            case MusicConstants.MUSIC_PLAYER_ERROR:
            case MusicConstants.VEDIO_PLAYER_PAUSE:
                return R.drawable.audio_icon_next;
        }

        return R.drawable.audio_icon_play;
    }

    /**
     * 构造通知栏Manager
     *
     * @return NotificationManager
     */
    private synchronized NotificationManager getNotificationManager(Context context) {
        if (null == mNotificationManager) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * 清除通知，常驻进程依然保留(如果开启)
     */
    public void cleanNotification() {
        this.isCreating = false;
        //通知栏关闭
        this.isShowMusicNotification = false;
        getNotificationManager(mContext).cancel(NOTIFICATION_ID);
    }

    /**
     * 清除通知，常驻进程依然保留(如果开启)
     */
    public void cleanNotification(int notifiid) {
        //通知栏关闭
        this.isShowMusicNotification = false;
        getNotificationManager(mContext).cancel(notifiid);
    }


    public void startForegroundService(Service service){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;
             NotificationManager manager;
             String name =  MusicUtils.getInstance().getAppName(service);
            channel = new NotificationChannel(MusicConstants.CHANNEL_ID,name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.setBypassDnd(true); //设置绕过免打扰模式
            channel.canBypassDnd(); //检测是否绕过免打扰模式
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
            manager = (NotificationManager)service. getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(service.getApplicationContext(),MusicConstants.CHANNEL_ID).build();
            service.startForeground(NOTIFICATION_ID, notification);
        }
    }
}
