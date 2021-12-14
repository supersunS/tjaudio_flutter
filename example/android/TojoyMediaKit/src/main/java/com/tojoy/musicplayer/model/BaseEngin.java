package com.tojoy.musicplayer.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tojoy.musicplayer.listener.OnResultCallBack;

import java.util.Map;

public class BaseEngin {

    protected static final String TAG = "BaseEngin";
    protected Context mContext;
    protected Handler mHandler;
    //数据为空
    public static final String API_EMPTY = "没有数据";

    //数据为空
    public static final int API_RESULT_EMPTY = 3002;
    protected Handler getHandler() {
        if(null==mHandler){
            mHandler=new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }



    /**
     * 对应生命周期调用
     */
    public void onDestroy(){
        mContext=null;
        if(null!=mHandler){
            mHandler.removeCallbacksAndMessages(null);
            mHandler.removeMessages(0);
            mHandler=null;
        }

    }
}
