package com.example.qfloat.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qfloat.ShowPattern;
import com.example.qfloat.core.FloatingWindowHelper;
import com.example.qfloat.core.FloatingWindowManager;
import com.example.qfloat.data.FloatConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class LifecycleUtils {

    public static Application application;
    private static int activityCount;
    private static WeakReference<Activity> mTopActivity;

    public static Activity getTopActivity() {
        if (mTopActivity != null) {
            return mTopActivity.get();
        } else {
            return null;
        }
    }

    public static void setLifecycleCallbacks(Application app) {
        application = app;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                // 计算启动的activity数目
                if (activity != null) {
                    activityCount++;
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (activity != null) {
                    if (mTopActivity != null) {
                        mTopActivity.clear();
                    }
                    mTopActivity = new WeakReference<Activity>(activity);
                    // 每次都要判断当前页面是否需要显示
                    checkShow(activity);
                }

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                if (activity != null) {
                    // 计算关闭的activity数目，并判断当前App是否处于后台
                    activityCount--;
                    checkHide(activity);
                }

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

    }

    /**
     * 判断浮窗是否需要显示
     */
    private static void checkShow(Activity activity) {
        ConcurrentHashMap<String, FloatingWindowHelper> windowMap = FloatingWindowManager.windowMap;
        for (String tag : windowMap.keySet()) {
            FloatingWindowHelper windowHelper = windowMap.get(tag);
            FloatConfig config = windowHelper.config;
            if (config != null) {
                if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                    // 当前页面的浮窗，不需要处理
                    return;
                } else if (config.showPattern == ShowPattern.BACKGROUND) {
                    // 仅后台显示模式下，隐藏浮窗
                    setVisible(false, tag);
                } else if (config.needShow) {
                    // 如果没有手动隐藏浮窗，需要考虑过滤信息
                    setVisible(!config.filterSet.contains(activity.getComponentName().getClassName()), tag);
                }
            }
        }
    }

    /**
     * 判断浮窗是否需要隐藏
     */
    private static void checkHide(Activity activity) {
        // 如果不是finish，并且处于前台，无需判断
        if (!activity.isFinishing() && isForeground()) {
            return;
        }
        ConcurrentHashMap<String, FloatingWindowHelper> windowMap = FloatingWindowManager.windowMap;
        for (String tag : windowMap.keySet()) {
            FloatingWindowHelper windowHelper = windowMap.get(tag);
            FloatConfig config = windowHelper.config;

            // 判断浮窗是否需要关闭
            if (activity.isFinishing()) {
                IBinder binder = windowHelper.params.token;
                if (binder != null) {
                    IBinder iBinder = activity.getWindow().getDecorView().getWindowToken();
                    if (binder == iBinder) {
                        FloatingWindowManager.dismiss(tag, true);
                    }
                }
            }
            if (!isForeground() && config.showPattern != ShowPattern.CURRENT_ACTIVITY) {
                // 当app处于后台时，全局、仅后台显示的浮窗，如果没有手动隐藏，需要显示
                setVisible(config.showPattern != ShowPattern.FOREGROUND && config.needShow, tag);
            }
        }
    }

    private static void setVisible(boolean isShow, String tag) {
        FloatingWindowHelper helper = FloatingWindowManager.windowMap.get(tag);
        if (helper != null && helper.config != null) {
            FloatingWindowManager.visible(isShow, tag, helper.config.needShow);
        }
        FloatingWindowManager.visible(isShow, tag, true);
    }

    public static boolean isForeground() {
        return activityCount > 0;
    }


}
