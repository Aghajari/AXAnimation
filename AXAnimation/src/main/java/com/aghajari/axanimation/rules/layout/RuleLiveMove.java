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
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;
import com.aghajari.axanimation.utils.InspectUtils;

import java.util.Map;

/**
 * A {@link RuleLiveSize} to move a corner of the view to the given point
 *
 * @author AmirHossein Aghajari
 * @see LiveSize
 */
public class RuleLiveMove extends RuleLiveSize<Object[]> implements LiveSizeDebugger {
    private final int gravity;
    private final boolean widthLocked, heightLocked;

    public RuleLiveMove(int gravity, boolean widthLocked, boolean heightLocked, LiveSize x, LiveSize y) {
        super(x, y);
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
            tmpData = new Object[2];
            tmpData[0] = p;

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredWidth();
            int x = (int) data[0].calculate(vw, vh, parentSize, target, original,
                    gravity & Gravity.HORIZONTAL_GRAVITY_MASK);
            int y = (int) data[1].calculate(vw, vh, parentSize, target, original,
                    gravity & Gravity.VERTICAL_GRAVITY_MASK);
            tmpData[1] = new Point(x, y);
        }

        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Point point = (Point) valueAnimator.getAnimatedValue();
                RuleMove.update(target, gravity, point.x, point.y, w, h, widthLocked, heightLocked);
                update(view, target);
            }
        });
        return animator;
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
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            handler.debug(view, target, original, parentSize, gravity);
            InspectUtils.inspect(view, view, target, gravity, false);

            if (tmpData != null) {
                Point p = (Point) tmpData[tmpData.length - 1];
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

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(data[0], data[1], view, gravity);
    }
}
