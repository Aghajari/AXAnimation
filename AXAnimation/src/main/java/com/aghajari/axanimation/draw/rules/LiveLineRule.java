/*
 * Copyright (C) 2021 - Amir Hossein Aghajari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.aghajari.axanimation.draw.rules;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.annotation.LineGravity;
import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.evaluator.PointFArrayEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;
import com.aghajari.axanimation.livevar.LiveSizePoint;

import java.util.Map;


/**
 * A {@link DrawRule} to draw a line.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawLine(float, float, float, float, Paint)
 */
public class LiveLineRule extends DrawRule<LiveSizePoint[][], Object[], PointF[]> implements LiveSizeDebugger {

    @LineGravity
    private final int gravity;

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param gravity     {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param values      Array of <code>LiveSizePoint[]</code>,
     *                    the LiveSizePoint[] must contains two point for start and end of the line.
     */
    public LiveLineRule(Paint paint, String key, boolean drawOnFront, @LineGravity int gravity, LiveSizePoint[]... values) {
        super(paint, key, drawOnFront, values);

        animatedValue = null;
        this.gravity = gravity;
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (animatedValue != null && !animatedValue[0].equals(animatedValue[1]))
            canvas.drawLine(animatedValue[0].x, animatedValue[0].y,
                    animatedValue[1].x, animatedValue[1].y, getPaint());
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return PointFArrayEvaluator.class;
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();

            for (int i = 0; i < data.length; i++) {
                LiveSizePoint[] liveSizePoints = data[i];
                PointF[] points = new PointF[liveSizePoints.length];
                for (int j = 0; j < liveSizePoints.length; j++) {
                    LiveSizePoint lsp = liveSizePoints[j];
                    lsp.prepare(vw, vh, parentSize, target, original);
                    points[j] = lsp.getPointF();
                }
                tmpData[i + 1] = points;
            }

            PointF[] points = (PointF[]) tmpData[1];
            PointF start;
            switch (gravity) {
                case Gravity.CENTER:
                    start = new PointF((points[0].x + points[1].x) / 2,
                            (points[0].y + points[1].y) / 2);
                    break;
                case Gravity.END:
                    start = new PointF(points[1].x, points[1].y);
                    break;
                case Gravity.START:
                    start = new PointF(points[0].x, points[0].y);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + gravity);
            }

            tmpData[0] = new PointF[]{start, start};
        }

        return ValueAnimator.ofObject(createEvaluator(), tmpData);
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debugLine(view, data);
    }
}