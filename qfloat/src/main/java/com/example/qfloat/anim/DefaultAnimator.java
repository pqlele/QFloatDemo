package com.example.qfloat.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;

import com.example.qfloat.interfaces.OnFloatAnimator;
import com.example.qfloat.SidePattern;
import com.example.qfloat.utils.DisplayUtils;

import kotlin.Triple;

public class DefaultAnimator implements OnFloatAnimator {

    @Override
    public Animator enterAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern) {
        return getAnimator(view, params, windowManager, sidePattern, false);
    }

    @Override
    public Animator exitAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern) {
        return getAnimator(view, params, windowManager, sidePattern, true);
    }

    private Animator getAnimator(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern, boolean isExit) {
        Triple<Integer, Integer, Boolean> triple = initValue(view, params, windowManager, sidePattern);
        // 退出动画的起始值、终点值，与入场动画相反
        int start = isExit ? triple.getSecond() : triple.getFirst();
        int end = isExit ? triple.getFirst() : triple.getSecond();

        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    int value = (int) animation.getAnimatedValue();
                    if (triple.getThird()) {
                        params.x = value;
                    } else {
                        params.y = value;
                    }
                    // 动画执行过程中页面关闭，出现异常
                    windowManager.updateViewLayout(view, params);
                } catch (Exception e) {
                    animator.cancel();
                }
            }
        });
        return animator;
    }

    /**
     * 计算边距，起始坐标等
     * Triple<Integer, Integer, Boolean>  : 开始值,结束值,是否水平方向
     */
    private Triple<Integer, Integer, Boolean> initValue(View view, WindowManager.LayoutParams params, WindowManager windowManager, SidePattern sidePattern) {
        Rect parentRect = new Rect();
        windowManager.getDefaultDisplay().getRectSize(parentRect);
        // 浮窗各边到窗口边框的距离
        int leftDistance = params.x;
        int rightDistance = parentRect.right - (leftDistance + view.getRight());
        int topDistance = params.y;
        int bottomDistance = parentRect.bottom - (topDistance + view.getBottom());

        // 水平、垂直方向的距离最小值
        int minX = Math.min(leftDistance, rightDistance);
        int minY = Math.min(topDistance, bottomDistance);

        boolean isHorizontal;
        int endValue;
        int startValue;
        switch (sidePattern) {
            case LEFT:
            case RESULT_LEFT:
                // 从左侧到目标位置，右移
                isHorizontal = true;
                endValue = params.x;
                startValue = -view.getRight();
                break;

            case RIGHT:
            case RESULT_RIGHT:
                // 从右侧到目标位置，左移
                isHorizontal = true;
                endValue = params.x;
                startValue = parentRect.right;
                break;

            case TOP:
            case RESULT_TOP:
                // 从顶部到目标位置，下移
                isHorizontal = false;
                endValue = params.y;
                startValue = -view.getBottom();
                break;

            case BOTTOM:
            case RESULT_BOTTOM:
                // 从底部到目标位置，上移
                isHorizontal = false;
                endValue = params.y;
                startValue = parentRect.bottom + getCompensationHeight(view, params);
                break;

            case DEFAULT:
            case AUTO_HORIZONTAL:
            case RESULT_HORIZONTAL:
                // 水平位移，哪边距离屏幕近，从哪侧移动
                isHorizontal = true;
                endValue = params.x;
                startValue = leftDistance < rightDistance ? -view.getRight() : parentRect.right;
                break;

            case AUTO_VERTICAL:
            case RESULT_VERTICAL:
                // 垂直位移，哪边距离屏幕近，从哪侧移动
                isHorizontal = false;
                endValue = params.y;
                startValue = topDistance < bottomDistance ? -view.getBottom() : parentRect.bottom + getCompensationHeight(view, params);
                break;

            default:
                if (minX <= minY) {
                    isHorizontal = true;
                    endValue = params.x;
                    startValue = leftDistance < rightDistance ? -view.getRight() : parentRect.right;
                } else {
                    isHorizontal = false;
                    endValue = params.y;
                    startValue = topDistance < bottomDistance ? -view.getBottom() :
                            parentRect.bottom + getCompensationHeight(view, params);
                }
                break;
        }
        return new Triple<>(startValue, endValue, isHorizontal);
    }


    /**
     * 单页面浮窗（popupWindow），坐标从顶部计算，需要加上状态栏的高度
     */
    private int getCompensationHeight(View view, WindowManager.LayoutParams params) {
        int[] location = new int[2];
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location);
        // 绝对高度和相对高度相等，说明是单页面浮窗（popupWindow），计算底部动画时需要加上状态栏高度
        return (location[1] == params.y) ? DisplayUtils.statusBarHeight(view) : 0;
    }

}
