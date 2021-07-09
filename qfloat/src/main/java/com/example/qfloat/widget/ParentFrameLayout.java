package com.example.qfloat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.example.qfloat.data.FloatConfig;
import com.example.qfloat.interfaces.OnFloatTouchListener;

public class ParentFrameLayout extends FrameLayout {

    public OnFloatTouchListener touchListener;
    public OnLayoutListener layoutListener;
    private boolean isCreated;
    private FloatConfig config;


    /**
     * 布局绘制完成的接口，用于通知外部做一些View操作，不然无法获取view宽高
     */
    public interface OnLayoutListener {
        void onLayout();
    }


    public ParentFrameLayout(Context context, FloatConfig config) {
        super(context);
        this.config = config;
        init();
    }

    private void init() {

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 初次绘制完成的时候，需要设置对齐方式、坐标偏移量、入场动画
        if (!isCreated) {
            isCreated = true;
            if (layoutListener != null) {
                layoutListener.onLayout();
            }
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev != null && touchListener != null) {
            touchListener.onTouch(ev);
        }
        // 是拖拽事件就进行拦截，反之不拦截
        // ps：拦截后将不再回调该方法，会交给该view的onTouchEvent进行处理，所以后续事件需要在onTouchEvent中回调
        return config.isDrag || super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null && touchListener != null) {
            touchListener.onTouch(event);
        }
        return config.isDrag || super.onTouchEvent(event);
    }

//    /**
//     * 按键转发到视图的分发方法，在这里关闭输入法
//     */
//    @Override
//    public boolean dispatchKeyEventPreIme(KeyEvent event) {
//        if (config.hasEditText && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//            InputMethodUtils.closedInputMethod(config.floatTag);
//        }
//        return super.dispatchKeyEventPreIme(event);
//    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (config != null && config.callbacks != null) {
            config.callbacks.dismiss();
        }
    }
}

