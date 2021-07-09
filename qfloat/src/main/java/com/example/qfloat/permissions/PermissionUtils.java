package com.example.qfloat.permissions;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.example.qfloat.interfaces.OnPermissionResult;
import com.example.qfloat.permissions.rom.HuaweiUtils;
import com.example.qfloat.permissions.rom.MeizuUtils;
import com.example.qfloat.permissions.rom.MiuiUtils;
import com.example.qfloat.permissions.rom.OppoUtils;
import com.example.qfloat.permissions.rom.QikuUtils;
import com.example.qfloat.permissions.rom.RomUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PermissionUtils {
    public static final int requestCode = 199;
    private static final String TAG = "PermissionUtils--->";

    /**
     * 检测是否有悬浮窗权限
     * 6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
     */
    public static boolean checkPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (RomUtils.checkIsHuaweiRom()) {
                return HuaweiUtils.checkFloatWindowPermission(context);
            } else if (RomUtils.checkIsMiuiRom()) {
                return MiuiUtils.checkFloatWindowPermission(context);
            } else if (RomUtils.checkIsOppoRom()) {
                return OppoUtils.checkFloatWindowPermission(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return MeizuUtils.checkFloatWindowPermission(context);
            } else if (RomUtils.checkIs360Rom()) {
                return QikuUtils.checkFloatWindowPermission(context);
            } else {
                return true;
            }
        } else {
            return commonROMPermissionCheck(context);
        }
    }

    /**
     * 6.0以后，通用悬浮窗权限检测
     * 但是魅族6.0的系统这种方式不好用，需要单独适配一下
     */
    private static boolean commonROMPermissionCheck(Context context) {
        if (RomUtils.checkIsMeizuRom()) {
            return MeizuUtils.checkFloatWindowPermission(context);
        } else {
            boolean result = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    Class<Settings> clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

    /**
     * 申请悬浮窗权限
     */
    public static void requestPermission(Activity activity, OnPermissionResult onPermissionResult) {
        PermissionFragment.requestPermission(activity, onPermissionResult);
    }

    public static void requestPermission(Fragment fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (RomUtils.checkIsHuaweiRom()) {
                HuaweiUtils.applyPermission(fragment);
            } else if (RomUtils.checkIsMiuiRom()) {
                MiuiUtils.applyMiuiPermission(fragment);
            } else if (RomUtils.checkIsOppoRom()) {
                OppoUtils.applyOppoPermission(fragment);
            } else if (RomUtils.checkIsMeizuRom()) {
                MeizuUtils.applyPermission(fragment);
            } else if (RomUtils.checkIs360Rom()) {
                QikuUtils.applyPermission(fragment);
            }
        } else {
            commonROMPermissionApply(fragment);
        }

    }

    /**
     * 通用 rom 权限申请
     */
    private static void commonROMPermissionApply(Fragment fragment) {
        // 这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            MeizuUtils.applyPermission(fragment);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                commonROMPermissionApplyInternal(fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    public static void commonROMPermissionApplyInternal(Fragment fragment) {
        try {
            Class<Settings> clazz = Settings.class;
            Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
            Intent intent = new Intent(field.get(null).toString());
            intent.setData(Uri.parse("package:" + fragment.getActivity().getPackageName()));
            fragment.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
