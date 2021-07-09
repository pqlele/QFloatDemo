package com.example.qfloat.interfaces;

import android.animation.Animator;
import android.view.View;
import android.view.WindowManager;

import com.example.qfloat.SidePattern;

public interface OnFloatAnimator {

    Animator enterAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern);

    Animator exitAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern);

}
