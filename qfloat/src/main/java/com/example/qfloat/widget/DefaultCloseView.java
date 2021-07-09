package com.example.qfloat.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.qfloat.interfaces.OnTouchRangeListener;
import com.example.qfloat.utils.DisplayUtils;

public class DefaultCloseView extends BaseSwitchView {

    private int normalColor = Color.parseColor("#99000000");
    private int inRangeColor = Color.parseColor("#99FF0000");
    private int shapeType = 0;

    private Paint paint;
    private Path path = new Path();
    private float width = 0f;
    private float height = 0f;
    private RectF rectF = new RectF();
    private Region region = new Region();
    private Region totalRegion = new Region();
    private boolean inRange = false;
    private float zoomSize = DisplayUtils.dp2px(getContext(), 8f);
    private OnTouchRangeListener listener;


    public DefaultCloseView(Context context) {
        this(context, null);
    }

    public DefaultCloseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultCloseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        initPaint();
        setWillNotDraw(false);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(normalColor);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.reset();
        if (inRange) {
            /*paint.setColor(inRangeColor);
            // 半椭圆
            rectF.set(getPaddingLeft(), 0f, width - getPaddingRight(), height * 2);
            path.addOval(rectF, Path.Direction.CW);*/

            //圆形
            paint.setColor(inRangeColor);
            path.addCircle(width, height, Math.min(width, height), Path.Direction.CW);


        } else {
            /*paint.setColor(normalColor);
            // 半椭圆
            rectF.set(getPaddingLeft() + zoomSize, zoomSize, width - getPaddingRight() - zoomSize, (height - zoomSize) * 2);
            path.addOval(rectF, Path.Direction.CW);
            totalRegion.set((int) (getPaddingLeft() + zoomSize), (int) zoomSize, (int) (width - getPaddingRight() - zoomSize), (int) height);
            region.setPath(path, totalRegion);*/

            paint.setColor(normalColor);
            path.addCircle(width, height, Math.min(width, height) - zoomSize, Path.Direction.CW);
            totalRegion.set((int)zoomSize, (int)zoomSize, (int)width, (int)height);
            region.setPath(path, totalRegion);
        }

        canvas.drawPath(path, paint);

        super.onDraw(canvas);
    }

    @Override
    public void setTouchRangeListener(MotionEvent event, OnTouchRangeListener listener) {
        this.listener = listener;
        initTouchRange(event);
    }

    private boolean initTouchRange(MotionEvent event) {
        int[] location = new int[2];
        // 获取在整个屏幕内的绝对坐标
        getLocationOnScreen(location);
        boolean currentInRange = region.contains(
                (int) (event.getRawX() - location[0]), (int) (event.getRawY() - location[1])
        );
        if (currentInRange != inRange) {
            inRange = currentInRange;
            invalidate();
        }
        if (listener != null) {
            listener.touchInRange(currentInRange, this);
        }

        if (event.getAction() == MotionEvent.ACTION_UP && currentInRange) {
            if (listener != null) {
                listener.touchUpInRange();
            }
        }
        return currentInRange;
    }

}
