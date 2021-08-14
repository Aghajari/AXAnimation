package com.aghajari.sample.animator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TouchView extends View {

    private final float delta = 0.01f;
    private final Paint paint;
    private final Paint paintText;
    private final Paint paintTouched;
    private final Path path;
    final PathMeasure pathMeasure;

    final float[] point = new float[2];
    private final PointF startPoint;
    private final PointF endPoint;
    private PointF currentPoint;
    private PointF nextPoint;
    private final ArrayList<PointF> points = new ArrayList<>();
    private float val;
    private float radius = 40;

    boolean done = false;
    boolean isTouching, canTouch;

    public TouchView(Context context) {
        super(context);
        radius = context.getResources().getDisplayMetrics().density * radius;

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(radius);

        paintTouched = new Paint(paint);
        paintTouched.setColor(Color.GREEN);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(40);

        radius /= 2;

        path = new Path();
        path.moveTo(100, 100);
        path.lineTo(600, 100);
        path.lineTo(600, 500);
        path.lineTo(100, 500);
        path.lineTo(100, 1000);


        pathMeasure = new PathMeasure(path, false);
        startPoint = getPoint(0);
        endPoint = getPoint(1);
        points.add(startPoint);
        currentPoint = new PointF(startPoint.x, startPoint.y);
        nextPoint = getPoint(val + delta);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
        canvas.drawPath(getSubPath(), paintTouched);
        canvas.drawText("Progress : " + Math.round(val * 100) + "%", 50, 50, paintText);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (done)
            return super.dispatchTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouching = isTouchingPoint(currentPoint, event);
                canTouch = isTouching;
                if (!isTouching) {
                    for (PointF p : points) {
                        if (isTouchingPoint(p, event)) {
                            canTouch = true;
                            break;
                        }
                    }
                }
                if (isTouching || canTouch) {
                    paint.setColor(Color.BLUE);
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTouching) {
                    if (isTouchingPoint(nextPoint, event)) {
                        goToNextPoint();
                        invalidate();
                    }
                } else if (canTouch) {
                    isTouching = isTouchingPoint(currentPoint, event);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isTouching || canTouch) {
                    isTouching = false;
                    canTouch = false;
                    paint.setColor(Color.RED);
                    invalidate();
                    return true;
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    protected boolean isTouchingPoint(PointF point, MotionEvent event) {
        return Math.sqrt(Math.pow(event.getX() - point.x, 2)
                + Math.pow(event.getY() - point.y, 2)) <= radius;
    }

    protected boolean isTouchingPoint(PointF point, PointF point2) {
        if (point2 == null || point == null) return false;
        return Math.sqrt(Math.pow(point2.x - point.x, 2)
                + Math.pow(point2.x - point.y, 2)) <= radius;
    }

    protected PointF getPoint(float val) {
        pathMeasure.getPosTan(pathMeasure.getLength() * Math.min(val, 1), point, null);
        return new PointF(point[0], point[1]);
    }

    protected void goToNextPoint() {
        if (done)
            return;

        val += delta;
        currentPoint = nextPoint;
        points.add(currentPoint);

        nextPoint = getPoint(val + delta);
        if (val >= 1) {
            val = 1;
            done = true;
            Toast.makeText(getContext(), "Done!", Toast.LENGTH_SHORT).show();
        }
    }

    private Path getSubPath() {
        Path subPath = new Path();
        pathMeasure.getSegment(0, val * pathMeasure.getLength(), subPath, true);
        return subPath;
    }
}
