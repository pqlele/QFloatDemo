package com.example.qfloat.utils;

import android.content.Context;

import com.example.qfloat.interfaces.OnDisplayHeight;

import org.jetbrains.annotations.NotNull;

public class DefaultDisplayHeight implements OnDisplayHeight {


    @Override
    public int getDisplayRealHeight(@NotNull Context context) {
        return DisplayUtils.rejectedNavHeight(context);
    }
}
