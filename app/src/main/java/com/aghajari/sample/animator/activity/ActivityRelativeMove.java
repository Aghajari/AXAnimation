package com.aghajari.sample.animator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.sample.animator.R;

public class ActivityRelativeMove extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        AXAnimation.create()
                .waitBefore(1000)
                .inspect(true).clearOldInspect(true)
                .repeatCount(1).repeatMode(AXAnimation.REVERSE)
                .duration(1500)
                .dp(this)
                .relativeMove(R.id.view1, Gravity.TOP | Gravity.END,
                        Gravity.BOTTOM | Gravity.START, -100, 100)
                .nextSectionWithDelay(500)
                .repeatCount(0)
                .toBottomOf(R.id.view1, Gravity.TOP, 100)
                .toLeftOf(R.id.view1, Gravity.RIGHT, -100)
                .withEndAction(animation -> {
                    Toast.makeText(this, "Double click to clear inspection", Toast.LENGTH_SHORT).show();
                })
                .start(findViewById(R.id.view2));
    }
}