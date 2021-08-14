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
import com.aghajari.axanimation.utils.SizeUtils;

/**
 * A {@link Rule} to change View's Width
 *
 * @author AmirHossein Aghajari
 */
public class RuleResizeWidth extends RuleWithTmpData<int[], int[]> {

    private final int gravity;

    public RuleResizeWidth(int gravity, int[] data) {
        super(data);
        this.gravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new int[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);
            tmpData[0] = original.getWidth();

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();

            for (int i = 0; i < tmpData.length; i++) {
                tmpData[i] = SizeUtils.calculate(tmpData[i], vw, vh, parentSize, target, original, Gravity.FILL_HORIZONTAL);
            }
        }

        ValueAnimator animator = ValueAnimator.ofInt(tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                update(target, (Integer) valueAnimator.getAnimatedValue());
                update(view, target);
            }
        });
        return initEvaluator(animator);
    }

    private void update(LayoutSize target, int width) {
        switch (gravity) {
            case Gravity.RIGHT:
                target.right = target.left + width;
                break;
            case Gravity.CENTER_HORIZONTAL:
                int center = target.getCenterX();
                target.left = center - width / 2;
                target.right = center + width / 2;
                break;
            case Gravity.LEFT:
            default:
                target.left = target.right - width;
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
        if (animatorValues != null && animatorValues.isInspectEnabled())
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
    }

}
