package com.wallstreetcn.qfloatdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qfloat.EasyFloat;
import com.example.qfloat.utils.DisplayUtils;

public class ThirdActivity extends AppCompatActivity {

    private View contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        contentView = findViewById(R.id.view_content);
    }

    public void testFinishScale(View view) {
        // 本页面调试，结束动画
        // 通过tag, 获取当前浮窗view
        View floatView = EasyFloat.getFloatView("showAppFloat");
        if (floatView != null) {
            int[] location = new int[2];
            floatView.getLocationOnScreen(location);
            //float x = location[0];
            //float y = location[1];
            float x = location[0] + floatView.getWidth() / 2f;
            float y = location[1] + floatView.getHeight() / 2f - DisplayUtils.getStatusBarHeight(this);
            close(contentView, x, y);
        } else {
            finish();
        }

    }


    public void close(View view, float pivotX, float pivotY) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setAlpha(value);
                view.setScaleX(value);
                view.setScaleY(value);
                view.setPivotX(pivotX);
                view.setPivotY(pivotY);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        animator.setDuration(350);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

}