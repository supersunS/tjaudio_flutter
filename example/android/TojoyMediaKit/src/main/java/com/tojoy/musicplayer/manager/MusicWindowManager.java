package com.tojoy.musicplayer.manager;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.tojoy.musicplayer.model.MusicStatus;
import com.tojoy.musicplayer.utils.AppUtils;
import com.tojoy.musicplayer.utils.LogUtil;
import com.tojoy.musicplayer.utils.MusicUtils;
import com.tojoy.musicplayer.view.MusicPlayerWindow;
import com.tojoy.musicplayer.view.MusicWindowTrash;

import java.lang.reflect.Method;

/**
 * houxianjun
 * 2021/03/31
 * WindowManager
 * 音频播放器悬浮窗
 */
public class MusicWindowManager {

    private static final String TAG = "MusicWindowManager";
    private static volatile MusicWindowManager mInstance;
    //悬浮窗口
    private MusicPlayerWindow mMusicPlayerWindow;
    private static WindowManager mWindowManager;
    public static int WINDOW_HEIGHT=52;
    public static int WINDOW_WIDTH_MAX=213;
    public static int WINDOW_WIDTH_MIN=52;
    public static MusicWindowManager getInstance() {
        if(null==mInstance){
            synchronized (MusicWindowManager.class) {
                if (null == mInstance) {
                    mInstance = new MusicWindowManager();
                }
            }
        }
        return mInstance;
    }

    private MusicWindowManager(){}

    /**
     * 添加一个播放器View到窗口
     * @param context Application全局上下文
     */
    public synchronized MusicWindowManager createMiniJukeBoxToWindown(Context context) {
        int screenHeight = AppUtils.getScreenHeight(context);
        createMiniJukeBoxToWindown(context,30,screenHeight-screenHeight/4);
        return mInstance;
    }

    /**
     * 添加一个播放器View到窗口,默认圆形播放器位于屏幕右下角
     * @param context Application全局上下文
     * @param offsetPixelX 圆形播放器X轴偏移量 单位像素 -1:使用默认
     * @param offsetPixelY 圆形播放器Y轴偏移量 单位像素 -1:使用默认
     */
    public synchronized void createMiniJukeBoxToWindown(Context context, int offsetPixelX, int offsetPixelY) {
        if(!isWindowShowing()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse( "package:"+ MusicUtils.getInstance().getPackageName(context)));
                    context.startActivity(intent);
                } else {
                    addMiniJukeBoxToWindown(context,offsetPixelX,offsetPixelY);
                }
            }else {
                addMiniJukeBoxToWindown(context,offsetPixelX,offsetPixelY);
            }
        }
    }

    /**
     * 添加一个View到窗口,默认位置是位于屏幕左上角，自行指定X、Y轴偏移量
     * @param context 全局上下文
     * @param offsetPixelX X轴偏移量 单位像素 -1:使用默认
     * @param offsetPixelY Y轴偏移量 单位像素 -1:使用默认
     */
    private synchronized void addMiniJukeBoxToWindown(Context context, int offsetPixelX, int offsetPixelY) {
        //添加唱片机到窗口前，先添加一个垃圾桶到屏幕
        //addTrashToWindow(context);
        if (null== mMusicPlayerWindow) {
            int screenWidth = MusicUtils.getInstance().getScreenWidth(context);
            int screenHeight = MusicUtils.getInstance().getScreenHeight(context);
            WindowManager windowManager = getWindowManager(context);
            mMusicPlayerWindow = new MusicPlayerWindow(context);
            WindowManager.LayoutParams jukeBoxLayoutParams = new WindowManager.LayoutParams();
            //绑定窗口和垃圾桶
            mMusicPlayerWindow.setWindowLayoutParams(jukeBoxLayoutParams);
           // mMusicPlayerWindow.setTrashWindow(mWindowTrash);
            mMusicPlayerWindow.setWindowManager(windowManager);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                jukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                jukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
                jukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                jukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            //FLAG_NOT_FOCUSABLE：屏蔽此Window所有触摸焦点
            //FLAG_LAYOUT_IN_SCREEN：使用整个Window区域，包括状态栏在内
            //FLAG_NOT_TOUCH_MODAL：自己窗口的焦点自己处理，其他窗口的焦点其他Window处理
            //FLAG_WATCH_OUTSIDE_TOUCH：非此Window的触摸事件，在此Window只能接受到一次特殊的事件，需要配合FLAG_NOT_TOUCH_MODAL设置
            jukeBoxLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //背景透明
            jukeBoxLayoutParams.format = PixelFormat.RGBA_8888;
            int viewW = MusicUtils.getInstance().dpToPxInt(context, WINDOW_WIDTH_MIN);
            int viewH = MusicUtils.getInstance().dpToPxInt(context, WINDOW_HEIGHT);
            //默认位于屏幕的左上角，具体位置定位定传X、Y偏移量
            jukeBoxLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            //悬浮窗口显示宽度
            jukeBoxLayoutParams.width = viewW;
            //悬浮窗口显示高度
            jukeBoxLayoutParams.height = viewH;
            if(offsetPixelX>-1){
                jukeBoxLayoutParams.x=offsetPixelX;
            }else{
                //X轴偏移量=screenWidth-(ViewHeight+marginBottomHeight)
                int dpToPxIntX = MusicUtils.getInstance().dpToPxInt(context, 15f)+ viewW;
                int offsetX = screenWidth - dpToPxIntX;
                jukeBoxLayoutParams.x=offsetX;
                LogUtil.d(TAG,"X:"+offsetX);
            }
            if(offsetPixelY>-1){
                jukeBoxLayoutParams.y=offsetPixelY;
            }else{
                //Y轴偏移量=ScreenHeight-(ViewHeight+statusBarHeight+marginBottomHeight)
                int dpToPxIntY = MusicUtils.getInstance().dpToPxInt(context, 80f)+ viewH;
                int offsetY = screenHeight - dpToPxIntY;
                jukeBoxLayoutParams.y=offsetY;
                LogUtil.d(TAG,"Y:"+offsetY);
            }
            windowManager.addView(mMusicPlayerWindow, jukeBoxLayoutParams);
        }
    }



    /**
     * 将小悬浮窗从屏幕上移除。
     * @param context 全局上下文
     */
    public synchronized void removeAllWindowView(Context context) {
        if(null!= mMusicPlayerWindow){
            mMusicPlayerWindow.onDestroy();
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mMusicPlayerWindow);
            mMusicPlayerWindow = null;
        }
    }

    /**
     * 此应用是否拥有悬浮窗权限
     * @param context 全局上下文
     * @return 为true表示拥有悬浮窗权限
     */
    public boolean haveWindownPermission(Context context){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * @param context 全局上下文
     * @return true 允许  false禁止
     */
    public boolean checkAlertWindowsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示正在加载中
     */
    public void updateWindowStatus(MusicStatus musicStatus) {
        if(mMusicPlayerWindow !=null){
            mMusicPlayerWindow.updateData(musicStatus);
        }
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 窗口是否有唱片机悬浮窗口显示
     * @return 为true标识当前窗口已经存在悬浮窗
     */
    public boolean isWindowShowing() {
        return null!= mMusicPlayerWindow;
    }

    /**
     * 悬浮窗中迷你唱片机动画开启
     */
    public void onResume() {
        if(null!= mMusicPlayerWindow) {
            mMusicPlayerWindow.onResume();
        }
    }

    /**
     * 悬浮窗中迷你唱片机动画关闭
     */
    public void onPause() {
        if(null!= mMusicPlayerWindow) {
            mMusicPlayerWindow.onPause();
        }
    }

    /**
     * MINIJukeBox悬浮窗可见
     */
    public void onVisible() {
        if(null!= mMusicPlayerWindow) {
            mMusicPlayerWindow.onVisible();
        }
    }

    /**
     * MINIJukeBox悬浮窗可见
     * @param audioID 音频ID
     */
    public void onVisible(long audioID) {
        if(null!= mMusicPlayerWindow) {
            mMusicPlayerWindow.onVisible(audioID);
        }
    }

    /**
     * MINIJukeBox悬浮窗不可见
     */
    public void onInvisible() {
        if(null!= mMusicPlayerWindow) {
            mMusicPlayerWindow.onInvisible();
        }
    }

    /**
     * 组件中对应函数调用
     */
    public void onDestroy() {
        if(null!= mMusicPlayerWindow &&null!=mWindowManager){
            mMusicPlayerWindow.onDestroy();
            mWindowManager.removeViewImmediate(mMusicPlayerWindow);
            mMusicPlayerWindow =null;
        }
        mWindowManager=null;mInstance=null;
    }
}
