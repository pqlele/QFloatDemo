package com.example.qfloat.interfaces;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

public interface OnDisplayHeight {

    /**
     * 获取屏幕有效的显示高度，不包含虚拟导航栏
     *
     * @param context ApplicationContext
     * @return 高度值（int类型）
     */
    int getDisplayRealHeight(@NotNull Context context);
}
