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
import com.aghajari.axanimation.layouts.AnimatedLayout;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.layouts.OnLayoutSizeReadyListener;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

/**
 * A {@link Rule} to move a corner of the view to the given point
 *
 * @author AmirHossein Aghajari
 */
public class RuleRelativeMove extends RuleWithTmpData<Point, Object[]> {
    private final int gravityOfSource, gravityOfTarget;
    private final boolean widthLocked, heightLocked;
    final int viewId;
    View relatedView;
    LayoutSize relatedLayout;

    public RuleRelativeMove(int gravityOfSource, int gravityOfTarget, int relatedView, Point point, boolean widthLocked, boolean heightLocked) {
        super(point);
        this.viewId = relatedView;
        this.relatedView = null;
        this.widthLocked = widthLocked;
        this.heightLocked = heightLocked;
        this.gravityOfSource = gravityOfSource == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravityOfSource;
        this.gravityOfTarget = gravityOfTarget == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravityOfTarget;
    }

    public RuleRelativeMove(int gravityOfSource, int gravityOfTarget, View relatedView, Point point, boolean widthLocked, boolean heightLocked) {
        super(point);
        this.viewId = -1;
        this.relatedView = relatedView;
        this.widthLocked = widthLocked;
        this.heightLocked = heightLocked;
        this.gravityOfSource = gravityOfSource == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravityOfSource;
        this.gravityOfTarget = gravityOfTarget == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravityOfTarget;
    }

    @Override
    public long shouldWait() {
        return (relatedView == null || relatedLayout != null) ? super.shouldWait() : 0;
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);
        if (relatedView == null && viewId != -1) {
            relatedView = ((View) view.getParent()).findViewById(viewId);
        }

        if (relatedView != null) {
            AnimatedLayout layout = (AnimatedLayout) view.getParent();
            layout.getLayoutSize(relatedView, new OnLayoutSizeReadyListener() {
                @Override
                public void onReady(View view, LayoutSize size) {
                    relatedLayout = size;
                }
            });
        }
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return PointEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (relatedView == null && viewId == -1 && relatedLayout == null)
            relatedLayout = parentSize;

        if (!isReverse() || tmpData == null) {
            final Point p = original.getPoint(gravityOfSource);
            final Point p2 = relatedLayout.getPoint(gravityOfTarget);
            p2.x += data.x;
            p2.y += data.y;
            tmpData = new Object[]{p, p2};
        }

        final int w = target.getWidth(), h = target.getHeight();

        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Point point = (Point) valueAnimator.getAnimatedValue();
                RuleMove.update(target, gravityOfSource, point.x, point.y, w, h, widthLocked, heightLocked);
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
        super.debug(view, target, original, parentSize);
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
            InspectUtils.inspect(view, view, target, gravityOfSource, false);
            InspectUtils.inspect(view, relatedView, relatedLayout, gravityOfTarget, false);

            if (isReverse()) {
                InspectUtils.inspect(view, view, relatedLayout, target, gravityOfTarget, gravityOfSource, null);
            } else {
                InspectUtils.inspect(view, view, target, relatedLayout, gravityOfSource, gravityOfTarget, null);
            }
        }
    }
}
