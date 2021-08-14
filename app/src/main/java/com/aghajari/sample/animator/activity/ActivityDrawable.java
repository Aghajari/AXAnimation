package com.aghajari.sample.animator.activity;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.sample.animator.R;

/**
 * @see com.aghajari.axanimation.evaluator.DrawableEvaluator
 * @see com.aghajari.axanimation.rules.property.RuleBackgroundDrawable
 * @see AXAnimation#background(Drawable...)
 */
public class ActivityDrawable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable);

        // DrawableEvaluator supports ColorDrawable, radius, stroke and all other properties

        /*GradientDrawable cd2 = new GradientDrawable();
        cd2.setColors(new int[]{Color.GREEN, Color.YELLOW});
        cd2.setOrientation(GradientDrawable.Orientation.TL_BR);
        cd2.setCornerRadius(100);
        cd2.setStroke(20, Color.RED, 0, 0);*/

        View view = findViewById(R.id.parent);

        GradientDrawable[] drawables = new GradientDrawable[4];
        drawables[0] = new GradientDrawable();
        drawables[0].setColors(new int[]{0xff7141e2, 0xffd46cb3});
        drawables[0].setOrientation(GradientDrawable.Orientation.BR_TL);
        drawables[1] = new GradientDrawable();
        drawables[1].setColors(new int[]{0xff57caa8, 0xff44c74b});
        drawables[1].setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        drawables[2] = new GradientDrawable();
        drawables[2].setColors(new int[]{0xff680b0b, 0xffc6b147});
        drawables[2].setOrientation(GradientDrawable.Orientation.BL_TR);
        drawables[3] = new GradientDrawable();
        drawables[3].setColors(new int[]{0xffc44e4e, 0xffdcb9b9});
        drawables[3].setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);

        AXAnimation animation = AXAnimation.create().duration(5000);
        for (GradientDrawable drawable : drawables) {
            animation.background(drawable).nextSection();
        }
        animation.animationRepeatCount(AXAnimation.INFINITE);
        animation.animationRepeatMode(AXAnimation.RESTART);
        animation.start(view);
    }
}