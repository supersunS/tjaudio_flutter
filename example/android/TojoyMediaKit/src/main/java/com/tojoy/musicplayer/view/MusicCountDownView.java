package com.tojoy.musicplayer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tojoy.musicplayer.model.MusicStatus;

import uk.co.senab.photoview.R;

public class MusicCountDownView {
    private Context mContext;
    private View mView;
    //礼物底部文字
    private TextView mTvgift;
    //倒计时礼物布局
    private RelativeLayout mRlGiftMain;

    private long mAnimTime;

    //礼物图片
    private ImageView ivGiftIcon;

    private OnDownFinishListener finishListener;
   private IndicatorView indicatorView;

    public void setFinishListener(OnDownFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    private MusicCirleProgressView mCircleProgress3;
    /**
     * 倒计时构造函数
     * @param context
     */
    public MusicCountDownView(Context context){
        mContext = context;
    }
    /**
     * 返回礼物倒计时view
     * @return
     */
    public View getView() {
        if (mView == null) {
            LayoutInflater aLayoutInflater = LayoutInflater.from(mContext);
            this.mView = aLayoutInflater.inflate(R.layout.view_music_countdown_gift_item, null);
        }
        initView();
        return mView;
    }
    private void initView() {
        mRlGiftMain= mView.findViewById(R.id.rl_gift_main);

        mCircleProgress3 = mView.findViewById(R.id.circle_progress_bar3);

        indicatorView = mView.findViewById(R.id.music_indicator1);

    }

    public void startCountDown(Long mAnimTime){
        mCircleProgress3.setAnimTime(mAnimTime);
        mCircleProgress3.setValue();
        indicatorView.startAnimator();
        updateMusicIndicatorBg(true);
    }
    public void updateData(MusicStatus musicStatus) {
        if(null!=mCircleProgress3){
            mCircleProgress3.updateData(musicStatus);
            indicatorView.updateData(musicStatus);
        }
    }
    public void onResume(){
        if(null!=mCircleProgress3){
            mCircleProgress3.onResume();
            indicatorView.onResume();
        }
    }

    public void onPause(){
        if(null!=mCircleProgress3){
            mCircleProgress3.onPause();
            indicatorView.onPause();
        }
    }
    public void onVisible() {
        if(null!=mCircleProgress3){
            mCircleProgress3.onVisible();
            indicatorView.onVisible();
        }
    }

    public void onInvisible() {
        if(null!=mCircleProgress3){
            mCircleProgress3.onInvisible();
            indicatorView.onInvisible();
        }
    }
    public void onDestroy(){
        if(null!=mCircleProgress3){
            mCircleProgress3.onDestroy();
            indicatorView.onDestroy();
        }
    }
    /**
     * 停止旋转动画
     */
    public void stopAnimator(){
        if(null!=mCircleProgress3){
            mCircleProgress3.stopAnimator();
            indicatorView.stopAnimator();
            updateMusicIndicatorBg(false);
        }
    }

    /**
     * 更新音柱背景色
     * @param isShowPlayControll
     */
    public void updateMusicIndicatorBg(boolean isShowPlayControll){
        indicatorView.updateBackground(isShowPlayControll);
    }

    /**
     * 倒计时完成后回调接口
     */
    public interface OnDownFinishListener {
        void progressFinished();
    }
}
