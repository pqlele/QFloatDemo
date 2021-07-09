package com.example.qfloat.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.WindowManager;

import com.example.qfloat.SidePattern;
import com.example.qfloat.interfaces.OnFloatAnimator;

/**
 * 右下角的删除浮窗 进入和退出动画
 */
public class CloseFloatAnimator implements OnFloatAnimator {

    @Override
    public Animator enterAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1.f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
                view.setPivotX(view.getWidth());
                view.setPivotY(view.getHeight());

            }
        });
        return animator;
    }

    @Override
    public Animator exitAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setScaleX(value);
                view.setScaleY(value);
                view.setPivotX(view.getWidth());
                view.setPivotY(view.getHeight());
            }
        });
        return animator;
    }

}
