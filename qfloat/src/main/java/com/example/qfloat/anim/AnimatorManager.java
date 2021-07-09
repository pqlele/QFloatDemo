package com.example.qfloat.anim;

import android.animation.Animator;
import android.view.View;
import android.view.WindowManager;

import com.example.qfloat.data.FloatConfig;
import com.example.qfloat.interfaces.OnFloatAnimator;

public class AnimatorManager {

    private View view;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private FloatConfig config;

    public AnimatorManager(View view, WindowManager.LayoutParams params, WindowManager windowManager, FloatConfig config) {
        this.view = view;
        this.params = params;
        this.windowManager = windowManager;
        this.config = config;
    }

    public Animator enterAnim() {
        OnFloatAnimator floatAnimator = config.floatAnimator;
        if (floatAnimator != null) {
            return floatAnimator.enterAnim(view, params, windowManager, config.sidePattern);
        }
        return null;
    }

    public Animator exitAnim() {
        OnFloatAnimator floatAnimator = config.floatAnimator;
        if (floatAnimator != null) {
            return floatAnimator.exitAnim(view, params, windowManager, config.sidePattern);
        }
        return null;
    }
}
