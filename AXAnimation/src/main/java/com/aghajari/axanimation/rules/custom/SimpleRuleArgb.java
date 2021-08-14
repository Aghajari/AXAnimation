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
package com.aghajari.axanimation.rules.custom;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.listener.AXAnimatorUpdateListener;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.Rule;

/**
 * A custom {@link Rule} using {@link ValueAnimator} and {@link ArgbEvaluator}
 *
 * @author AmirHossein Aghajari
 */
public class SimpleRuleArgb extends Rule<int[]> {

    private final AXAnimatorUpdateListener<Integer> listener;

    public SimpleRuleArgb(AXAnimatorUpdateListener<Integer> listener, int... data) {
        super(data);
        this.listener = listener;
    }

    public SimpleRuleArgb(AXAnimatorUpdateListener<Integer> listener, LiveVar<int[]> data) {
        super(data);
        this.listener = listener;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return ArgbEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        final ValueAnimator animator = ValueAnimator.ofInt(data);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                listener.onAnimationUpdate(view, animator, (int) valueAnimator.getAnimatedValue());
            }
        });
        return initEvaluator(animator);
    }

}
