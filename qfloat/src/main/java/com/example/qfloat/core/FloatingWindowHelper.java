package com.example.qfloat.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.qfloat.ShowPattern;
import com.example.qfloat.anim.AnimatorManager;
import com.example.qfloat.data.FloatConfig;
import com.example.qfloat.interfaces.OnFloatTouchListener;
import com.example.qfloat.utils.DisplayUtils;
import com.example.qfloat.utils.InputMethodUtils;
import com.example.qfloat.utils.LifecycleUtils;
import com.example.qfloat.widget.ParentFrameLayout;


public class FloatingWindowHelper {

    public Context context;
    public FloatConfig config;

    private WindowManager windowManager;
    public WindowManager.LayoutParams params;
    ParentFrameLayout frameLayout;

    private TouchUtils touchUtils;
    private Animator enterAnimator;

    public FloatingWindowHelper(Context context, FloatConfig config) {
        this.context = context;
        this.config = config;
    }

    public void createWindow() {
        try {
            touchUtils = new TouchUtils(context, config);
            initParams();
            addView();
            config.isShow = true;
        } catch (Exception e) {
            e.printStackTrace();
            if (config != null && config.callbacks != null) {
                config.callbacks.createdResult(false, e.getMessage(), null);
            }
        }

    }

    private void initParams() {
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 设置窗口类型为应用子窗口，和PopupWindow同类型
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
            // 子窗口必须和创建它的Activity的windowToken绑定
            params.token = getToken();
        } else {
            // 系统全局窗口，可覆盖在任何应用之上，以及单独显示在桌面上
            // 安卓6.0 以后，全局的Window类别，必须使用TYPE_APPLICATION_OVERLAY
            params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.START | Gravity.TOP;
        // 设置浮窗以外的触摸事件可以传递给后面的窗口、不自动获取焦点
        params.flags = config.immersionStatusBar ?
                // 没有边界限制，允许窗口扩展到屏幕外
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                : WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = config.widthMatch ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = config.heightMatch ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;

        if (config.immersionStatusBar && config.heightMatch) {
            params.height = DisplayUtils.getScreenHeight(context);
        }

        // 如若设置了固定坐标，直接定位
        if (!config.locationPair.equals(new Pair<>(0, 0))) {
            params.x = config.locationPair.first;
            params.y = config.locationPair.second;
        }
        this.params = params;
    }

    private IBinder getToken() {
        Activity activity = context instanceof Activity ? (Activity) context : LifecycleUtils.getTopActivity();
        if (activity != null && activity.getWindow() != null
                && activity.getWindow().getDecorView() != null) {
            return activity.getWindow().getDecorView().getWindowToken();
        } else {
            return null;
        }
    }

    /**
     * 将自定义的布局，作为xml布局的父布局，添加到windowManager中，
     * 重写自定义布局的touch事件，实现拖拽效果。
     */
    private void addView() {
        // 创建一个frameLayout作为浮窗布局的父容器
        frameLayout = new ParentFrameLayout(context, config);
        frameLayout.setTag(config.floatTag);
        // 将浮窗布局文件添加到父容器frameLayout中，并返回该浮窗文件
        View floatingView = LayoutInflater.from(context).inflate(config.layoutId, frameLayout, true);
        // 为了避免创建的时候闪一下，我们先隐藏视图，不能直接设置GONE，否则定位会出现问题
        floatingView.setVisibility(View.INVISIBLE);
        // 将frameLayout添加到系统windowManager中
        windowManager.addView(frameLayout, params);


        // 通过重写frameLayout的Touch事件，实现拖拽效果
        frameLayout.touchListener = new OnFloatTouchListener() {
            @Override
            public void onTouch(MotionEvent event) {
                touchUtils.updateFloat(frameLayout, event, windowManager, params);
            }
        };

        // 在浮窗绘制完成的时候，设置初始坐标、执行入场动画
        frameLayout.layoutListener = new ParentFrameLayout.OnLayoutListener() {
            @Override
            public void onLayout() {
                setGravity(frameLayout);
                // 如果设置了过滤当前页，或者后台显示前台创建、前台显示后台创建，隐藏浮窗，否则执行入场动画
                if (config.filterSelf
                        || (config.showPattern == ShowPattern.BACKGROUND && LifecycleUtils.isForeground())
                        || (config.showPattern == ShowPattern.FOREGROUND && !LifecycleUtils.isForeground())
                ) {
                    setVisible(View.GONE, true);
                    initEditText();
                } else {
                    enterAnim(floatingView);
                }

                // 设置callbacks
                config.layoutView = floatingView;
                if (config.invokeView != null) {
                    config.invokeView.invoke(floatingView);
                }
                if (config.callbacks != null) {
                    config.callbacks.createdResult(true, null, floatingView);
                }
            }
        };
    }

    private void initEditText() {
        if (config.hasEditText) {
            if (frameLayout != null) {
                traverseViewGroup(frameLayout);
            }
        }
    }

    private void traverseViewGroup(View view) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                // 遍历ViewGroup，是子view判断是否是EditText，是ViewGroup递归调用
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View child = ((ViewGroup) view).getChildAt(i);
                    if (child instanceof ViewGroup) {
                        traverseViewGroup(child);
                    } else {
                        checkEditText(child);
                    }
                }
            } else {
                checkEditText(view);
            }
        }

    }

    private void checkEditText(View view) {
        if (view instanceof EditText) {
            InputMethodUtils.initInputMethod((EditText) view, config.floatTag);
        }
    }

    /**
     * 设置浮窗的可见性
     */
    public void setVisible(int visible, boolean needShow) {
        if (frameLayout == null || frameLayout.getChildCount() < 1) return;
        // 如果用户主动隐藏浮窗，则该值为false
        config.needShow = needShow;
        frameLayout.setVisibility(visible);

        View view = frameLayout.getChildAt(0);
        if (visible == View.VISIBLE) {
            config.isShow = true;
            if (config.callbacks != null) {
                config.callbacks.show(view);
            }
        } else {
            config.isShow = false;
            if (config.callbacks != null) {
                config.callbacks.hide(view);
            }
        }
    }

    /**
     * 设置浮窗的对齐方式，支持上下左右、居中、上中、下中、左中和右中，默认左上角
     * 支持手动设置的偏移量
     */
    @SuppressLint("RtlHardcoded")
    private void setGravity(View view) {
        if (!config.locationPair.equals(new Pair<>(0, 0)) || view == null) {
            return;
        }
        Rect parentRect = new Rect();
        // 获取浮窗所在的矩形
        windowManager.getDefaultDisplay().getRectSize(parentRect);
        int[] location = new int[2];
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location);
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        int statusBarHeight = location[1] > params.y ? DisplayUtils.statusBarHeight(view) : 0;
        int parentBottom = config.displayHeight.getDisplayRealHeight(context) - statusBarHeight;
        switch (config.gravity) {
            // 右上
            case Gravity.END:
            case Gravity.END | Gravity.TOP:
            case Gravity.RIGHT:
            case Gravity.RIGHT | Gravity.TOP:
                params.x = parentRect.right - view.getWidth();
                break;
            // 左下
            case Gravity.START | Gravity.BOTTOM:
            case Gravity.BOTTOM:
            case Gravity.LEFT | Gravity.BOTTOM:
                params.y = parentBottom - view.getHeight();
                break;
            // 右下
            case Gravity.END | Gravity.BOTTOM:
            case Gravity.RIGHT | Gravity.BOTTOM:
                params.x = parentRect.right - view.getWidth();
                params.y = parentBottom - view.getHeight();
                break;
            // 居中
            case Gravity.CENTER:
                params.x = (parentRect.right - view.getWidth()) >> 1;
                params.y = (parentBottom - view.getHeight()) >> 1;
                break;
            // 上中
            case Gravity.CENTER_HORIZONTAL:
            case Gravity.TOP | Gravity.CENTER_HORIZONTAL:
                params.x = (parentRect.right - view.getWidth()) >> 1;
                break;
            // 下中
            case Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL:
                params.x = (parentRect.right - view.getWidth()) >> 1;
                params.y = parentBottom - view.getHeight();
                break;
            // 左中
            case Gravity.CENTER_VERTICAL:
            case Gravity.START | Gravity.CENTER_VERTICAL:
            case Gravity.LEFT | Gravity.CENTER_VERTICAL:
                params.y = (parentBottom - view.getHeight()) >> 1;
                break;
            // 右中
            case Gravity.END | Gravity.CENTER_VERTICAL:
            case Gravity.RIGHT | Gravity.CENTER_VERTICAL:
                params.x = parentRect.right - view.getWidth();
                params.y = (parentBottom - view.getHeight()) >> 1;
                break;
            // 其他情况,均视为左上
            default:
                break;
        }
        // 设置偏移量
        params.x += config.offsetPair.first;
        params.y += config.offsetPair.second;

        if (config.immersionStatusBar) {
            if (config.showPattern != ShowPattern.CURRENT_ACTIVITY) {
                params.y -= statusBarHeight;
            }
        } else {
            if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                params.y += statusBarHeight;
            }
        }
        // 更新浮窗位置信息
        windowManager.updateViewLayout(view, params);
    }

    /**
     * 入场动画
     */
    private void enterAnim(View floatingView) {
        if (frameLayout == null || config.isAnim) {
            return;
        }
        enterAnimator = new AnimatorManager(frameLayout, params, windowManager, config)
                .enterAnim();
        // 可以延伸到屏幕外，动画结束按需去除该属性，不然旋转屏幕可能置于屏幕外部
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        if (enterAnimator != null) {
            enterAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    config.isAnim = false;
                    if (!config.immersionStatusBar) {
                        // 不需要延伸到屏幕外了，防止屏幕旋转的时候，浮窗处于屏幕外
                        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    }
                    initEditText();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    floatingView.setVisibility(View.VISIBLE);
                    config.isAnim = true;
                }
            });
            enterAnimator.start();
        }

        if (enterAnimator == null) {
            floatingView.setVisibility(View.VISIBLE);
            windowManager.updateViewLayout(floatingView, params);
        }
    }

    /**
     * 退出动画
     */
    public void exitAnim() {
        if (frameLayout == null || (config.isAnim && enterAnimator == null)) {
            return;
        }
        if (enterAnimator != null) {
            enterAnimator.cancel();
        }

        Animator animator = new AnimatorManager(frameLayout, params, windowManager, config).exitAnim();
        if (animator == null) {
            remove(false);
        } else {
            // 二次判断，防止重复调用引发异常
            if (config.isAnim) {
                return;
            }
            config.isAnim = true;

            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    frameLayout.setVisibility(View.INVISIBLE);
                    remove(false);
                }
            });
            animator.start();
        }
    }


    /**
     * 退出动画执行结束/没有退出动画，进行回调、移除等操作
     */
    public void remove(boolean force) {
        try {
            config.isAnim = false;
            FloatingWindowManager.remove(config.floatTag);
            // removeView是异步删除，在Activity销毁的时候会导致窗口泄漏，所以使用removeViewImmediate直接删除view
            if (force) {
                windowManager.removeViewImmediate(frameLayout);
            } else {
                windowManager.removeView(frameLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新浮窗坐标
     */
    public void updateFloat(int x, int y) {
        if (frameLayout != null) {
            if (x == -1 && y == -1) {
                // 未指定具体坐标，执行吸附动画
                frameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        touchUtils.updateFloat(frameLayout, params, windowManager);
                    }
                }, 200);
            } else {
                params.x = x;
                params.y = y;
                windowManager.updateViewLayout(frameLayout, params);
            }
        }
    }

}
