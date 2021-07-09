package com.example.qfloat.core;

import android.content.Context;
import android.view.View;

import com.example.qfloat.EasyFloatMessage;
import com.example.qfloat.data.FloatConfig;

import java.util.concurrent.ConcurrentHashMap;

public class FloatingWindowManager {

    private static final String DEFAULT_TAG = "default";
    public static ConcurrentHashMap<String, FloatingWindowHelper> windowMap = new ConcurrentHashMap<>();


    /**
     * 创建浮窗，tag不存在创建，tag存在创建失败
     * 创建结果通过tag添加到相应的map进行管理
     */
    public static void create(Context context, FloatConfig config) {
        if (!checkTag(config)) {
            FloatingWindowHelper windowHelper = new FloatingWindowHelper(context, config);
            windowMap.put(config.floatTag, windowHelper);
            windowHelper.createWindow();
        } else {
            // 存在相同的tag，直接创建失败
            if (config.callbacks != null) {
                config.callbacks.createdResult(false, EasyFloatMessage.WARN_REPEATED_TAG, null);
            }
        }
    }

    /**
     * 关闭浮窗，执行浮窗的退出动画
     */
    public static void dismiss(String tag, boolean force) {
        FloatingWindowHelper helper = getHelper(tag);
        if (helper != null) {
            if (force) {
                helper.remove(force);
            } else {
                helper.exitAnim();
            }
        }
    }

    /**
     * 移除当条浮窗信息，在退出完成后调用
     */
    public static void remove(String floatTag) {
        windowMap.remove(getTag(floatTag));
    }

    /**
     * 设置浮窗的显隐，用户主动调用隐藏时，needShow需要为false
     */
    public static void visible(boolean isShow, String tag, boolean needShow) {
        FloatingWindowHelper helper = getHelper(tag);
        if (helper != null) {
            helper.setVisible(isShow ? View.VISIBLE : View.GONE, needShow);
        }
    }


    /**
     * 检测浮窗的tag是否有效，不同的浮窗必须设置不同的tag
     */
    public static boolean checkTag(FloatConfig config) {
        // 如果未设置tag，设置默认tag
        config.floatTag = getTag(config.floatTag);
        return windowMap.containsKey(config.floatTag);
    }


    /**
     * 获取浮窗tag，为空则使用默认值
     */
    public static String getTag(String tag) {
        if (tag != null) {
            return tag;
        } else {
            return DEFAULT_TAG;
        }
    }


    /**
     * 获取具体的系统浮窗管理类
     */
    public static FloatingWindowHelper getHelper(String tag) {
        return windowMap.get(getTag(tag));
    }


}
