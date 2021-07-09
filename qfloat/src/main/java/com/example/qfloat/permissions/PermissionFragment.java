package com.example.qfloat.permissions;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.qfloat.interfaces.OnPermissionResult;

public class PermissionFragment extends Fragment {

    private static OnPermissionResult onPermissionResult;

    public static void requestPermission(Activity activity, OnPermissionResult result) {
        onPermissionResult = result;
        activity.getFragmentManager()
                .beginTransaction()
                .add(new PermissionFragment(), activity.getLocalClassName())
                .commitAllowingStateLoss();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 权限申请
        PermissionUtils.requestPermission(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtils.requestCode) {
            // 需要延迟执行，不然即使授权，仍有部分机型获取不到权限
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;

                    boolean check = PermissionUtils.checkPermission(getActivity());
                    // 回调权限结果
                    if (onPermissionResult != null) {
                        onPermissionResult.permissionResult(check);
                    }
                    onPermissionResult = null;
                    // 将Fragment移除
                    getFragmentManager().beginTransaction().remove(PermissionFragment.this).commitAllowingStateLoss();
                }
            }, 500);
        }

    }
}
