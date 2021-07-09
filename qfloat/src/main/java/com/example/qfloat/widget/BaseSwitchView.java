package com.example.qfloat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.example.qfloat.interfaces.OnTouchRangeListener;

public abstract class BaseSwitchView extends RelativeLayout {
    public BaseSwitchView(Context context) {
        super(context);
    }

    public BaseSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void setTouchRangeListener(MotionEvent event, OnTouchRangeListener listener);

}
