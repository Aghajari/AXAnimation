package com.aghajari.sample.animator.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.sample.animator.R;

public class ActivityDrawPath extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);

        Path path = new Path();
        path.moveTo(100, 100);
        path.lineTo(600, 100);
        path.lineTo(600, 500);
        path.lineTo(100, 500);
        path.lineTo(100, 1000);

        AXAnimation.create()
                .waitBefore(1500)
                .duration(1000)
                .dp(this)
                .drawPath("path", true, Gravity.CENTER, paint, path)
                .backgroundColor(Color.BLUE)
                .textColor(Color.WHITE)
                .unlockY().unlockX()
                .toTop(150)
                .toRight(130)
                .nextSectionWithDelay(500)
                .reversePreviousRuleSection()
                .start(findViewById(R.id.view2));
    }
}