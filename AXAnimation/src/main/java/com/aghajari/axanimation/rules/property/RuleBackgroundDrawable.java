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
package com.aghajari.axanimation.rules.property;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.evaluator.DrawableEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;

/**
 * A {@link Rule} to change View's background using {@link DrawableEvaluator}
 * Supports {@link ColorDrawable} and {@link GradientDrawable}
 * Other types of drawable will animate by {@link com.aghajari.axanimation.evaluator.FadeDrawableEvaluator}
 *
 * @author AmirHossein Aghajari
 */
public class RuleBackgroundDrawable extends RuleWithTmpData<Object[], Drawable> {

    public RuleBackgroundDrawable(Drawable... drawables) {
        super(drawables);
    }

    public Drawable getStartDrawable(View view) {
        if (!isReverse() || tmpData == null)
            tmpData = view.getBackground();

        if (tmpData != null &&
                (tmpData instanceof ColorDrawable || tmpData instanceof GradientDrawable)) {
        } else {
            tmpData = new ColorDrawable(Color.TRANSPARENT);
            tmpData.setBounds(0,0, view.getMeasuredWidth(),view.getMeasuredHeight());
        }

        return tmpData;
    }

    public Object[] getDrawables(View view) {
        boolean getStartValue = animatorValues != null && animatorValues.isFirstValueFromView();
        Drawable startValue = getStartValue ? getStartDrawable(view) : null;

        if (!getStartValue && data != null) {
            if (data.length <= 1) {
                startValue = getStartDrawable(view);
            }
        }

        if (startValue == null) {
            return data;
        } else {
            Object[] tmpData = new Object[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);
            tmpData[0] = startValue;
            return tmpData;
        }
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return DrawableEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), getDrawables(view));

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setBackground((Drawable) valueAnimator.getAnimatedValue());
            }
        });
        return animator;
    }


}