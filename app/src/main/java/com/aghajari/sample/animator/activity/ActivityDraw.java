package com.aghajari.sample.animator.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.evaluator.ArgbEvaluator;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.livevar.LiveVarUpdater;
import com.aghajari.axanimation.rules.RuleSection;
import com.aghajari.axanimation.rules.WaitRule;
import com.aghajari.sample.animator.R;

import java.util.Random;

/**
 * @see com.aghajari.axanimation.draw.DrawableLayout
 * @see com.aghajari.axanimation.draw.CanvasView
 * @see com.aghajari.axanimation.draw.DrawRule
 * @see com.aghajari.axanimation.draw.rules.ArcRule
 * @see com.aghajari.axanimation.draw.rules.LineRule
 * @see com.aghajari.axanimation.draw.rules.TextRule
 */
public class ActivityDraw extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        View view = findViewById(R.id.view);

        LiveVar<CharSequence> text = LiveVar.ofValue("");
        LiveVar<Integer[]> startColor = LiveVar.ofArray();
        LiveVar<Integer[]> endColor = startColor.reverseArray();

        Paint textPaint = new Paint();
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);

        int cx = AXAnimation.ORIGINAL | Gravity.CENTER_HORIZONTAL;
        int cy = AXAnimation.ORIGINAL | Gravity.CENTER_VERTICAL;

        AXAnimation.create()
                .updateLiveVar(LiveVarUpdater.forEachSection(text,
                        "", "Hello 1", "Hello 2", "Hello 3", "Hello 4", "Hello 5"))
                .updateLiveVar(new LiveVarUpdater(startColor) {
                    final Random rnd = new Random();

                    @Override
                    public void update(AXAnimation animation, int sectionIndex, int realSectionIndex, RuleSection section) {
                        if (!(section instanceof WaitRule)) {
                            int color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                            target.update(Color.TRANSPARENT, color);
                            paint.setColor(color);
                        }
                    }
                })
                .duration(7500)
                .drawCircle("circle", true, paint, cx, cy, 200, false, -90)
                .nextSectionImmediate()
                .duration(1500).firstValueFromView(false)
                .drawText("text", true, false, textPaint, Gravity.CENTER, cx, cy, text)
                .duration(1000)
                .drawSetPaint(textPaint, "textSize", false, 50f, 100f)
                .duration(500)
                .drawSetPaint(textPaint, "color", false, ArgbEvaluator.getInstance(), startColor)
                .delay(1000).duration(500)
                .drawSetPaint(textPaint, "color", false, ArgbEvaluator.getInstance(), endColor)
                .nextSectionWithDelay(100)
                .repeatPreviousRuleSection(4, AXAnimation.RESTART, 100)
                .nextSection()
                .applyAnimatorForReverseRules(true)
                .duration(2000)
                .reverseRuleSection(0)
                .start(view);

        LiveSize liveSize = LiveSize.create(AXAnimation.PARENT_HEIGHT).minus(100);

        AXAnimation.create()
                .duration(1000)
                .drawLine("line", true, Gravity.CENTER, paint,
                        0, 100, AXAnimation.MATCH_PARENT, 100)
                .drawLine("line2", true, Gravity.CENTER, paint,
                        LiveSize.create(), liveSize, LiveSize.create(AXAnimation.MATCH_PARENT), liveSize)
                .animationRepeatMode(AXAnimation.REVERSE)
                .animationRepeatCount(AXAnimation.INFINITE)
                .start(view);
    }
}