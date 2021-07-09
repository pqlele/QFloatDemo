package com.example.qfloat.utils;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class InputMethodUtils {

    @SuppressLint("ClickableViewAccessibility")
    public static void initInputMethod(EditText editText, String tag) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    openInputMethod(editText, tag);
                }
                return false;
            }
        });
    }

    /**
     * 让浮窗获取焦点，并打开软键盘
     */
    public static void openInputMethod(EditText editText, String tag) {


    }

    /**
     * 当软键盘关闭时，调用此方法，移除系统浮窗的焦点，不然系统返回键无效
     */
    public static void closedInputMethod(String tag) {

    }

}
