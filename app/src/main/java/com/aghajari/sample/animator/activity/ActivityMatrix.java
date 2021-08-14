package com.aghajari.sample.animator.activity;

import android.graphics.Matrix;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.sample.animator.R;

public class ActivityMatrix extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);

        Matrix matrix = new Matrix();
        matrix.setSkew(0.15f, 0.15f);
        matrix.postScale(2f,2f);
        matrix.postTranslate(-150,-100);

        AXAnimation.create()
                .waitBefore(1000)
                .duration(1000)
                .toCenterOf(AXAnimation.PARENT_ID)
                .nextSectionWithDelay(500)
                .matrix(matrix)
                .start(findViewById(R.id.view2));
    }
}