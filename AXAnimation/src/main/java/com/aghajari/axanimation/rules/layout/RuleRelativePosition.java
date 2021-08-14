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

import com.aghajari.axanimation.layouts.AnimatedLayout;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.layouts.OnLayoutSizeReadyListener;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

/**
 * A {@link Rule} to move axis of view to the given value.
 *
 * @author AmirHossein Aghajari
 */
public class RuleRelativePosition extends RuleWithTmpData<Integer, int[]> {

    final boolean lockedWidth, lockedHeight;
    final int gravitySource, gravityTarget;
    final int viewId;
    View relatedView;
    LayoutSize relatedLayout;

    public RuleRelativePosition(int gravitySource, int gravityTarget, View relatedView, boolean lockedWidth, boolean lockedHeight, int delta) {
        super(delta);
        this.viewId = -1;
        this.relatedView = relatedView;
        this.lockedWidth = lockedWidth;
        this.lockedHeight = lockedHeight;

        if (gravityTarget == Gravity.CENTER) {
            if (Gravity.isHorizontal(gravitySource))
                this.gravityTarget = Gravity.CENTER_HORIZONTAL;
            else
                this.gravityTarget = Gravity.CENTER_VERTICAL;
        } else {
            this.gravityTarget = gravityTarget;
        }

        if (gravitySource == Gravity.CENTER) {
            if (Gravity.isHorizontal(gravityTarget))
                this.gravitySource = Gravity.CENTER_HORIZONTAL;
            else
                this.gravitySource = Gravity.CENTER_VERTICAL;
        } else {
            this.gravitySource = gravitySource;
        }
    }

    public RuleRelativePosition(int gravitySource, int gravityTarget, int relatedView, boolean lockedWidth, boolean lockedHeight, int delta) {
        super(delta);
        this.viewId = relatedView;
        this.relatedView = null;
        this.lockedWidth = lockedWidth;
        this.lockedHeight = lockedHeight;
        this.gravitySource = gravitySource;
        this.gravityTarget = gravityTarget;
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
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (relatedView == null && viewId == -1 && relatedLayout == null)
            relatedLayout = parentSize;

        final int w = original.getWidth();
        final int h = original.getHeight();

        if (!isReverse() || tmpData == null) {
            int start, end;
            switch (gravitySource) {
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
                    start = original.left + w / 2;
                    break;
                case Gravity.CENTER_VERTICAL:
                    start = original.top + h / 2;
                    break;
                default:
                    throw new IllegalStateException("Unexpected gravity: " + gravitySource);
            }

            switch (gravityTarget) {
                case Gravity.LEFT:
                    end = relatedLayout.left;
                    break;
                case Gravity.RIGHT:
                    end = relatedLayout.right;
                    break;
                case Gravity.TOP:
                    end = relatedLayout.top;
                    break;
                case Gravity.BOTTOM:
                    end = relatedLayout.bottom;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    end = relatedLayout.left + relatedLayout.getWidth() / 2;
                    break;
                case Gravity.CENTER_VERTICAL:
                    end = relatedLayout.top + relatedLayout.getHeight() / 2;
                    break;
                default:
                    throw new IllegalStateException("Unexpected gravity: " + gravityTarget);
            }
            end += data;

            tmpData = new int[]{start, end};
        }

        ValueAnimator animator = ValueAnimator.ofInt(tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                switch (gravitySource) {
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
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
            InspectUtils.inspect(view, view, target, gravitySource, false);
            InspectUtils.inspect(view, relatedView, relatedLayout, gravityTarget, false);

            /*InspectUtils.inspect(view, view, target, gravitySource, true);
            if (relatedView != null) {
                InspectUtils.inspect(view, relatedView, relatedLayout, gravityTarget, true);
            }*/

            if (tmpData != null) {
                Point end;
                boolean h = Gravity.isHorizontal(gravityTarget);
                if (h) {
                    end = new Point(tmpData[1] - data, -1);
                } else {
                    end = new Point(-1, tmpData[1] - data);
                }
                InspectUtils.inspect(view, view, target, gravitySource, end, isReverse(), h);
            }
        }
    }
}
