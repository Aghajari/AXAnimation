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
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.utils.SizeUtils;

/**
 * A {@link DrawRule} to draw an arc.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
 */
public class ArcRule extends DrawRule<float[], float[], Float> {

    private final float startAngle;
    private final boolean isCircle;
    private final float cx, cy, radius;
    private final boolean useCenter;
    private RectF oval;

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param cx          The x-coordinate of the center of the circle to be drawn
     * @param cy          The y-coordinate of the center of the circle to be drawn
     * @param radius      The radius of the circle to be drawn
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngles Sweep angle (in degrees) measured clockwise (Animator values)
     */
    public ArcRule(Paint paint, String key, boolean drawOnFront,
                   float cx, float cy, float radius, boolean useCenter, float startAngle, float... sweepAngles) {
        super(paint, key, drawOnFront, sweepAngles);
        animatedValue = Float.NaN;
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.useCenter = useCenter;
        this.startAngle = startAngle;
        isCircle = true;
    }

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param oval        The bounds of oval used to define the shape and size of the arc
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngles Sweep angle (in degrees) measured clockwise (Animator values)
     */
    public ArcRule(Paint paint, String key, boolean drawOnFront,
                   RectF oval, boolean useCenter, float startAngle, float... sweepAngles) {
        super(paint, key, drawOnFront, sweepAngles);
        animatedValue = Float.NaN;
        this.oval = oval;
        this.cx = 0;
        this.cy = 0;
        this.radius = 0;
        this.useCenter = useCenter;
        this.startAngle = startAngle;
        isCircle = false;
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (oval != null && !animatedValue.isNaN())
            canvas.drawArc(oval, startAngle, animatedValue, useCenter, getPaint());
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new float[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);
            tmpData[0] = 0;

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();
            if (isCircle) {
                float cx = SizeUtils.calculate(this.cx, vw, vh, parentSize, target, original, Gravity.LEFT);
                float cy = SizeUtils.calculate(this.cy, vw, vh, parentSize, target, original, Gravity.TOP);
                oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            } else {
                oval.left = SizeUtils.calculate(oval.left, vw, vh, parentSize, target, original, Gravity.LEFT);
                oval.top = SizeUtils.calculate(oval.top, vw, vh, parentSize, target, original, Gravity.TOP);
                oval.right = SizeUtils.calculate(oval.right, vw, vh, parentSize, target, original, Gravity.RIGHT);
                oval.bottom = SizeUtils.calculate(oval.bottom, vw, vh, parentSize, target, original, Gravity.BOTTOM);
            }
        }

        return ValueAnimator.ofFloat(tmpData);
    }
}