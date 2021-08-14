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
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.evaluator.RectEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.SizeUtils;

/**
 * A {@link Rule} to set View's padding
 *
 * @see View#setPadding(int, int, int, int)
 * @author AmirHossein Aghajari
 */
public class RulePadding extends RuleWithTmpData<Rect[], Object[]> {

    public RulePadding(Rect... data) {
        super(data);
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return RectEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];
            // do not use System.arraycopy here
            // get a copy of data elements, might you need to use the originals later.
            for (int i = 0; i < data.length; i++)
                tmpData[i + 1] = new Rect(data[i]);
            tmpData[0] = target.getRect();

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();

            for (Object o : tmpData) {
                Rect size = (Rect) o;
                size.left = SizeUtils.calculate(size.left, vw, vh, parentSize, target, original, Gravity.LEFT);
                size.top = SizeUtils.calculate(size.top, vw, vh, parentSize, target, original, Gravity.TOP);
                size.right = SizeUtils.calculate(size.right, vw, vh, parentSize, target, original, Gravity.RIGHT);
                size.bottom = SizeUtils.calculate(size.bottom, vw, vh, parentSize, target, original, Gravity.BOTTOM);
            }
        }

        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Rect rect = (Rect) valueAnimator.getAnimatedValue();
                view.setPadding(rect.left, rect.top, rect.right, rect.bottom);
                update(view, target);
            }
        });
        return animator;
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

}
