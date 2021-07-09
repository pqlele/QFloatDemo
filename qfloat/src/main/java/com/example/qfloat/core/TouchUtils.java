package com.example.qfloat.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.qfloat.ShowPattern;
import com.example.qfloat.data.FloatConfig;
import com.example.qfloat.utils.DisplayUtils;

public class TouchUtils {

    private Context context;
    private FloatConfig config;

    // 窗口所在的矩形
    private Rect parentRect = new Rect();

    // 悬浮的父布局高度、宽度
    private int parentHeight;
    private int parentWidth;

    // 四周坐标边界值
    private int leftBorder;
    private int topBorder;
    private int rightBorder;
    private int bottomBorder;

    // 起点坐标
    private float lastX;
    private float lastY;

    // 浮窗各边距离父布局的距离
    private int leftDistance;
    private int rightDistance;
    private int topDistance;
    private int bottomDistance;

    // x轴、y轴的最小距离值
    private int minX;
    private int minY;
    private int[] location = new int[2];
    private int statusBarHeight;

    // 屏幕可用高度 - 浮窗自身高度 的剩余高度
    private int emptyHeight;


    public TouchUtils(Context context, FloatConfig config) {
        this.context = context;
        this.config = config;
    }


    /**
     * 根据吸附模式，实现相应的拖拽效果
     */
    public void updateFloat(View view, MotionEvent event, WindowManager windowManager, WindowManager.LayoutParams params) {
        if (config.callbacks != null) {
            config.callbacks.touchEvent(view, event);
        }
        // 不可拖拽、或者正在执行动画，不做处理
        if (!config.dragEnable || config.isAnim) {
            config.isDrag = false;
            return;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                config.isDrag = false;
                // 记录触摸点的位置
                lastX = event.getRawX();
                lastY = event.getRawY();
                // 初始化一些边界数据
                initBoarderValue(view, params);
                Log.i("tag@@", "ACTION_DOWN ====> lastX:" + lastX + ",lastY:" + lastY);
                break;

            case MotionEvent.ACTION_MOVE:
                // 过滤边界值之外的拖拽
                if (event.getRawX() < leftBorder || event.getRawX() > rightBorder + view.getWidth()
                        || event.getRawY() < topBorder || event.getRawY() > bottomBorder + view.getHeight()) {
                    return;
                }

                // 移动值 = 本次触摸值 - 上次触摸值
                float dx = event.getRawX() - lastX;
                float dy = event.getRawY() - lastY;

                Log.i("tag@@", "ACTION_MOVE ===> event.getRawX():" + event.getRawX() + ", event.getRawY():" + event.getRawY() + ",  lastX:" + lastX + ",  lastY:" + lastY + ";  dx:" + dx + ";  dy:" + dy + ",  params.x:" + params.x + ",  params.y:" + params.y);

                // 忽略过小的移动，防止点击无效
                if (!config.isDrag && dx * dx + dy * dy < 81) {
                    return;
                }
                config.isDrag = true;
                int x = (int) (params.x + dx);
                int y = (int) (params.y + dy);
                Log.i("tag@@", "move  *** leftBorder:" + leftBorder + ", rightBorder:" + rightBorder + ", topBorder:" + topBorder + ",  bottomBorder:" + bottomBorder);
                // 检测浮窗是否到达边缘
                if (x < leftBorder) {
                    x = leftBorder;
                } else if (x > rightBorder) {
                    x = rightBorder;
                }

                if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                    // 单页面浮窗，设置状态栏不沉浸时，最小高度为状态栏高度
                    if (y < statusBarHeight(view) && !config.immersionStatusBar) {
                        y = statusBarHeight(view);
                    }
                }

                if (y < topBorder) {
                    y = topBorder;
                } else if (y < 0) {
                    if (config.immersionStatusBar) {
                        y = Math.max(y, -statusBarHeight);
                    } else {
                        y = 0;
                    }
                } else if (y > bottomBorder) {
                    y = bottomBorder;
                }

                switch (config.sidePattern) {
                    case LEFT:
                        x = 0;
                        break;
                    case RIGHT:
                        x = parentWidth - view.getWidth();
                        break;
                    case TOP:
                        y = 0;
                        break;
                    case BOTTOM:
                        y = emptyHeight;
                        break;
                    case AUTO_HORIZONTAL:
                        x = event.getRawX() * 2 > parentWidth ? parentWidth - view.getWidth() : 0;
                        break;
                    case AUTO_VERTICAL:
                        y = (event.getRawY() - parentRect.top) * 2 > parentHeight ? parentHeight - view.getHeight() : 0;
                        break;
                    case AUTO_SIDE:
                        leftDistance = (int) event.getRawX();
                        rightDistance = (int) (parentWidth - event.getRawX());
                        topDistance = (int) (event.getRawY() - parentRect.top);
                        bottomDistance = (int) (parentHeight + parentRect.top - event.getRawY());
                        minX = Math.min(leftDistance, rightDistance);
                        minY = Math.min(topDistance, bottomDistance);

                        if (minX < minY) {
                            x = leftDistance == minX ? 0 : parentWidth - view.getWidth();
                        } else {
                            y = topDistance == minY ? 0 : emptyHeight;
                        }
                        break;
                }

                // 重新设置坐标信息
                params.x = x;
                params.y = y;

                Log.i("tag@@", "move---->  x:" + x + ",y:" + y + ",params.x:" + params.x + ",params.y:" + params.y);

                windowManager.updateViewLayout(view, params);
                if (config.callbacks != null) {
                    config.callbacks.drag(view, event);
                }
                // 更新上次触摸点的数据
                lastX = event.getRawX();
                lastY = event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!config.isDrag) {
                    return;
                }
                // 回调拖拽事件的ACTION_UP
                if (config.callbacks != null) {
                    config.callbacks.drag(view, event);
                }
                switch (config.sidePattern) {
                    case RESULT_LEFT:
                    case RESULT_RIGHT:
                    case RESULT_TOP:
                    case RESULT_BOTTOM:
                    case RESULT_HORIZONTAL:
                    case RESULT_VERTICAL:
                    case RESULT_SIDE:
                        sideAnim(view, params, windowManager);
                        break;
                    default:
                        if (config.callbacks != null) {
                            config.callbacks.dragEnd(view);
                        }
                        break;
                }
                break;

        }

    }

    private void initBoarderValue(View view, WindowManager.LayoutParams params) {
        // 屏幕宽高需要每次获取，可能会有屏幕旋转、虚拟导航栏的状态变化
        parentWidth = DisplayUtils.getScreenWidth(context);
        parentHeight = config.displayHeight.getDisplayRealHeight(context);
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location);
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        statusBarHeight = location[1] > params.y ? statusBarHeight(view) : 0;
        emptyHeight = parentHeight - view.getHeight() - statusBarHeight;

        leftBorder = Math.max(0, config.leftBorder);
        rightBorder = Math.min(parentWidth, config.rightBorder) - view.getWidth();


        if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 单页面浮窗，坐标屏幕顶部计算
            topBorder = config.immersionStatusBar ? config.topBorder : config.topBorder + statusBarHeight(view);
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            topBorder = config.immersionStatusBar ? config.topBorder - statusBarHeight(view) : config.topBorder;
        }


        if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 单页面浮窗，坐标屏幕顶部计算
            bottomBorder = config.immersionStatusBar ? Math.min(emptyHeight, config.bottomBorder - view.getHeight()) :
                    Math.min(emptyHeight, config.bottomBorder + statusBarHeight(view) - view.getHeight());
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            bottomBorder = config.immersionStatusBar ? Math.min(emptyHeight, config.bottomBorder - statusBarHeight(view) - view.getHeight()) :
                    Math.min(emptyHeight, config.bottomBorder - view.getHeight());
        }
    }

    private void sideAnim(View view, WindowManager.LayoutParams params, WindowManager windowManager) {
        initDistanceValue(params);
        boolean isX;
        int end;
        switch (config.sidePattern) {
            case RESULT_LEFT:
                isX = true;
                end = leftBorder;
                break;
            case RESULT_RIGHT:
                isX = true;
                end = params.x + rightDistance;
                break;
            case RESULT_HORIZONTAL:
                isX = true;
                end = leftDistance < rightDistance ? leftBorder : params.x + rightDistance;
                break;
            case RESULT_TOP:
                isX = false;
                end = topBorder;
                break;
            case RESULT_BOTTOM:
                isX = false;
                //不要轻易使用此相关模式，需要考虑虚拟导航栏的情况
                end = bottomBorder;
                break;
            case RESULT_VERTICAL:
                isX = false;
                end = topDistance < bottomDistance ? topBorder : bottomBorder;
                break;
            case RESULT_SIDE:
                if (minX < minY) {
                    isX = true;
                    end = leftDistance < rightDistance ? leftBorder : params.x + rightDistance;
                } else {
                    isX = false;
                    end = topDistance < bottomDistance ? topBorder : bottomBorder;
                }
                break;
            default:
                return;
        }

        ValueAnimator animator = ValueAnimator.ofInt(isX ? params.x : params.y, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    if (isX) {
                        params.x = (int) animation.getAnimatedValue();
                    } else {
                        params.y = (int) animation.getAnimatedValue();
                    }
                    // 极端情况，还没吸附就调用了关闭浮窗，会导致吸附闪退
                    windowManager.updateViewLayout(view, params);
                } catch (Exception e) {
                    animator.cancel();
                }
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                dragEnd(view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dragEnd(view);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                config.isAnim = true;
            }
        });
        animator.start();

    }

    private void dragEnd(View view) {
        config.isAnim = false;
        if (config.callbacks != null) {
            config.callbacks.dragEnd(view);
        }
    }

    /**
     * 计算一些边界距离数据
     */
    private void initDistanceValue(WindowManager.LayoutParams params) {
        leftDistance = params.x - leftBorder;
        rightDistance = rightBorder - params.x;
        topDistance = params.y - topBorder;
        bottomDistance = bottomBorder - params.y;
        minX = Math.min(leftDistance, rightDistance);
        minY = Math.min(topDistance, bottomDistance);
    }

    private int statusBarHeight(View view) {
        return DisplayUtils.statusBarHeight(view);
    }

    /**
     * 根据吸附类别，更新浮窗位置
     */
    public void updateFloat(View view, WindowManager.LayoutParams params, WindowManager windowManager) {
        initBoarderValue(view, params);
        sideAnim(view, params, windowManager);
    }


}
