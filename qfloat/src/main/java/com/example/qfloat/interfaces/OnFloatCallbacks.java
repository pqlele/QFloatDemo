package com.example.qfloat.interfaces;

import android.view.MotionEvent;
import android.view.View;

public interface OnFloatCallbacks {

    /**
     * 浮窗的创建结果，是否创建成功
     *
     * @param isCreated 是否创建成功
     * @param msg       失败返回的结果
     * @param view      浮窗xml布局
     */
    void createdResult(boolean isCreated, String msg, View view);

    void show(View view);

    void hide(View view);

    void dismiss();

    void touchEvent(View view, MotionEvent event);

    /**
     * 浮窗被拖拽时的回调，坐标为浮窗的左上角坐标
     */
    void drag(View view, MotionEvent event);


    /**
     * 拖拽结束时的回调，坐标为浮窗的左上角坐标
     */
    void dragEnd(View view);


}
