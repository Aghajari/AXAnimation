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
package com.aghajari.axanimation.rules;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.listener.AXAnimatorUpdateListener;
import com.aghajari.axanimation.livevar.LiveVar;

/**
 * A custom {@link PropertyRule} using {@link android.animation.ValueAnimator}
 * {@link #getStartValue(View)} returns the first value of animator
 * So you can override it to return first view's property value in a subclass
 * Or automatically it will call the getter method of the property.
 *
 * @author AmirHossein Aghajari
 */
public class PropertyValueRule<T> extends PropertyRule<T> {

    private final AXAnimatorUpdateListener<T> listener;

    @SafeVarargs
    public PropertyValueRule(@Nullable AXAnimatorUpdateListener<T> listener, final String property, @Nullable T... data) {
        this(listener, property, null, data);
    }

    @SafeVarargs
    public PropertyValueRule(@Nullable AXAnimatorUpdateListener<T> listener, final String property, TypeEvaluator<?> evaluator, @Nullable T... data) {
        super(property, evaluator, data);
        this.listener = listener;
    }

    public PropertyValueRule(@Nullable AXAnimatorUpdateListener<T> listener, final String property, @NonNull LiveVar<T[]> data) {
        this(listener, property, null, data);
    }

    public PropertyValueRule(@Nullable AXAnimatorUpdateListener<T> listener, final String property, TypeEvaluator<?> evaluator, @NonNull LiveVar<T[]> data) {
        super(property, evaluator, data);
        this.listener = listener;
    }

    public void onAnimationUpdate(View view, ValueAnimator animator, T value) {
        if (listener != null)
            listener.onAnimationUpdate(view, animator, value);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull final View view, @Nullable LayoutSize target, @Nullable LayoutSize original, @Nullable LayoutSize parentSize) {
        Object values = getValuesWithTmpCheck(view);
        if (values == null)
            return null;

        TypeEvaluator<?> evaluator = createEvaluator();
        final ValueAnimator animator;
        if (Float.class.equals(type)) {
            animator = ValueAnimator.ofFloat((float[]) values);

            if (evaluator != null)
                animator.setEvaluator(evaluator);
        } else if (Integer.class.equals(type)) {
            animator = ValueAnimator.ofInt((int[]) values);

            if (evaluator != null)
                animator.setEvaluator(evaluator);
        } else {
            animator = ValueAnimator.ofObject(evaluator, (Object[]) values);
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //noinspection unchecked
                PropertyValueRule.this.onAnimationUpdate(view, animator, (T) animation.getAnimatedValue());
            }
        });
        return animator;
    }
}
