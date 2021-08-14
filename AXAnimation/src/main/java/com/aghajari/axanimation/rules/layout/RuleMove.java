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
package com.aghajari.axanimation.rules.layout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.evaluator.PointEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

/**
 * A {@link Rule} to move a corner of the view to the given point
 *
 * @author AmirHossein Aghajari
 */
public class RuleMove extends RuleWithTmpData<Point[], Object[]> {
    private final int gravity;
    private final boolean widthLocked, heightLocked;

    public RuleMove(int gravity, boolean widthLocked, boolean heightLocked, Point... point) {
        super(point);
        this.widthLocked = widthLocked;
        this.heightLocked = heightLocked;
        this.gravity = gravity == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravity;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return PointEvaluator.class;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        final Point p = original.getPoint(gravity);
        final int w = target.getWidth(), h = target.getHeight();

        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);
            tmpData[0] = p;
        }

        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Point point = (Point) valueAnimator.getAnimatedValue();
                update(target, gravity, point.x, point.y, w, h, widthLocked, heightLocked);
                update(view, target);
            }
        });
        return animator;
    }


    static void update(LayoutSize target, int gravity, int x, int y, int w, int h, boolean xLocked, boolean yLocked) {
        final int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        switch (hg) {
            case Gravity.CENTER_HORIZONTAL:
                target.left = x - w / 2;
                target.right = x + w / 2;
                break;
            case Gravity.RIGHT:
                target.right = x;
                if (xLocked)
                    target.left = x - w;
                break;
            case Gravity.LEFT:
                target.left = x;
                if (xLocked)
                    target.right = x + w;
                break;
        }

        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        switch (verticalGravity) {
            case Gravity.TOP:
                target.top = y;
                if (yLocked)
                    target.bottom = y + h;
                break;
            case Gravity.CENTER_VERTICAL:
                target.top = y - h / 2;
                target.bottom = y + h / 2;
                break;
            case Gravity.BOTTOM:
                target.bottom = y;
                if (yLocked)
                    target.top = y - h;
                break;
        }
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize) {
        super.debug(view, target, original, parentSize);
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
            InspectUtils.inspect(view, view, target, gravity, false);

            Point p = data[data.length - 1];
            LayoutSize relatedLayout = new LayoutSize(p.x, p.y, p.x, p.y);
            InspectUtils.inspect(view, null, relatedLayout, gravity, false);
            if (isReverse()) {
                InspectUtils.inspect(view, view, relatedLayout, target, gravity, gravity, null);
            } else {
                InspectUtils.inspect(view, view, target, relatedLayout, gravity, gravity, null);
            }
        }
    }
}
