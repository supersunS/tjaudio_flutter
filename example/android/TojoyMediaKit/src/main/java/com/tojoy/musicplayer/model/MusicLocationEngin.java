package com.tojoy.musicplayer.model;

import android.content.Context;

import com.tojoy.musicplayer.listener.OnResultCallBack;
import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.manager.MusicSubjectObservable;
import com.tojoy.musicplayer.utils.MediaUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MusicLocationEngin extends  BaseEngin{
    private Subscription mSubscribe;
    private static volatile MusicLocationEngin mInstance = null;
    public static MusicLocationEngin getInstance() {
        if(null==mInstance){
            synchronized (MusicLocationEngin.class) {
                if (null == mInstance) {
                    mInstance = new MusicLocationEngin();
                }
            }
        }
        return mInstance;
    }

    private MusicLocationEngin(){

    }

    /**
     * 获取音频列表
     * @param callBack 回调监听器
     */
    public  void getLocationAudios(final Context context, final OnResultCallBack callBack){
        //内存中已经存在本地歌曲列表，不再重复查询
        List<AudioInfo> audioInfos = MediaUtils.getInstance().getLocationMusic();
        if(null!=audioInfos){
            if(null!=callBack){
                List<AudioInfo> medias=new ArrayList<>();
                medias.addAll(audioInfos);
                if(medias.size()>0){
                    callBack.onResponse(medias);
                }else{
                    callBack.onError(API_RESULT_EMPTY,API_EMPTY);
                }
            }
            return;
        }
        mContext=context;
        mSubscribe =rx.Observable
                .just(context)
                .map(new Func1<Context, List<AudioInfo>>() {
                    @Override
                    public List<AudioInfo> call(Context cts) {
                        return MediaUtils.getInstance().queryLocationMusics(cts);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<AudioInfo>>() {
                    @Override
                    public void call(List<AudioInfo> data) {
                        if(null!=data&&data.size()>0){
                            callBack.onResponse(data);
                        }else{
                            callBack.onError(API_RESULT_EMPTY,API_EMPTY);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mSubscribe){
            mSubscribe.unsubscribe();
            mSubscribe=null;
        }
    }
}
