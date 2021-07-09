package com.wallstreetcn.qfloatdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qfloat.EasyFloat;
import com.example.qfloat.ShowPattern;
import com.example.qfloat.SidePattern;
import com.example.qfloat.anim.CloseFloatAnimator;
import com.example.qfloat.interfaces.OnFloatCallbacks;
import com.example.qfloat.interfaces.OnInvokeView;
import com.example.qfloat.interfaces.OnTouchRangeListener;
import com.example.qfloat.utils.DragUtils;
import com.example.qfloat.widget.BaseSwitchView;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    /**
     * 显示浮窗
     */
    public void showFloat(View view) {
        String tag = "showAppFloat";
        EasyFloat.with(this)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setShowPattern(ShowPattern.FOREGROUND)
                .setImmersionStatusBar(false)
                .setGravity(Gravity.CENTER_VERTICAL | Gravity.END, 0, 10)
                .setLayout(R.layout.test_view_float_custom, new OnInvokeView() {
                    @Override
                    public void invoke(View view) {
                        view.findViewById(R.id.tv_float).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(SecondActivity.this, "点击浮窗", Toast.LENGTH_SHORT).show();
                                // test start to ThirdActivity
                                Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                })
                .setTag(tag)
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void createdResult(boolean isCreated, String msg, View view) {
                        Toast.makeText(SecondActivity.this, "isCreated:" + isCreated, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void show(View view) {
                        Toast.makeText(SecondActivity.this, "show", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void hide(View view) {
                        Toast.makeText(SecondActivity.this, "hide", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void dismiss() {
                        Toast.makeText(SecondActivity.this, "dismiss", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void touchEvent(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            TextView textView = view.findViewById(R.id.tv_float);
                            textView.setText("拖一下试试");
                        }
                    }

                    @Override
                    public void drag(View view, MotionEvent event) {
                        TextView textView = view.findViewById(R.id.tv_float);
                        textView.setText("我被拖拽...");

                        DragUtils.registerDragClose(event, new OnTouchRangeListener() {
                            @Override
                            public void touchInRange(boolean inRange, BaseSwitchView view) {
                                // 拖到位了,震一下
                                vibrate(SecondActivity.this, 20, inRange);
                                // ...
                            }

                            @Override
                            public void touchUpInRange() {
                                EasyFloat.dismiss(tag, true);
                            }
                        }, R.layout.default_close_layout, ShowPattern.CURRENT_ACTIVITY, new CloseFloatAnimator());
                    }

                    @Override
                    public void dragEnd(View view) {
                        TextView textView = view.findViewById(R.id.tv_float);
                        textView.setText("拖拽结束");
                    }
                })
                .show();

    }

    private boolean vibrating;

    /**
     * 震动设置
     */
    public void vibrate(Context context, int millionSeconds, boolean inRange) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (!vibrator.hasVibrator() || (inRange && vibrating)) {
                return;
            }
            vibrating = inRange;
            if (inRange) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(millionSeconds, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(millionSeconds);
                }
            } else {
                vibrator.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 跳转 -> ThirdActivity
     *
     * @param view
     */
    public void startThird(View view) {
        startActivity(new Intent(this, ThirdActivity.class));
    }
}