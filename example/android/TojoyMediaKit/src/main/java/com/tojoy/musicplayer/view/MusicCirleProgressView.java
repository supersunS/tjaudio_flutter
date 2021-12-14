package com.tojoy.musicplayer.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tojoy.musicplayer.manager.MusicPlayerManager;
import com.tojoy.musicplayer.model.MusicStatus;
import com.tojoy.musicplayer.utils.AppUtils;
import com.tojoy.musicplayer.utils.LogUtil;

import uk.co.senab.photoview.R;

public class MusicCirleProgressView extends View {
    public static final boolean ANTI_ALIAS = true;

    public static final int DEFAULT_SIZE = 150;
    public static final int DEFAULT_START_ANGLE = 270;
    public static final int DEFAULT_SWEEP_ANGLE = 360;

    public static final int DEFAULT_ANIM_TIME = 10000;

    public static final int DEFAULT_MAX_VALUE = 100;
    public static final int DEFAULT_VALUE = 50;

    public static final int DEFAULT_HINT_SIZE = 15;
    public static final int DEFAULT_UNIT_SIZE = 30;
    public static final int DEFAULT_VALUE_SIZE = 15;

    public static final int DEFAULT_ARC_WIDTH = 2;

    public static final int DEFAULT_WAVE_HEIGHT = 40;
    private static final String TAG = MusicCirleProgressView.class.getSimpleName();
    private Context mContext;
    //默认大小
    private int mDefaultSize;
    //是否开启抗锯齿
    private boolean antiAlias;
    //绘制数值
    private float mValue;
    private float mMaxValue;
    private int mPrecision;
    private int mValueColor;
    private float mValueSize;

    //绘制圆弧
    private Paint mArcPaint;
    private float mArcWidth;
    private float mStartAngle, mSweepAngle;
    private RectF mRectF;
    private int mArcColor;
    //渐变的颜色是360度，如果只显示270，那么则会缺失部分颜色
//    private SweepGradient mSweepGradient;
//    private int[] mGradientColors = {Color.GREEN, Color.YELLOW, Color.RED};
    //当前进度，[0.0f,1.0f]
    private float mPercent;
    //动画时间
    private long mAnimTime;
    //属性动画
    private ValueAnimator mAnimator;

    //绘制背景圆弧
    private Paint mBgArcPaint;
    private int mBgArcColor;
    private float mBgArcWidth;

    //圆心坐标，半径
    private Point mCenterPoint;
    private float mRadius;
    private float mTextOffsetPercentInRadius;
    //是否准备要开始动画
    private boolean readyPlay=false;
    //是否允许悬浮窗显示
    private boolean isVisible=true;
    public MusicCirleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultSize = AppUtils.dip2px(mContext, DEFAULT_SIZE);

        mRectF = new RectF();
        mCenterPoint = new Point();
        initAttrs(attrs);
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MusicCircleProgressBar);

        antiAlias = typedArray.getBoolean(R.styleable.MusicCircleProgressBar_antiAlias, ANTI_ALIAS);

        mValue = typedArray.getFloat(R.styleable.MusicCircleProgressBar_value, DEFAULT_VALUE);
        mMaxValue = typedArray.getFloat(R.styleable.MusicCircleProgressBar_maxValue, DEFAULT_MAX_VALUE);
        //内容数值精度格式
        mPrecision = typedArray.getInt(R.styleable.MusicCircleProgressBar_precision, 0);
        mValueColor = typedArray.getColor(R.styleable.MusicCircleProgressBar_valueColor, Color.BLACK);
        mValueSize = typedArray.getDimension(R.styleable.MusicCircleProgressBar_valueSize, DEFAULT_VALUE_SIZE);

        mArcWidth = typedArray.getDimension(R.styleable.MusicCircleProgressBar_arcWidth,DEFAULT_ARC_WIDTH);
        mStartAngle = typedArray.getFloat(R.styleable.MusicCircleProgressBar_startAngle, DEFAULT_START_ANGLE);
        mSweepAngle = typedArray.getFloat(R.styleable.MusicCircleProgressBar_sweepAngle, DEFAULT_SWEEP_ANGLE);
        mArcColor = typedArray.getColor(R.styleable.MusicCircleProgressBar_arcColors, Color.BLUE);
        mBgArcColor = typedArray.getColor(R.styleable.MusicCircleProgressBar_bgArcColor, Color.WHITE);
        mBgArcWidth = typedArray.getDimension(R.styleable.MusicCircleProgressBar_bgArcWidth, DEFAULT_ARC_WIDTH);
        mTextOffsetPercentInRadius = typedArray.getFloat(R.styleable.MusicCircleProgressBar_textOffsetPercentInRadius, 0.33f);

        mPercent =0;
        //mAnimTime = typedArray.getInt(R.styleable.MusicCircleProgressBar_animTime, DEFAULT_ANIM_TIME);

        typedArray.recycle();
    }

    private void initPaint() {

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(antiAlias);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mArcPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔粗细
        mArcPaint.setStrokeWidth(mArcWidth);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND,或方形样式 Cap.SQUARE
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setColor(mArcColor);
        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(antiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(CustomMeasure(widthMeasureSpec, mDefaultSize),
                CustomMeasure(heightMeasureSpec, mDefaultSize));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        //求圆弧和背景圆弧的最大宽度
        float maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        //求最小值作为实际值
        int minSize = Math.min(w - getPaddingLeft() - getPaddingRight() - 2 * (int) maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) maxArcWidth);
        //减去圆弧的宽度，否则会造成部分圆弧绘制在外围
        mRadius = minSize / 2;
        //获取圆的相关参数
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        //绘制圆弧的边界
        mRectF.left = mCenterPoint.x - mRadius - maxArcWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - maxArcWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + maxArcWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + maxArcWidth / 2;
        Log.d(TAG, "onSizeChanged: 控件大小 = " + "(" + w + ", " + h + ")"
                + "圆心坐标 = " + mCenterPoint.toString()
                + ";圆半径 = " + mRadius
                + ";圆的外接矩形 = " + mRectF.toString());
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
    }



    private void drawArc(Canvas canvas) {
        // 绘制背景圆弧
        // 从进度圆弧结束的地方开始重新绘制，优化性能
        canvas.save();
        float currentAngle = mSweepAngle * mPercent;
        canvas.rotate(mStartAngle, mCenterPoint.x, mCenterPoint.y);
        canvas.drawArc(mRectF, currentAngle, mSweepAngle - currentAngle + 2, false, mBgArcPaint);
        // 第一个参数 oval 为 RectF 类型，即圆弧显示区域
        // startAngle 和 sweepAngle  均为 float 类型，分别表示圆弧起始角度和圆弧度数
        // 3点钟方向为0度，顺时针递增
        // 如果 startAngle < 0 或者 > 360,则相当于 startAngle % 360
        // useCenter:如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形
        canvas.drawArc(mRectF, 2, currentAngle, false, mArcPaint);

        canvas.restore();
    }


    public boolean isAntiAlias() {
        return antiAlias;
    }



    public float getValue() {
        return mValue;
    }

    /**
     * 设置当前值
     *
     */
    public void setValue() {

        mPercent = 0;

        startAnimator( mAnimTime);
    }

    private void startAnimator( long animTime) {
        if(mAnimator==null){
            LogUtil.d(MusicPlayerManager.TAG,"从新倒计时动画 animTime="+animTime);
            mAnimator = new ValueAnimator();
            mAnimator = ValueAnimator.ofFloat(0, 1);
            mAnimator.setDuration(animTime);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPercent = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.start();
        }
        else{
            playDiscAnimator();
        }

    }

    /**
     * 播放唱盘动画
     */
    private void playDiscAnimator() {
        LogUtil.d(MusicPlayerManager.TAG,"从暂停当前倒计时动画 ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (mAnimator.isPaused()) {
                    mAnimator.resume();
                } else {
                    if(!mAnimator.isRunning()){
                        mAnimator.start();
                    }
                }
            }else{
                mAnimator.start();
            }
    }

    /**
     * 暂停旋转动画
     */
    private void pausAnimator() {
        if(null!=mAnimator){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAnimator.pause();
            }else{
                mAnimator.cancel();
                mAnimator=null;
            }
        }
    }

    /**
     * 停止旋转动画
     */
    public void stopAnimator(){
        if(null!=mAnimator){
            mAnimator.cancel();
            mAnimator=null;
        }
    }

    /**
     * 刷新数据
     * @param musicStatus
     */
    public void updateData(MusicStatus musicStatus){

        //ID更新
        if(Integer.valueOf(musicStatus.getId())>0){
            MusicCirleProgressView.this.setTag(musicStatus.getId());
        }
        final int playerStatus = musicStatus.getPlayerStatus();
        MusicCirleProgressView.this.post(new Runnable() {
            @Override
            public void run() {
                //停止
                if(MusicStatus.PLAYER_STATUS_STOP==playerStatus){
                    LogUtil.d(TAG,"update，播放器停止");
                    readyPlay=false;
                    stopAnimator();
                    //暂停
                }else if(MusicStatus.PLAYER_STATUS_PAUSE==playerStatus){
                    LogUtil.d(TAG,"update，播放器暂停");
                    readyPlay=false;
                    pausAnimator();
                    //播放
                }else if(MusicStatus.PLAYER_STATUS_START==playerStatus
                        ){
                    LogUtil.d(MusicPlayerManager.TAG,"update，播放器开始"+getAnimTime());
                    readyPlay=true;
                    startAnimator(getAnimTime());
                    //销毁
                }
                else if(MusicStatus.PLAYER_STATUS_PREPARED==playerStatus){
                    mPercent =0;
                    LogUtil.d(MusicPlayerManager.TAG,"update，播放器缓冲中");
                    stopAnimator();
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
            startAnimator(getAnimTime());
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
     * 获取最大值
     *
     * @return
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }


    public long getAnimTime() {
        return mAnimTime;
    }

    public void setAnimTime(long animTime) {
        mAnimTime = animTime;
    }

    /**
     * 重置
     */
    public void reset() {
        startAnimator(mAnimTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }
    /**
     * 测量 View
     *
     * @param measureSpec
     * @param defaultSize View 的默认大小
     * @return
     */
    public  int CustomMeasure(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }


}
