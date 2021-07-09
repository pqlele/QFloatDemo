package com.example.qfloat.utils;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.qfloat.EasyFloat;
import com.example.qfloat.ShowPattern;
import com.example.qfloat.SidePattern;
import com.example.qfloat.interfaces.OnFloatAnimator;
import com.example.qfloat.interfaces.OnFloatCallbacks;
import com.example.qfloat.interfaces.OnTouchRangeListener;
import com.example.qfloat.widget.BaseSwitchView;

public class DragUtils {

    private static final String CLOSE_TAG = "CLOSE_TAG";
    private static BaseSwitchView closeView;


    /**
     * 注册侧滑关闭浮窗
     */
    public static void registerDragClose(MotionEvent event, OnTouchRangeListener listener, int layoutId,
                                         ShowPattern showPattern, OnFloatAnimator appFloatAnimator) {

        showClose(layoutId, showPattern, appFloatAnimator);

        // 设置触摸状态监听
        if (closeView != null) {
            closeView.setTouchRangeListener(event, listener);
        }
        // 抬起手指时，关闭删除选项
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            dismissClose();
        }
    }

    private static void showClose(int layoutId, ShowPattern showPattern, OnFloatAnimator appFloatAnimator) {
        if (EasyFloat.isShow(CLOSE_TAG)) {
            return;
        }
        EasyFloat.with(LifecycleUtils.application)
                .setLayout(layoutId)
                .setShowPattern(showPattern)
                .setMatchParent(false, false)
                .setTag(CLOSE_TAG)
                .setGravity(Gravity.END | Gravity.BOTTOM, 0, 0)
                .setSidePattern(SidePattern.BOTTOM)
                .setAnimator(appFloatAnimator)
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void createdResult(boolean isCreated, String msg, View view) {
                        if (!isCreated || view == null) {
                            return;
                        }

                        ViewGroup viewGroup = (ViewGroup) view;
                        if (viewGroup.getChildCount() > 0) {
                            // 获取区间判断布局
                            View child = viewGroup.getChildAt(0);
                            if (child instanceof BaseSwitchView) {
                                closeView = (BaseSwitchView) child;
                            }
                        }
                    }

                    @Override
                    public void show(View view) {

                    }

                    @Override
                    public void hide(View view) {

                    }

                    @Override
                    public void dismiss() {
                        closeView = null;
                    }

                    @Override
                    public void touchEvent(View view, MotionEvent event) {

                    }

                    @Override
                    public void drag(View view, MotionEvent event) {

                    }

                    @Override
                    public void dragEnd(View view) {

                    }
                }).show();
    }

    private static void dismissClose() {
        EasyFloat.dismiss(CLOSE_TAG, false);
    }

}
