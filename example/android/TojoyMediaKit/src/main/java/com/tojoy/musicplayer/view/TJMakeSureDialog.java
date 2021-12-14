package com.tojoy.musicplayer.view;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tojoy.musicplayer.utils.AppUtils;

import uk.co.senab.photoview.R;


/**
 * Created by chengyanfang on 2017/10/13.
 */

public class TJMakeSureDialog {

    Context mContext;

    View mView;

    AdapeterTenDialog alertDialog;
    //取消监听
    OnDialogCancelListener mCancelListener;

    //确定按钮
    TextView mBtnSure;

    public TJMakeSureDialog(Context context) {
        mContext = context;
        initUI();
    }

    public TJMakeSureDialog(Context context, View.OnClickListener onClickListener) {
        mContext = context;
        initUI();
        initEvent(onClickListener, true);
    }

    /**
     * 适配android Q 的升级
     *
     * @param isCanDismiss
     */
    public TJMakeSureDialog(Context context, View.OnClickListener onClickListener, boolean isCanDismiss) {
        mContext = context;
        initUI();
        initEvent(onClickListener, isCanDismiss);
    }




    private void initEvent(View.OnClickListener onClickListener, boolean isCanDismiss) {
        mView.findViewById(R.id.rlv_sure).setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
            if (isCanDismiss) {
                alertDialog.dismiss();
            }

        });
        mView.findViewById(R.id.rlv_cancle).setOnClickListener(v -> {
            if (mCancelListener != null) {
                mCancelListener.onCancelClick();
            }
            if (isCanDismiss) {
                alertDialog.dismiss();
            }
        });
    }



    private void initUI() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.popup_makesure_layout, null);
        mBtnSure = (TextView) mView.findViewById(R.id.button_sure);
    }




    public void show() {
        alertDialog = new AdapeterTenDialog(mContext, R.style.tjoy_dialog_style);
        alertDialog.setContentView(mView, new ViewGroup.LayoutParams(AppUtils.dip2px(mContext, 270), ViewGroup.LayoutParams.WRAP_CONTENT));
        alertDialog.getWindow().setGravity(Gravity.CENTER);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.setOnKeyListener((dialog, keyCode, event) -> true);
    }



    /**
     * ****************** UI定制化
     */
    public TJMakeSureDialog setTitleAndCotent(String title, String content) {
        ((TextView) mView.findViewById(R.id.tv_title)).setText(title);
        ((TextView) mView.findViewById(R.id.tv_content)).setText(content);
        return this;
    }

    /**
     * 提示内容显示两行并居中
     *
     * @param content
     * @param hint
     * @return
     */
    public TJMakeSureDialog setcontent(String content, String hint) {
        mView.findViewById(R.id.rlv_contenthint_layout).setVisibility(View.VISIBLE);
        ((TextView) mView.findViewById(R.id.tv_content_meeting)).setText(content);
        ((TextView) mView.findViewById(R.id.tv_content_hint)).setText(hint);
        return this;
    }

    /**
     * 显示提示框
     */
    public void showAlert() {
        mView.findViewById(R.id.rlv_cancle).setVisibility(View.GONE);
        mView.findViewById(R.id.bottom_vertical_divider).setVisibility(View.GONE);
        show();
    }


    public TJMakeSureDialog hideTitle() {
        mView.findViewById(R.id.rlv_title_layout).setVisibility(View.GONE);
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.setMargins(AppUtils.dip2px(mContext, 16), AppUtils.dip2px(mContext, 35), AppUtils.dip2px(mContext, 16), AppUtils.dip2px(mContext, 35));
        lps.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ((TextView) mView.findViewById(R.id.tv_content)).setLayoutParams(lps);
//        ((TextView) mView.findViewById(R.id.tv_content)).setTextSize(15);

        return this;
    }

    /**
     * 控制文案内容显示行数以及末尾样式
     * @return
     */
    public TJMakeSureDialog defineTextStyle(int maxLine, TextUtils.TruncateAt at){
        ((TextView) mView.findViewById(R.id.tv_content)).setMaxLines(maxLine);
        ((TextView) mView.findViewById(R.id.tv_content)).setEllipsize(at);

        return this;
    }

    public void setSameColor() {
        TextView mTextView;
        mTextView = (TextView) mView.findViewById(R.id.button_cancle);
        mTextView.setTextColor(mContext.getResources().getColor(R.color.main_blue));
    }

    public void setLogoutColor() {
        TextView mTextView;
        mTextView = (TextView) mView.findViewById(R.id.button_cancle);
        mTextView.setTextColor(mContext.getResources().getColor(R.color.color_61000000));
    }



    /**
     * 设置按钮文字
     */

    public TJMakeSureDialog setBtnText(String sure, String cancle) {
        ((TextView) mView.findViewById(R.id.button_sure)).setText(sure);
        ((TextView) mView.findViewById(R.id.button_cancle)).setText(cancle);
        return this;
    }

    public TJMakeSureDialog setBtnSureText(String text) {
        mBtnSure.setText(text);
        return this;
    }

    /**
     * 判断是否在展示
     *
     * @return
     */
    public boolean isShowing() {
        if (alertDialog == null) {
            return false;
        }
        return alertDialog.isShowing();
    }

    public void dismiss() {
        if (isShowing()) {
            alertDialog.dismiss();
        }
    }

    /**
     * 设置确定按钮的显示
     *
     * @param okText
     */
    public TJMakeSureDialog setOkText(String okText) {
        mBtnSure.setText(okText);
        return this;
    }

    //设置取消监听
    public TJMakeSureDialog setCancelListener(OnDialogCancelListener mCancelListener) {
        this.mCancelListener = mCancelListener;
        return this;
    }

    public TJMakeSureDialog setExchangeClolor() {
        TextView sure = mView.findViewById(R.id.button_sure);
        TextView cancle = mView.findViewById(R.id.button_cancle);
        sure.setTextColor(mContext.getResources().getColor(R.color.color_596271));
        cancle.setTextColor(mContext.getResources().getColor(R.color.main_blue));
        return this;
    }

    //点击取消监听
    public interface OnDialogCancelListener {
        void onCancelClick();
    }

    public void setEvent(View.OnClickListener onClickListener) {
        mView.findViewById(R.id.rlv_sure).setOnClickListener(onClickListener);

        mView.findViewById(R.id.rlv_cancle).setOnClickListener(v -> {
            if (mCancelListener != null) {
                mCancelListener.onCancelClick();
            }
            alertDialog.dismiss();
        });
    }


}
