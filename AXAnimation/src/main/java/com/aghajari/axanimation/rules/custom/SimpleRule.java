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
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.listener.AXAnimatorUpdateListener;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.Rule;

/**
 * A custom {@link Rule} using {@link ValueAnimator}
 *
 * @author AmirHossein Aghajari
 */
public class SimpleRule<T> extends Rule<Object[]> {

    private final TypeEvaluator<T> evaluator;
    private final AXAnimatorUpdateListener<T> listener;

    @SafeVarargs
    public SimpleRule(TypeEvaluator<T> evaluator, AXAnimatorUpdateListener<T> listener, T... data) {
        super(data);
        this.evaluator = evaluator;
        this.listener = listener;
    }

    public SimpleRule(TypeEvaluator<T> evaluator, AXAnimatorUpdateListener<T> listener, LiveVar<Object[]> data) {
        super(data);
        this.evaluator = evaluator;
        this.listener = listener;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return evaluator.getClass();
    }

    @Override
    public TypeEvaluator<?> createEvaluator() {
        return evaluator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        final ValueAnimator animator = ValueAnimator.ofObject(evaluator, data);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                listener.onAnimationUpdate(view, animator, (T) valueAnimator.getAnimatedValue());
            }
        });
        return animator;
    }

}
