package com.tojoy.musicplayer.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.model.MusicStatus;
import com.tojoy.musicplayer.utils.AppUtils;
import com.tojoy.musicplayer.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.photoview.R;

public class IndicatorView extends RelativeLayout {
    private Context mContext;
    private View mView;
    private LinearLayout rl_music_main;
    private View indicator01;
    private View indicator02;
    private View indicator03;
    private View indicator04;
    private View indicator05;
    private long time  =1000;
    private List<ValueAnimator> animList;
    //是否准备要开始动画
    private boolean readyPlay=false;
    //是否允许悬浮窗显示
    private boolean isVisible=true;
    private int maxHeight = 12;
    private int middleHeight = maxHeight/2;
    private int smallHeight = maxHeight/4;

    public IndicatorView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initView();
    }
    /**
     * 返回礼物倒计时view
     * @return
     */
    public void getView() {
        if (mView == null) {
            LayoutInflater aLayoutInflater = LayoutInflater.from(mContext);
            this.mView = aLayoutInflater.inflate(R.layout.view_music_indicator, null);
            indicator01 = mView.findViewById(R.id.indicator01);
            indicator02 = mView.findViewById(R.id.indicator02);
            indicator03 = mView.findViewById(R.id.indicator03);
            indicator04 = mView.findViewById(R.id.indicator04);
            indicator05 = mView.findViewById(R.id.indicator05);
            rl_music_main = mView.findViewById(R.id.rl_music_main);
            initAnim();
            updateBackground(false);
        }
    }
    private void initView() {
        getView();
        this.addView(mView);
    }

    public void initAnim(){
        animList = new ArrayList<>();
        performAnim1();
        performAnim2();
        performAnim3();
    }

    public void performAnim1(){

        int[] floatArray = new int[]{AppUtils.dpToPx(smallHeight),AppUtils.dpToPx(middleHeight),AppUtils.dpToPx(maxHeight),AppUtils.dpToPx(middleHeight),AppUtils.dpToPx(smallHeight)};
        //属性动画对象
        ValueAnimator va ;

        va = ValueAnimator.ofInt(floatArray);
        va.setDuration(time);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(int)valueAnimator.getAnimatedValue();
                //动态更新view的高度
                indicator01.getLayoutParams().height = h;
                indicator01.requestLayout();
                indicator05.getLayoutParams().height = h;
                indicator05.requestLayout();
            }
        });
        animList.add(va);
    }
    public void performAnim2(){

        int[] floatArray = new int[]{AppUtils.dpToPx(middleHeight),AppUtils.dpToPx(maxHeight),AppUtils.dpToPx(middleHeight),AppUtils.dpToPx(smallHeight),AppUtils.dpToPx(middleHeight)};
        //属性动画对象
        ValueAnimator va ;

        va = ValueAnimator.ofInt(floatArray);
        va.setDuration(time);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(int)valueAnimator.getAnimatedValue();
                //动态更新view的高度
                indicator02.getLayoutParams().height = h;
                indicator02.requestLayout();
                indicator04.getLayoutParams().height = h;
                indicator04.requestLayout();
            }
        });
        animList.add(va);
    }
    public void performAnim3(){

        int[] floatArray = new int[]{AppUtils.dpToPx(maxHeight),AppUtils.dpToPx(middleHeight),AppUtils.dpToPx(smallHeight),AppUtils.dpToPx(middleHeight),AppUtils.dpToPx(maxHeight)};
        //属性动画对象
        ValueAnimator va ;

        va = ValueAnimator.ofInt(floatArray);
        va.setDuration(time);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(int)valueAnimator.getAnimatedValue();
                //动态更新view的高度
                indicator03.getLayoutParams().height = h;
                indicator03.requestLayout();
            }
        });
        animList.add(va);
    }

    public void startAnimator( ) {
        if(animList==null){
            if(animList!=null&&animList.size()>0){
                for(ValueAnimator valueAnimator : animList){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (valueAnimator.isPaused()) {
                            valueAnimator.resume();
                        } else {
                            if(!valueAnimator.isRunning()){
                                valueAnimator.start();
                            }
                        }
                    }else{
                        valueAnimator.start();
                    }
                }
            }
        }
        else{
            playDiscAnimator();
        }
    }

    /**
     * 播放唱盘动画
     */
    private void playDiscAnimator() {
        if(animList!=null&&animList.size()>0){
            for(ValueAnimator valueAnimator : animList){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (valueAnimator.isPaused()) {
                        valueAnimator.resume();
                    } else {
                        if(!valueAnimator.isRunning()){
                            valueAnimator.start();
                        }
                    }
                }else{
                    valueAnimator.start();
                }
            }
        }
    }
    /**
     * 暂停旋转动画
     */
    private void pausAnimator() {
        if(animList!=null&&animList.size()>0){
            for(ValueAnimator mAnimator : animList){
                if(null!=mAnimator){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mAnimator.pause();
                    }else{
                        mAnimator.cancel();
                        mAnimator=null;
                    }
                }
            }
        }
    }

    /**
     * 停止旋转动画
     */
    public void stopAnimator(){
        if(animList!=null&&animList.size()>0){
            for(ValueAnimator mAnimator : animList){
                if(null!=mAnimator){
                    mAnimator.cancel();
                    mAnimator=null;
                }
            }
        }

    }

    /**
     * 刷新数据
     * @param musicStatus
     */
    public void updateData(MusicStatus musicStatus){

        //ID更新
        if(Integer.valueOf(musicStatus.getId())>0){
            IndicatorView.this.setTag(musicStatus.getId());
        }
        final int playerStatus = musicStatus.getPlayerStatus();
        IndicatorView.this.post(new Runnable() {
            @Override
            public void run() {
                //停止
                if(MusicStatus.PLAYER_STATUS_STOP==playerStatus){
                    LogUtil.d(MusicPlayerManager.TAG,"update，播放器停止");
                    readyPlay=false;
                    stopAnimator();
                    //暂停
                }else if(MusicStatus.PLAYER_STATUS_PAUSE==playerStatus){
                    LogUtil.d(MusicPlayerManager.TAG,"update，播放器暂停");
                    readyPlay=false;
                    pausAnimator();
                    //播放
                }else if(MusicStatus.PLAYER_STATUS_START==playerStatus
                        ||MusicStatus.PLAYER_STATUS_PREPARED==playerStatus){
                    LogUtil.d(MusicPlayerManager.TAG,"update，音柱播放器开始");
                    readyPlay=true;
                    startAnimator();
                    //销毁
                }else if(MusicStatus.PLAYER_STATUS_DESTROY==playerStatus){
                    LogUtil.d(MusicPlayerManager.TAG,"update，播放器销毁");
                    readyPlay=false;

                    stopAnimator();
                    //失败
                }else if(MusicStatus.PLAYER_STATUS_ERROR==playerStatus){
                    LogUtil.d(MusicPlayerManager.TAG,"update，播放器收到无效播放地址/没有获取到音频焦点");
                    readyPlay=false;
                    stopAnimator();
                }
            }
        });
    }

    public void onResume(){
        if(readyPlay){
            startAnimator();
        }
    }

    public void onPause(){
        stopAnimator();
    }
    public void onVisible() {
        isVisible=true;
        onResume();
    }

    public void onInvisible() {
        isVisible=false;
        stopAnimator();
    }
    public void onDestroy(){
        readyPlay=false;
        isVisible=false;
        stopAnimator();
        mContext=null;
    }

    /**
     * 根据播放器控制栏是否显示控制音柱背景与色值
     * @param isShowPlayControll
     */
    public void updateBackground(boolean isShowPlayControll){
     if(isShowPlayControll){
          indicator01.setBackgroundResource(R.drawable.shape_music_indicator_bg);
          indicator02.setBackgroundResource(R.drawable.shape_music_indicator_bg);
         indicator03.setBackgroundResource(R.drawable.shape_music_indicator_bg);
          indicator04.setBackgroundResource(R.drawable.shape_music_indicator_bg);
         indicator05.setBackgroundResource(R.drawable.shape_music_indicator_bg);
         rl_music_main.setBackgroundResource(R.drawable.shape_music_player_indicator_bg);
     }
     else{
         indicator01.setBackgroundResource(R.drawable.shape_music_indicator_gray_bg);
         indicator02.setBackgroundResource(R.drawable.shape_music_indicator_gray_bg);
         indicator03.setBackgroundResource(R.drawable.shape_music_indicator_gray_bg);
         indicator04.setBackgroundResource(R.drawable.shape_music_indicator_gray_bg);
         indicator05.setBackgroundResource(R.drawable.shape_music_indicator_gray_bg);
         rl_music_main.setBackgroundResource(R.drawable.shape_music_player_indicator_white_bg);
     }
    }
}
