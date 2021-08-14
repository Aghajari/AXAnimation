package com.aghajari.sample.animator.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.AXAnimationSet;
import com.aghajari.sample.animator.R;

public class ActivityAnimationSet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        AXAnimation.create()
                .duration(1000)
                .toBottom(AXAnimation.MATCH_PARENT)
                .nextSection()
                .toLeft(0)
                .nextSectionWithDelay(500)
                .backToFirstPlace()
                .save("v1");

        AXAnimation.create()
                .duration(1000)
                .toTop(0)
                .nextSection()
                .toRight(AXAnimation.MATCH_PARENT)
                .nextSectionWithDelay(500)
                .backToFirstPlace()
                .save("v2");

        AXAnimationSet.delay(1000)
                .andAnimate("v1", findViewById(R.id.view1))
                .andAnimate("v2", findViewById(R.id.view2))
                .start();
    }
}