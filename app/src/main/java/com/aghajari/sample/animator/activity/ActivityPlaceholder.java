package com.aghajari.sample.animator.activity;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.sample.animator.R;

public class ActivityPlaceholder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

AXAnimation.create()
        .waitBefore(1000)
        .duration(1000)
        .toCenterOf(AXAnimation.PARENT_ID)
        .scale(2f)
        .nextSectionWithDelay(500)
        .reversePreviousRuleSection()
        .copyOfView(true, true,
                AXAnimation.create()
                        .waitBefore(1000)
                        .duration(1000)
                        .scale(0.5f)
                        .delay(250).duration(750)
                        .backgroundColor(Color.MAGENTA)
                        .nextSectionWithDelay(500)
                        .reversePreviousRuleSection()
        )
        .start(findViewById(R.id.view1));
    }
}