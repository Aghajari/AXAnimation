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
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.evaluator.RectFEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.utils.SizeUtils;

/**
 * A {@link DrawRule} to draw a rect.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawRect(Rect, Paint)
 */
public class RectRule extends DrawRule<RectF[], Object[], RectF> {

    private final int gravity;

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rect to be drawn. (Animator values)
     */
    public RectRule(Paint paint, String key, boolean drawOnFront, int gravity, RectF... values) {
        super(paint, key, drawOnFront, values);

        animatedValue = null;
        this.gravity = gravity;
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (animatedValue != null && !animatedValue.isEmpty())
            canvas.drawRect(animatedValue, getPaint());
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return RectFEvaluator.class;
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);
            tmpData[0] = null;

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();

            for (Object o : tmpData) {
                if (o == null)
                    continue;

                RectF size = (RectF) o;
                size.left = SizeUtils.calculate(size.left, vw, vh, parentSize, target, original, Gravity.LEFT);
                size.top = SizeUtils.calculate(size.top, vw, vh, parentSize, target, original, Gravity.TOP);
                size.right = SizeUtils.calculate(size.right, vw, vh, parentSize, target, original, Gravity.RIGHT);
                size.bottom = SizeUtils.calculate(size.bottom, vw, vh, parentSize, target, original, Gravity.BOTTOM);
            }

            PointF point = getPoint(data[0]);
            tmpData[0] = new RectF(point.x, point.y, point.x, point.y);
        }

        return ValueAnimator.ofObject(createEvaluator(), tmpData);
    }

    private PointF getPoint(RectF r) {
        PointF p = new PointF();
        final int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        switch (hg) {
            case Gravity.RIGHT:
                p.x = r.right;
                break;
            case Gravity.LEFT:
                p.x = r.left;
                break;
            case Gravity.FILL_HORIZONTAL:
                p.x = r.width();
                break;
            default:
                p.x = r.centerX();
                break;
        }

        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        switch (verticalGravity) {
            case Gravity.TOP:
                p.y = r.top;
                break;
            case Gravity.BOTTOM:
                p.y = r.bottom;
                break;
            case Gravity.FILL_VERTICAL:
                p.y = r.height();
                break;
            default:
                p.y = r.centerY();
                break;
        }

        return p;
    }
}