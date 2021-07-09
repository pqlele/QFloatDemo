package com.example.qfloat.interfaces;

import com.example.qfloat.widget.BaseSwitchView;

public interface OnTouchRangeListener {

    /**
     * 手指触摸到指定区域
     */
    void touchInRange(boolean inRange, BaseSwitchView view);


    /**
     * 在指定区域抬起手指
     */
    void touchUpInRange();

}
