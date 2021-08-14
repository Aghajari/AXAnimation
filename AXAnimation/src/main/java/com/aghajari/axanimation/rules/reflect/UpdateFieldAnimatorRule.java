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
package com.aghajari.axanimation.rules.reflect;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.listener.AXAnimatorUpdateListener;
import com.aghajari.axanimation.rules.Debugger;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom {@link Rule} using {@link ValueAnimator} to update a {@link java.lang.reflect.Field} of View
 *
 * @author AmirHossein Aghajari
 */
public class UpdateFieldAnimatorRule<T> extends RuleWithTmpData<Object[], Object[]> implements Debugger {

    private final String fieldName;
    private final AXAnimatorUpdateListener<T> listener;
    private final TypeEvaluator<T> evaluator;
    private final boolean invalidate;

    @SafeVarargs
    public UpdateFieldAnimatorRule(String fieldName,
                                   AXAnimatorUpdateListener<T> listener,
                                   TypeEvaluator<T> evaluator,
                                   boolean invalidate,
                                   T... data) {
        super(data);
        this.fieldName = fieldName;
        this.listener = listener;
        this.evaluator = evaluator;
        this.invalidate = invalidate;
    }

    @Override
    public Map<String, String> debugValues(@NonNull View view) {
        Map<String, String> map = new HashMap<>();
        map.put("FieldName", fieldName);
        return map;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return evaluator.getClass();
    }

    @Override
    public TypeEvaluator<?> createEvaluator() {
        return evaluator;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            if (animatorValues == null || animatorValues.isFirstValueFromView()) {
                tmpData = new Object[data.length];
                System.arraycopy(data, 0, tmpData, 0, data.length);
                try {
                    tmpData[0] = getFieldValue(view, fieldName);
                } catch (Exception e) {
                    Log.e("AXAnimation", "UpdateFieldAnimatorRule (" + fieldName + ")", e);
                    return null;
                }
            } else {
                tmpData = data;
            }
        }

        final ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                try {
                    setFieldValue(view, fieldName, valueAnimator.getAnimatedValue());
                    if (invalidate)
                        view.invalidate();
                } catch (Exception e) {
                    Log.e("AXAnimation", "UpdateFieldAnimatorRule (" + fieldName + ")", e);
                }
                if (listener != null) {
                    //noinspection unchecked
                    listener.onAnimationUpdate(view, valueAnimator, (T) valueAnimator.getAnimatedValue());
                }
            }
        });
        return animator;
    }

    protected void setFieldValue(Object obj, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(), fieldName);
        if (getAccessible())
            field.setAccessible(true);
        field.set(obj, value);
    }

    protected Object getFieldValue(Object obj, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(), fieldName);
        if (getAccessible())
            field.setAccessible(true);
        return field.get(obj);
    }

    protected boolean getAccessible() {
        return false;
    }

    protected Field getField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return cls.getField(fieldName);
    }
}
