package com.tojoy.musicplayer.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tojoy.musicplayer.constants.MusicConstants;
import com.tojoy.musicplayer.listener.MusicPlayerEventListener;
import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.manager.MusicSubjectObservable;
import com.tojoy.musicplayer.manager.MusicWindowManager;
import com.tojoy.musicplayer.model.AudioInfo;
import com.tojoy.musicplayer.model.MusicStatus;
import com.tojoy.musicplayer.utils.AppUtils;
import com.tojoy.musicplayer.utils.LogUtil;
import com.tojoy.musicplayer.utils.MusicUtils;


import java.util.Observable;
import java.util.Observer;

import uk.co.senab.photoview.R;

public class MusicPlayerWindow extends FrameLayout implements MusicPlayerEventListener, Observer {

    private static final String TAG = "MusicPlayerWindow";
    //状态栏高度屏幕的宽高,悬浮球停靠在屏幕边界的最小距离
    private int mStatusBarHeight, mScreenWidth, mScreenHeight,mHorMiniMagin;
    //震动
    //private Vibrator mVibrator;
    //窗口
    private WindowManager mWindowManager;
    //窗口参数
    private WindowManager.LayoutParams mWindowLayoutParams;
    //迷你唱片机
    //private MusicJukeBoxViewSmall mBoxViewSmall;
    //播放器控制面板
    private LinearLayout playerControllerPanelLL;
    private LinearLayout musicControllerLL;
    private ImageView music_notice_def_btn_last;
    private ImageView music_notice_def_btn_pause;
    private ImageView music_notice_def_btn_next;
    //手势分发
    private GestureDetector mGestureDetector;
    //手指在屏幕上的实时X、Y坐标
    private float xInScreen,yInScreen;
    //手指按下X、Y坐标
    private static float xDownInScreen,yDownInScreen;
    //手指按下此View在屏幕中X、Y坐标
    private float xInView,yInView;
    //手指是否持续触摸屏幕中,是否持续碰撞中
    private boolean isTouchIng =false,isCollideIng=false;
    //单击\滚动 事件的有效像素
    public static int SCROLL_PIXEL=5;
    //是否被折叠
    private boolean isFold=false;
    private MusicCountDownView giftItemView=null;
    private RelativeLayout ll_main;
    //当前播放音频对象
    private AudioInfo currentMusicInfo;
    public MusicPlayerWindow(@NonNull Context context) {
        this(context,null);
    }

    public MusicPlayerWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicPlayerWindow(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.music_player_window,this);
        //EventBus.getDefault().register(context);
        //mBoxViewSmall = (MusicJukeBoxViewSmall) findViewById(R.id.music_window_juke);
        playerControllerPanelLL = (LinearLayout) findViewById(R.id.ll_player_controller_panel);
        musicControllerLL = (LinearLayout) findViewById(R.id.ll_music_controller);
        musicControllerLL.setVisibility(GONE);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicPlayerWindow);
            mHorMiniMagin = typedArray.getDimensionPixelSize(R.styleable.MusicPlayerWindow_musicPlayerWinHorMagin, 15);
            typedArray.recycle();
        }else{
            //停靠边界,四周
            mHorMiniMagin = MusicUtils.getInstance().dpToPxInt(getContext(), 15f);
        }
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(context);
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(context);
        //手势分发
        mGestureDetector = new GestureDetector(getContext(),new JukeBoxGestureListener());

        music_notice_def_btn_last = findViewById(R.id.music_notice_def_btn_last);
        music_notice_def_btn_pause = findViewById(R.id.music_notice_def_btn_pause);
        music_notice_def_btn_next = findViewById(R.id.music_notice_def_btn_next);
        getGiftItemView();
        initListener();
    }

    public void initListener(){
        MusicPlayerManager.getInstance().addObservable(this);
        //注册播放器状态监听器
        MusicPlayerManager.getInstance().addOnPlayerEventListener(this);
        getGiftItemView().getView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //记录手指按下时手指在唱片机View中的位置
                        xInView = event.getX();
                        yInView = event.getY();
                        xDownInScreen=event.getRawX();
                        yDownInScreen = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //实时获取相对于屏幕X,Y位置刷新
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        updateJukeLocation();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        actionTouchUp(event);
                        break;
                }
                return mGestureDetector.onTouchEvent(event);
            }
        });
        OnClickListener onClickListener=new OnClickListener() {
           @Override
           public void onClick(View view) {
               int id = view.getId();//上一首
               if (id == R.id.music_notice_def_btn_last) {
                   giftItemView.stopAnimator();
                    MusicPlayerManager.getInstance().playLastMusic();
                   //开始、暂停
               } else if (id == R.id.music_notice_def_btn_pause) {

                   MusicPlayerManager.getInstance().playOrPause();

                   //下一首
               } else if (id == R.id.music_notice_def_btn_next) {
                   giftItemView.stopAnimator();
                   MusicPlayerManager.getInstance().playNextMusic();
               }
               else if (id == R.id.music_notice_def_btn_close) {
                  // EventBus.getDefault().post(new MusicUpdateFinishEvent());
                  // EventBus.getDefault().unregister(getContext());
                   MusicPlayerManager.getInstance().onStop();
                 //  MusicWindowManager.getInstance().removeAllWindowView(getContext().getApplicationContext());
               }
            }
       };

        music_notice_def_btn_last.setOnClickListener(onClickListener);
        music_notice_def_btn_pause.setOnClickListener(onClickListener);
        music_notice_def_btn_next.setOnClickListener(onClickListener);
        findViewById(R.id.music_notice_def_btn_close).setOnClickListener(onClickListener);
    }
    /**
     * 手势识别
     */
    private class JukeBoxGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            performAnim2();
            return super.onSingleTapUp(e);
        }
    }

    @Override
    public void onMusicPlayerState(int playerState, String message) {
        LogUtil.d(TAG,"onMusicPlayerState-->"+playerState);
        Handler handler = getHandler();
        if(handler!=null){
            handler.post(new Runnable() {
                @Override
                public void run() {
//                    if (playerState==MusicConstants.MUSIC_PLAYER_ERROR&&!TextUtils.isEmpty(message)) {
//                        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
//                    }
                    switch (playerState) {
                        case MusicConstants.MUSIC_PLAYER_PREPARE:

                            if (null != music_notice_def_btn_pause) music_notice_def_btn_pause.setImageResource(
                                    R.drawable.audio_icon_pause);
                            giftItemView.updateMusicIndicatorBg(true);
                            break;
                        case MusicConstants.MUSIC_PLAYER_BUFFER:

                            break;
                        case MusicConstants.MUSIC_PLAYER_PLAYING:
                            if (null != music_notice_def_btn_pause)
                                music_notice_def_btn_pause.setImageResource(R.drawable.audio_icon_pause);
                            giftItemView.updateMusicIndicatorBg(true);
                            break;
                        case MusicConstants.MUSIC_PLAYER_PAUSE:
                            if (null != music_notice_def_btn_pause)
                                music_notice_def_btn_pause.setImageResource(R.drawable.audio_icon_play);
                            giftItemView.updateMusicIndicatorBg(false);
                            break;
                        case MusicConstants.MUSIC_PLAYER_STOP:
                            if (null != music_notice_def_btn_pause) music_notice_def_btn_pause.setImageResource(
                                    R.drawable.audio_icon_play);
                            giftItemView.stopAnimator();
                            break;
                        case MusicConstants.MUSIC_PLAYER_ERROR:
                            if (null != music_notice_def_btn_pause){
                                music_notice_def_btn_pause.setImageResource(R.drawable.audio_icon_play);
                            }
                            giftItemView.stopAnimator();
                            break;
                    }
                }
            });
        }
    }

    /**
     * 音频缓冲成功获取播放总时长
     * @param totalDurtion 总时长
     */
    @Override
    public void onPrepared(long totalDurtion) {
        LogUtil.d(MusicPlayerManager.TAG, " onPrepared onAnimationUpdate: totalDurtion =  " + totalDurtion+"换成秒"+(totalDurtion/1000));
        giftItemView.startCountDown(totalDurtion);
    }

    @Override
    public void onBufferingUpdate(int percent) {
        LogUtil.d(MusicPlayerManager.TAG, "onBufferingUpdate="+percent );
    }



    /**
     * 获取正在播放数据的回调
     * @param musicInfo 正在播放的对象
     * @param position 当前正在播放的位置
     */
    @Override
    public void onPlayMusiconInfo(AudioInfo musicInfo, int position) {
          if(currentMusicInfo!=null){
              giftItemView.stopAnimator();
          }
          else{
              currentMusicInfo = musicInfo;
          }
    }

    @Override
    public void onMusicPathInvalid(AudioInfo musicInfo, int position) {

    }

    @Override
    public void onTaskRuntime(long totalDurtion, long currentDurtion, int bufferProgress) {

    }


    /**
     * 根据回调更新浮窗组件显示暂停或开始图片
     * @param observable
     * @param arg
     */
    @Override
    public void update(Observable observable, Object arg) {
        if(observable instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
            MusicStatus musicStatus= (MusicStatus) arg;
            //过滤其它组件的特殊事件
            if(musicStatus.getPlayerStatus()>-2){
                LogUtil.d(MusicPlayerManager.TAG, "MuscicPlayerWindow-----updateData 回调" );
                updateData(musicStatus);
            }
        }
    }
    public void setWindowLayoutParams(WindowManager.LayoutParams windowLayoutParams) {
        this.mWindowLayoutParams=windowLayoutParams;
    }



    public void setWindowManager(WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    /**
     * 松手
     * @param event 手势事件
     */
    private void actionTouchUp(MotionEvent event) {
        xInView=0;yInView=0;
        isTouchIng =false;isCollideIng=false;
    }

    /**
     * 更新浮窗显示位置
     */
    private void updateJukeLocation() {
        if(null!=mWindowManager&&null!=mWindowLayoutParams){
            float toX = xInScreen - xInView;
            float toY = yInScreen - yInView;
            if(toX< mHorMiniMagin){
                toX= mHorMiniMagin;
            }else if(toX>(mScreenWidth -getWidth()- mHorMiniMagin)){
                toX= mScreenWidth -getWidth()- mHorMiniMagin;
            }
            if(toY<0){
                toY=0;
            }else if(toY>(mScreenHeight -getHeight())){
                toY= mScreenHeight -getHeight();
            }
            mWindowLayoutParams.x = (int) toX;
            mWindowLayoutParams.y = (int) toY;
            mWindowManager.updateViewLayout(this, mWindowLayoutParams);
        }
    }



    /**
     * 刷新数据
     * @param musicStatus 播放器内部状态
     */
    public void updateData(MusicStatus musicStatus) {

        if(null!=giftItemView){
            giftItemView.updateData(musicStatus);
        }
        if(musicStatus.getPlayerStatus()==MusicStatus.PLAYER_STATUS_STOP){
            MusicWindowManager.getInstance().removeAllWindowView(getContext().getApplicationContext());
        }
    }

    public void onResume() {

        if(null!=giftItemView){
            giftItemView.onResume();
        }
    }

    public void onPause() {

        if(null!=giftItemView){
            giftItemView.onPause();
        }
    }

    /**
     * 唱片机可见
     */
    public void onVisible() {

        if(null!=giftItemView){
            giftItemView.onVisible();
        }
    }

    /**
     * 唱片机可见
     * @param audioID 音频ID
     */
    public void onVisible(long audioID) {

        if(null!=giftItemView){
            giftItemView.onVisible();
        }
    }

    /**
     * 唱片机不可见
     */
    public void onInvisible() {

        if(null!=giftItemView){
            giftItemView.onInvisible();
        }
    }

    public void onDestroy() {

        if(null!=giftItemView){
            giftItemView.onDestroy();
        }
        mScreenWidth =0;
        mScreenHeight =0;
        xInScreen=0;yInScreen=0;xInView=0;yInView=0;isTouchIng=false;isCollideIng=false;
    }

    /**
     * 返回倒计时组件view
     * @return
     */
    public MusicCountDownView getGiftItemView() {
        if(giftItemView==null){

            giftItemView =new MusicCountDownView(getContext());
            ll_main= findViewById(R.id.rl_count_down);
            ll_main.addView(giftItemView.getView());
        }
        return giftItemView;
    }

     private void performAnim2(){
        int with = AppUtils.dip2px(getContext(),MusicWindowManager.WINDOW_WIDTH_MAX);
        int begin = AppUtils.dip2px(getContext(),MusicWindowManager.WINDOW_HEIGHT);
        //View是否显示的标志
        isFold = !isFold;
        //属性动画对象
        ValueAnimator va ;
        if(isFold){
            //显示view，宽度从0变到height值
            va = ValueAnimator.ofInt(begin,with);
            if(null!=mWindowManager&&null!=mWindowLayoutParams){
                mWindowLayoutParams.width =MusicUtils.getInstance().dpToPxInt(getContext(), MusicWindowManager.WINDOW_WIDTH_MAX);
                mWindowLayoutParams.height=MusicUtils.getInstance().dpToPxInt(getContext(), MusicWindowManager.WINDOW_HEIGHT) ;
                mWindowManager.updateViewLayout(MusicPlayerWindow.this, mWindowLayoutParams);
            }
        }else{
            //隐藏view，宽度从height变为0
            va = ValueAnimator.ofInt(with,begin);
        }
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(Integer)valueAnimator.getAnimatedValue();
                //动态更新view的高度
                playerControllerPanelLL.getLayoutParams().width = h;
                playerControllerPanelLL.requestLayout();

            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(isFold){
                    musicControllerLL.setVisibility(VISIBLE);
                }
                else{
                    musicControllerLL.setVisibility(GONE);
                    if(null!=mWindowManager&&null!=mWindowLayoutParams){
                        mWindowLayoutParams.width =MusicUtils.getInstance().dpToPxInt(getContext(), MusicWindowManager.WINDOW_HEIGHT);
                        mWindowLayoutParams.height=MusicUtils.getInstance().dpToPxInt(getContext(), MusicWindowManager.WINDOW_HEIGHT) ;
                        mWindowManager.updateViewLayout(MusicPlayerWindow.this, mWindowLayoutParams);
                    }
                }
               // giftItemView.updateMusicIndicatorBg(isFold);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        va.setDuration(500);
        //开始动画
        va.start();
    }

}
