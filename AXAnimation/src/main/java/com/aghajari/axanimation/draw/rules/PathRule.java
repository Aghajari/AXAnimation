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
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.annotation.LineGravity;
import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A {@link DrawRule} to draw a path.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawPath(Path, Paint)
 */
public class PathRule extends DrawRule<Path, Void, Float> {

    private final PathMeasure pathMeasure;
    private Path animatedPath;
    @LineGravity
    private final int gravity;


    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param gravity     {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param path        The path to be drawn
     */
    public PathRule(Paint paint, String key, boolean drawOnFront, @LineGravity int gravity, Path path) {
        super(paint, key, drawOnFront, path);

        animatedValue = null;
        this.gravity = gravity;
        this.animatedPath = null;
        pathMeasure = new PathMeasure(path, false);
    }

    @Override
    protected void updateValue(float fraction, View target, Float value) {
        super.updateValue(fraction, target, value);

        if (fraction == 0)
            return;

        if (fraction == 1) {
            animatedPath = data;
            return;
        }

        if (animatedPath == null)
            animatedPath = new Path();
        else
            animatedPath.reset();

        float len = pathMeasure.getLength();
        float start, end;

        switch (gravity) {
            case Gravity.CENTER:
                start = (0.5f - (fraction / 2f)) * len;
                end = (0.5f + (fraction / 2f)) * len;
                break;
            case Gravity.END:
                start = (1 - fraction) * len;
                end = len;
                break;
            case Gravity.START:
                start = 0;
                end = fraction * len;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + gravity);
        }

        pathMeasure.getSegment(start, end, animatedPath, true);
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (animatedPath != null)
            canvas.drawPath(animatedPath, getPaint());
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        return ValueAnimator.ofFloat(0, 1);
    }
}