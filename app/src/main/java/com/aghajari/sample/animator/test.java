package com.aghajari.sample.animator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.evaluator.ArgbEvaluator;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.livevar.LiveVarUpdater;
import com.aghajari.axanimation.rules.DebugRuleSectionWrapper;
import com.aghajari.axanimation.rules.DebugRuleWrapper;
import com.aghajari.axanimation.rules.RuleSection;

import java.util.Random;

public class test extends View {

    public test(Context context) {
        super(context);
    }

    public void test(){
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.rightMargin = 200;
        lp.bottomMargin = 200;

        /*AXAnimation.create()
                .duration(800)
                .relativeMove(findViewById(R.id.view1), 0, Gravity.BOTTOM | Gravity.RIGHT, 0, 0)
                .nextSectionWithDelay(500)
                .relativeMove(findViewById(R.id.view2), Gravity.BOTTOM | Gravity.RIGHT, 0, 0, 0)
                .nextSectionWithReverseDelay(400)
                .duration(400)
                .backgroundColor(Color.WHITE, Color.RED)
                .nextSectionWithDelay(500)
                .visibility(View.GONE)
                .nextSection()
                .visibility(View.VISIBLE)
                .scale(2f)
                .delay(200)
                .duration(800)
                .backToFirstPlace()
                .nextSection()
                .flipVerticalToHide()
                .nextSectionWithDelay(500)
                .flipVerticalToShow()
                .save("movement");

        AXAnimationSet.delay(1000)
                .thenAnimate(
                        AXAnimation.create()
                                .duration(800)
                                .toBottomOf(AXAnimation.PARENT_ID, Gravity.BOTTOM, 0),
                        findViewById(R.id.view2))
                .andAnimate(
                        AXAnimation.create()
                                .duration(800)
                                .toTop(0),
                        findViewById(R.id.view1))
                .thenDelay(500)
                .andAnimate( "movement",findViewById(R.id.view))
                .start();
         */



        AXAnimation.create()
                .wrap(DebugRuleWrapper.class)
                .wrap(DebugRuleSectionWrapper.class, true)
                .textColor(Color.WHITE)
                .waitBefore(1600)
                .duration(2000)
                .repeatCount(1)
                .repeatMode(AXAnimation.REVERSE)
                .inspect(true)
                .clearOldInspect(true)
                .dp()
                .relativeMove(findViewById(0), Gravity.TOP | Gravity.RIGHT,
                        Gravity.BOTTOM | Gravity.LEFT, -100, 100)
                .nextSection()
                .delay(2000)
                .repeatCount(0)
                .repeatMode(AXAnimation.REVERSE)
        ;//.start(findViewById(R.id.view1));


        //.start(findViewById(R.id.view));

        /*AXAnimation animation = AXAnimation.create().wrap(DebugRuleWrapper.class);
        for (int i = 1; i <= 5; i++) {
            animation.duration(1500)
                    .firstValueFromView(false)
                    .drawText("text", true, false, txt, Gravity.CENTER, 400, 400, "Hello " + i)
                    .duration(1000)
                    .drawSetPaint(txt, "textSize", false, 50f, 100f)
                    .duration(500)
                    .drawSetPaint(txt, "color", false, new ArgbEvaluator(), Color.TRANSPARENT, Color.RED)
                    .delay(1000).duration(500)
                    .drawSetPaint(txt, "color", false, new ArgbEvaluator(), Color.RED, Color.TRANSPARENT)
                    .nextSectionWithDelay(100);
        }
        animation.start(findViewById(R.id.view));*/


        AXAnimation.create()
                .wrap(DebugRuleWrapper.class)
                .duration(400)
                .toCenterOf(AXAnimation.PARENT_ID)
                .nextSectionWithDelay(500)
                .press()
                .fadeIn();
        //.start(findViewById(R.id.view1));

        /**AXAnimation a = AXAnimation.create()
         .waitBefore(1500)
         .toCenterOf(AXAnimation.PARENT_ID)
         .nextSection()
         .toLeft(LiveSize.create().plus(R.id.view2,Gravity.LEFT).minus(R.id.view2,Gravity.FILL_HORIZONTAL));
         a.start(findViewById(R.id.view1));*/
        //setContentView(new TouchView(this));
        //setContentView(new TestEditText(this));
    }
}
