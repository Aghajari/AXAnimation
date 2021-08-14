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
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

/**
 * A {@link Rule} to move axis of view to the given value.
 *
 * @author AmirHossein Aghajari
 */
abstract class RulePositionBase<T> extends RuleWithTmpData<T, int[]> {

    protected final boolean lockedWidth, lockedHeight;
    protected final int gravity;

    public RulePositionBase(int gravity, boolean lockedWidth, boolean lockedHeight, T data) {
        super(data);
        this.lockedWidth = lockedWidth;
        this.lockedHeight = lockedHeight;
        this.gravity = gravity;
    }

    protected abstract int calculate(View view, LayoutSize target, LayoutSize original, LayoutSize parentSize);

    @SuppressWarnings("ConstantConditions")
    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        final int w = original.getWidth();
        final int h = original.getHeight();

        if (!isReverse() || tmpData == null) {
            int start;
            switch (gravity) {
                case Gravity.LEFT:
                    start = original.left;
                    break;
                case Gravity.RIGHT:
                    start = original.right;
                    break;
                case Gravity.TOP:
                    start = original.top;
                    break;
                case Gravity.BOTTOM:
                    start = original.bottom;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    start = original.getCenterX();
                    break;
                case Gravity.CENTER_VERTICAL:
                    start = original.getCenterY();
                    break;
                default:
                    throw new IllegalStateException("Unexpected gravity: " + gravity);
            }

            int tmp = calculate(view, target, original, parentSize);
            tmpData = new int[]{start, tmp};
        }

        ValueAnimator animator = ValueAnimator.ofInt(tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                switch (gravity) {
                    case Gravity.LEFT:
                        target.left = (int) valueAnimator.getAnimatedValue();
                        if (lockedWidth)
                            target.right = target.left + w;
                        break;
                    case Gravity.RIGHT:
                        target.right = (int) valueAnimator.getAnimatedValue();
                        if (lockedWidth)
                            target.left = target.right - w;
                        break;
                    case Gravity.TOP:
                        target.top = (int) valueAnimator.getAnimatedValue();
                        if (lockedHeight)
                            target.bottom = target.top + h;
                        break;
                    case Gravity.BOTTOM:
                        target.bottom = (int) valueAnimator.getAnimatedValue();
                        if (lockedHeight)
                            target.top = target.bottom - h;
                        break;
                    case Gravity.CENTER_HORIZONTAL:
                        target.left = ((int) valueAnimator.getAnimatedValue()) - (w / 2);
                        if (lockedWidth)
                            target.right = target.left + w;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        target.top = ((int) valueAnimator.getAnimatedValue()) - (h / 2);
                        if (lockedHeight)
                            target.bottom = target.top + h;
                        break;
                }
                update(view, target);
            }
        });
        return initEvaluator(animator);
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
            InspectUtils.inspect(view, view, target, gravity, true);
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
        }
    }
}
