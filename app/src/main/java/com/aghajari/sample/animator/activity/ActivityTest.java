package com.aghajari.sample.animator.activity;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.rules.DebugRuleSectionWrapper;
import com.aghajari.axanimation.rules.DebugRuleWrapper;
import com.aghajari.sample.animator.R;

public class ActivityTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        View target = findViewById(R.id.target);

Paint paint = new Paint();
paint.setStyle(Paint.Style.STROKE);
paint.setStrokeWidth(20);
paint.setColor(Color.WHITE);

LiveSize cx = LiveSize.create(AXAnimation.CONTENT_WIDTH).divide(2);
LiveSize cy = LiveSize.create(AXAnimation.CONTENT_HEIGHT).divide(2);

AXAnimation.create().dp()
        .wrap(DebugRuleWrapper.class)
        .waitBefore(1000)
        .duration(1000)
        .scale(2f)
        .nextSectionWithDelay(500)
        .reversePreviousRuleSection()
        .start(target);

    }
}