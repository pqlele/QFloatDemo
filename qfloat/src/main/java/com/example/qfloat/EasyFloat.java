package com.example.qfloat;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.example.qfloat.core.FloatingWindowHelper;
import com.example.qfloat.core.FloatingWindowManager;
import com.example.qfloat.data.FloatConfig;
import com.example.qfloat.interfaces.OnDisplayHeight;
import com.example.qfloat.interfaces.OnFloatAnimator;
import com.example.qfloat.interfaces.OnFloatCallbacks;
import com.example.qfloat.interfaces.OnInvokeView;
import com.example.qfloat.interfaces.OnPermissionResult;
import com.example.qfloat.permissions.PermissionUtils;
import com.example.qfloat.utils.LifecycleUtils;

import java.util.ArrayList;
import java.util.Set;

public class EasyFloat {

    /**
     * 通过上下文，创建浮窗的构建者信息，使浮窗拥有一些默认属性
     *
     * @param context 上下文信息，优先使用Activity上下文，因为系统浮窗权限的自动申请，需要使用Activity信息
     * @return 浮窗属性构建者
     */
    public static Builder with(Context context) {
        if (context instanceof Activity) {
            return new Builder(context);
        } else {
            return new Builder(LifecycleUtils.getTopActivity() == null ? context : LifecycleUtils.getTopActivity());
        }
    }

    /**
     * 关闭当前浮窗
     *
     * @param tag   浮窗标签
     * @param force 立即关闭，有退出动画也不执行
     */
    public static void dismiss(String tag, boolean force) {
        FloatingWindowManager.dismiss(tag, force);
    }

    /**
     * 隐藏当前浮窗
     *
     * @param tag 浮窗标签
     */
    public static void hide(String tag) {
        FloatingWindowManager.visible(false, tag, false);
    }


    /**
     * 设置当前浮窗可见
     *
     * @param tag 浮窗标签
     */
    public static void show(String tag) {
        FloatingWindowManager.visible(true, tag, true);
    }

    /**
     * 设置当前浮窗是否可拖拽，先获取浮窗的config，后修改相应属性
     *
     * @param dragEnable 是否可拖拽
     * @param tag        浮窗标签
     */
    public static void dragEnable(boolean dragEnable, String tag) {
        FloatConfig config = getConfig(tag);
        if (config != null) {
            config.dragEnable = dragEnable;
        }
    }

    /**
     * 获取当前浮窗是否显示，通过浮窗的config，获取显示状态
     *
     * @param tag 浮窗标签
     * @return 当前浮窗是否显示
     */
    public static boolean isShow(String tag) {
        FloatConfig config = getConfig(tag);
        if (config != null) {
            return config.isShow;
        }
        return false;
    }

    /**
     * 获取当前浮窗中，我们传入的View
     *
     * @param tag 浮窗标签
     */
    public static View getFloatView(String tag) {
        FloatConfig config = getConfig(tag);
        if (config != null) {
            return config.layoutView;
        }
        return null;
    }


    /**
     * 更新浮窗坐标，未指定坐标执行吸附动画
     *
     * @param tag 浮窗标签
     * @param x   更新后的X轴坐标
     * @param y   更新后的Y轴坐标
     */
    public static void updateFloat(String tag, int x, int y) {
        FloatingWindowHelper helper = FloatingWindowManager.getHelper(tag);
        if (helper != null) {
            helper.updateFloat(x, y);
        }
    }

    public static void updateFloat(String tag) {
        updateFloat(tag, -1, -1);
    }

    /**
     * 为当前浮窗过滤，设置需要过滤的Activity
     *
     * @param activity 需要过滤的Activity
     * @param tag      浮窗标签
     */
    public static void filterActivity(Activity activity, String tag) {
        Set<String> filterSet = getFilterSet(tag);
        if (filterSet != null) {
            filterSet.add(activity.getComponentName().getClassName());
        }
    }

    /**
     * 为当前浮窗，设置需要过滤的Activity类名（一个或者多个）
     *
     * @param tag   浮窗标签
     * @param clazz 需要过滤的Activity类名，一个或者多个
     */
    public static void filterActivities(String tag, Class<?>... clazz) {
        Set<String> filterSet = getFilterSet(tag);
        if (filterSet != null) {
            ArrayList<String> list = new ArrayList<>();
            for (Class c : clazz) {
                list.add(c.getName());
            }
            filterSet.addAll(list);
        }
    }

    /**
     * 为当前浮窗，移除需要过滤的Activity
     *
     * @param activity 需要移除过滤的Activity
     * @param tag      浮窗标签
     */
    public static void removeFilter(Activity activity, String tag) {
        Set<String> filterSet = getFilterSet(tag);
        if (filterSet != null) {
            filterSet.remove(activity.getComponentName().getClassName());
        }
    }

    /**
     * 清除当前浮窗的所有过滤信息
     *
     * @param tag 浮窗标签
     */
    public static void clearFilters(String tag) {
        Set<String> filterSet = getFilterSet(tag);
        if (filterSet != null) {
            filterSet.clear();
        }
    }


    /**
     * 获取当前浮窗的过滤集合
     *
     * @param tag 浮窗标签
     */
    private static Set<String> getFilterSet(String tag) {
        FloatConfig config = getConfig(tag);
        if (config != null) {
            return config.filterSet;
        }
        return null;
    }

    /**
     * 获取当前浮窗的config
     *
     * @param tag 浮窗标签
     */
    private static FloatConfig getConfig(String tag) {
        FloatingWindowHelper helper = FloatingWindowManager.getHelper(tag);
        if (helper != null) {
            return helper.config;
        }
        return null;

    }


    /**
     * 浮窗的属性构建类，支持链式调用
     */
    public static class Builder implements OnPermissionResult {

        private Context context;
        // 创建浮窗数据类，方便管理配置
        private FloatConfig config = new FloatConfig();

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置浮窗的吸附模式
         *
         * @param sidePattern 浮窗吸附模式
         */
        public Builder setSidePattern(SidePattern sidePattern) {
            config.sidePattern = sidePattern;
            return this;
        }

        /**
         * 设置浮窗的显示模式
         */
        public Builder setShowPattern(ShowPattern showPattern) {
            config.showPattern = showPattern;
            return this;
        }


        /**
         * 设置浮窗的布局文件，以及布局的操作接口
         *
         * @param layoutId   布局文件的资源Id
         * @param invokeView 布局文件的操作接口
         */
        public Builder setLayout(int layoutId, OnInvokeView invokeView) {
            config.layoutId = layoutId;
            config.invokeView = invokeView;
            return this;
        }

        public Builder setLayout(int layoutId) {
            setLayout(layoutId, null);
            return this;
        }

        /**
         * 设置浮窗的对齐方式，以及偏移量
         *
         * @param gravity 对齐方式
         * @param offsetX 目标坐标的水平偏移量
         * @param offsetY 目标坐标的竖直偏移量
         */
        public Builder setGravity(int gravity, int offsetX, int offsetY) {
            config.gravity = gravity;
            config.offsetPair = new Pair<>(offsetX, offsetY);
            return this;
        }

        /**
         * 设置浮窗的起始坐标，优先级高于setGravity
         *
         * @param x 起始水平坐标
         * @param y 起始竖直坐标
         */
        public Builder setLocation(int x, int y) {
            config.locationPair = new Pair<>(x, y);
            return this;
        }


        /**
         * 设置浮窗的拖拽边距值
         *
         * @param left   浮窗左侧边距
         * @param top    浮窗顶部边距
         * @param right  浮窗右侧边距
         * @param bottom 浮窗底部边距
         */
        public Builder setBorder(int left, int top, int right, int bottom) {
            config.leftBorder = left;
            config.topBorder = top;
            config.rightBorder = right;
            config.bottomBorder = bottom;
            return this;
        }

        /**
         * 设置浮窗的标签：只有一个浮窗时，可以不设置；
         * 有多个浮窗必须设置不容的浮窗，不然没法管理，所以禁止创建相同标签的浮窗
         *
         * @param floatTag 浮窗标签
         */
        public Builder setTag(String floatTag) {
            config.floatTag = floatTag;
            return this;
        }

        /**
         * 设置浮窗是否可拖拽
         *
         * @param dragEnable 是否可拖拽
         */
        public Builder setDragEnable(boolean dragEnable) {
            config.dragEnable = dragEnable;
            return this;
        }

        /**
         * 设置浮窗是否状态栏沉浸
         *
         * @param immersionStatusBar 是否状态栏沉浸
         */
        public Builder setImmersionStatusBar(boolean immersionStatusBar) {
            config.immersionStatusBar = immersionStatusBar;
            return this;
        }


        /**
         * 浮窗是否包含EditText，浮窗默认不获取焦点，无法弹起软键盘，所以需要适配
         *
         * @param hasEditText 是否包含EditText
         */
        public Builder hasEditText(boolean hasEditText) {
            config.hasEditText = hasEditText;
            return this;
        }


        /**
         * 通过传统接口，进行浮窗的各种状态回调
         *
         * @param callbacks 浮窗的各种事件回调
         */
        public Builder registerCallbacks(OnFloatCallbacks callbacks) {
            config.callbacks = callbacks;
            return this;
        }


        /**
         * 设置浮窗的出入动画
         *
         * @param floatAnimator 浮窗的出入动画，为空时不执行动画
         */
        public Builder setAnimator(OnFloatAnimator floatAnimator) {
            config.floatAnimator = floatAnimator;
            return this;
        }

        /**
         * 设置屏幕的有效显示高度（不包含虚拟导航栏的高度）
         *
         * @param displayHeight 屏幕的有效高度
         */
        public Builder setDisplayHeight(OnDisplayHeight displayHeight) {
            config.displayHeight = displayHeight;
            return this;
        }

        /**
         * 设置浮窗宽高是否充满屏幕
         *
         * @param widthMatch  宽度是否充满屏幕
         * @param heightMatch 高度是否充满屏幕
         */
        public Builder setMatchParent(boolean widthMatch, boolean heightMatch) {
            config.widthMatch = widthMatch;
            config.heightMatch = heightMatch;
            return this;
        }


        /**
         * 设置需要过滤的Activity类名，仅对系统浮窗有效
         *
         * @param clazz 需要过滤的Activity类名
         */
        public Builder setFilter(Class<?>... clazz) {
            for (Class c : clazz) {
                config.filterSet.add(c.getName());
                if (context instanceof Activity) {
                    // 过滤掉当前Activity
                    if (c.getName() == ((Activity) context).getComponentName().getClassName()) {
                        config.filterSelf = true;
                    }
                }
            }
            return this;
        }

        /**
         * 创建浮窗，包括Activity浮窗和系统浮窗，如若系统浮窗无权限，先进行权限申请
         */
        public void show() {
            if (config.layoutId == 0) {
                // 未设置浮窗布局文件，不予创建
                callbackCreateFailed(EasyFloatMessage.WARN_NO_LAYOUT);
            } else if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                // 仅当页显示，则直接创建activity浮窗
                createFloat();
            } else if (PermissionUtils.checkPermission(context)) {
                // 系统浮窗需要先进行权限审核，有权限则创建app浮窗
                createFloat();
            } else {
                // 申请浮窗权限
                requestPermission();
            }
        }

        /**
         * 通过浮窗管理类，统一创建浮窗
         */
        private void createFloat() {
            FloatingWindowManager.create(context, config);
        }

        /**
         * 通过Fragment去申请系统悬浮窗权限
         */
        private void requestPermission() {
            if (context instanceof Activity) {
                PermissionUtils.requestPermission((Activity) context, this);
            } else {
                callbackCreateFailed(EasyFloatMessage.WARN_CONTEXT_REQUEST);
            }
        }

        /**
         * 回调创建失败
         *
         * @param reason 失败原因
         */
        private void callbackCreateFailed(String reason) {
            if (config.callbacks != null) {
                config.callbacks.createdResult(false, reason, null);
            }
            if (reason == EasyFloatMessage.WARN_NO_LAYOUT || reason == EasyFloatMessage.WARN_UNINITIALIZED || reason == EasyFloatMessage.WARN_CONTEXT_ACTIVITY) {
                // 针对无布局、未按需初始化、Activity浮窗上下文错误，直接抛异常
                Log.e("EasyFloat", "callbackCreateFailed:" + reason);
            }
        }


        /**
         * 申请浮窗权限的结果回调
         *
         * @param isOpen 悬浮窗权限是否打开
         */
        @Override
        public void permissionResult(boolean isOpen) {
            if (isOpen) {
                createFloat();
            } else {
                callbackCreateFailed(EasyFloatMessage.WARN_PERMISSION);
            }
        }
    }

}
