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
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.evaluator.FloatArrayEvaluator;
import com.aghajari.axanimation.evaluator.IntArrayEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveVar;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * A custom {@link Rule} using {@link ObjectAnimator}
 * {@link #getStartValue(View)} returns the first value of animator
 * So you can override it to return first view's property value in a subclass
 * Or automatically it will call the getter method of the property.
 *
 * @author AmirHossein Aghajari
 */
public class PropertyRule<T> extends RuleWithTmpData<T[], Object> {

    private final String property;
    protected Class<?> type;
    private final TypeEvaluator<?> evaluator;
    private Method getter = null;

    @SafeVarargs
    public PropertyRule(final String property, @Nullable T... data) {
        this(property, null, data);
    }

    public PropertyRule(final String property, @NonNull LiveVar<T[]> data) {
        this(property, null, data);
    }

    @SafeVarargs
    public PropertyRule(final String property, TypeEvaluator<?> evaluator, @Nullable T... data) {
        super(data);
        this.property = property;
        this.evaluator = evaluator;
        if (data != null)
            this.type = data.getClass().getComponentType();
    }

    public PropertyRule(final String property, TypeEvaluator<?> evaluator, @NonNull LiveVar<T[]> data) {
        super(data);
        this.property = property;
        this.evaluator = evaluator;
        if (this.data != null)
            this.type = this.data.getClass().getComponentType();
    }

    public Object getStartValue(View view) {
        if (getter == null) {
            try {
                getter = view.getClass().getMethod(getMethodName("get", getProperty()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        try {
            return getter.invoke(view);
        } catch (Exception e) {
            Log.e("AXAnimator", "PropertyRule", e);
        }
        return null;
    }

    protected String getProperty() {
        return property;
    }

    protected Object getValuesWithTmpCheck(View view) {
        if (!isReverse() || tmpData == null)
            tmpData = getValues(view);

        return tmpData;
    }

    protected Object getValues(View view) {
        boolean getStartValue = animatorValues != null && animatorValues.isFirstValueFromView();
        Object startValue = getStartValue ? getStartValue(view) : null;

        if (startValue == null && data == null)
            return null;

        if (type == null && startValue != null)
            type = startValue.getClass();

        int length = 0;
        if (data != null) {
            length += Array.getLength(data);
        }
        if (startValue != null) {
            if (startValue.getClass().equals(Array.newInstance(type, 0).getClass())) {
                length += Array.getLength(startValue);
            } else {
                length += 1;
            }
        }

        Object values;
        if (Float.class.equals(type)) {
            values = new float[length];
        } else if (Integer.class.equals(type)) {
            values = new int[length];
        } else {
            values = new Object[length];
        }

        if (length == 0)
            return values;

        int lengthStart = 0;
        if (startValue != null) {
            if (startValue.getClass().equals(Array.newInstance(type, 0).getClass())) {
                lengthStart = Array.getLength(startValue);
                arraycopy(startValue, 0, values, 0, lengthStart);
            } else {
                lengthStart = 1;
                Array.set(values, 0, startValue);
            }
        }

        if (data != null) {
            int lengthEnd = Array.getLength(data);
            arraycopy(data, 0, values, lengthStart, lengthEnd);
        }
        return values;
    }

    protected void arraycopy(Object src, int srcPos,
                             Object dest, int destPos,
                             int length) {
        if (src instanceof Float[] && dest instanceof float[]) {
            for (int i = srcPos; i < length; i++) {
                ((float[]) dest)[i + destPos] = ((Float[]) src)[i];
            }
        } else if (src instanceof Integer[] && dest instanceof int[]) {
            for (int i = srcPos; i < length; i++) {
                ((int[]) dest)[i + destPos] = ((Integer[]) src)[i];
            }
        } else {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
    }

    /**
     * Utility method to derive a setter/getter method name from a property name, where the
     * prefix is typically "set" or "get" and the first letter of the property name is
     * capitalized.
     *
     * @param prefix       The precursor to the method name, before the property name begins, typically
     *                     "set" or "get".
     * @param propertyName The name of the property that represents the bulk of the method name
     *                     after the prefix. The first letter of this word will be capitalized in the resulting
     *                     method name.
     * @return String the property name converted to a method name according to the conventions
     * specified above.
     */
    protected static String getMethodName(String prefix, String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            // shouldn't get here
            return prefix;
        }
        char firstLetter = Character.toUpperCase(propertyName.charAt(0));
        String theRest = propertyName.substring(1);
        return prefix + firstLetter + theRest;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        if (evaluator == null) {
            if (float[].class.equals(type)) {
                return FloatArrayEvaluator.class;
            } else if (int[].class.equals(type)) {
                return IntArrayEvaluator.class;
            }
            return null;
        }

        return evaluator.getClass();
    }

    @Override
    public TypeEvaluator<?> createEvaluator() {
        return evaluator;
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view, @Nullable LayoutSize target, @Nullable LayoutSize original, @Nullable LayoutSize parentSize) {
        Object values = getValuesWithTmpCheck(view);
        if (values == null)
            return null;

        TypeEvaluator<?> evaluator = createEvaluator();

        final ObjectAnimator animator;
        if (Float.class.equals(type)) {
            animator = ObjectAnimator.ofFloat(getTarget(view), getProperty(), (float[]) values);

            if (evaluator != null)
                animator.setEvaluator(evaluator);
        } else if (Integer.class.equals(type)) {
            animator = ObjectAnimator.ofInt(getTarget(view), getProperty(), (int[]) values);

            if (evaluator != null)
                animator.setEvaluator(evaluator);
        } else {
            animator = ObjectAnimator.ofObject(getTarget(view), getProperty(), evaluator, (Object[]) values);
        }

        if (shouldResetWhenDone()) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animator.setCurrentPlayTime(isReverse() ? animator.getDuration() : 0);
                }
            });
        }
        return animator;
    }

    protected Object getTarget(@NonNull View view) {
        return view;
    }

    protected boolean shouldResetWhenDone() {
        return false;
    }

    // static methods

    /**
     * @see View#setAlpha(float)
     */
    public static PropertyRule<Float> alpha(Float... data) {
        return new PropertyRule<>("alpha", data);
    }

    /**
     * @see View#setRotation(float)
     */
    public static PropertyRule<Float> rotation(Float... data) {
        return new PropertyRule<>("rotation", data);
    }

    /**
     * @see View#setRotationX(float)
     */
    public static PropertyRule<Float> rotationX(Float... data) {
        return new PropertyRule<>("rotationX", data);
    }

    /**
     * @see View#setRotationY(float)
     */
    public static PropertyRule<Float> rotationY(Float... data) {
        return new PropertyRule<>("rotationY", data);
    }

    /**
     * @see View#setScaleX(float)
     */
    public static PropertyRule<Float> scaleX(Float... data) {
        return new PropertyRule<>("scaleX", data);
    }

    /**
     * @see View#setScaleY(float)
     */
    public static PropertyRule<Float> scaleY(Float... data) {
        return new PropertyRule<>("scaleY", data);
    }

    /**
     * @see View#setCameraDistance(float)
     */
    public static PropertyRule<Float> cameraDistance(Float... data) {
        return new PropertyRule<>("cameraDistance", data);
    }

    /**
     * @see View#setPivotX(float)
     */
    public static PropertyRule<Float> pivotX(Float... data) {
        return new PropertyRule<>("pivotX", data);
    }

    /**
     * @see View#setPivotY(float)
     */
    public static PropertyRule<Float> pivotY(Float... data) {
        return new PropertyRule<>("pivotY", data);
    }

    /**
     * @see View#setTranslationX(float)
     */
    public static PropertyRule<Float> translationX(Float... data) {
        return new PropertyRule<>("translationX", data);
    }

    /**
     * @see View#setTranslationY(float)
     */
    public static PropertyRule<Float> translationY(Float... data) {
        return new PropertyRule<>("translationY", data);
    }

    /**
     * @see View#setTranslationZ(float)
     */
    public static PropertyRule<Float> translationZ(Float... data) {
        return new PropertyRule<>("translationZ", data);
    }

    /**
     * @see android.widget.TextView#setTextSize(float)
     */
    public static PropertyRule<Float> textSize(Float... data) {
        return new PropertyRule<>("textSize", data);
    }

    /**
     * @see View#setX(float)
     */
    public static PropertyRule<Float> x(Float... data) {
        return new PropertyRule<>("x", data);
    }

    /**
     * @see View#setY(float)
     */
    public static PropertyRule<Float> y(Float... data) {
        return new PropertyRule<>("y", data);
    }

    /**
     * @see View#setZ(float)
     */
    public static PropertyRule<Float> z(Float... data) {
        return new PropertyRule<>("z", data);
    }


    // LiveVar

    /**
     * @see View#setAlpha(float)
     */
    public static PropertyRule<Float> alpha(LiveVar<Float[]> data) {
        return new PropertyRule<>("alpha", data);
    }

    /**
     * @see View#setRotation(float)
     */
    public static PropertyRule<Float> rotation(LiveVar<Float[]> data) {
        return new PropertyRule<>("rotation", data);
    }

    /**
     * @see View#setRotationX(float)
     */
    public static PropertyRule<Float> rotationX(LiveVar<Float[]> data) {
        return new PropertyRule<>("rotationX", data);
    }

    /**
     * @see View#setRotationY(float)
     */
    public static PropertyRule<Float> rotationY(LiveVar<Float[]> data) {
        return new PropertyRule<>("rotationY", data);
    }

    /**
     * @see View#setScaleX(float)
     */
    public static PropertyRule<Float> scaleX(LiveVar<Float[]> data) {
        return new PropertyRule<>("scaleX", data);
    }

    /**
     * @see View#setScaleY(float)
     */
    public static PropertyRule<Float> scaleY(LiveVar<Float[]> data) {
        return new PropertyRule<>("scaleY", data);
    }

    /**
     * @see View#setCameraDistance(float)
     */
    public static PropertyRule<Float> cameraDistance(LiveVar<Float[]> data) {
        return new PropertyRule<>("cameraDistance", data);
    }

    /**
     * @see View#setPivotX(float)
     */
    public static PropertyRule<Float> pivotX(LiveVar<Float[]> data) {
        return new PropertyRule<>("pivotX", data);
    }

    /**
     * @see View#setPivotY(float)
     */
    public static PropertyRule<Float> pivotY(LiveVar<Float[]> data) {
        return new PropertyRule<>("pivotY", data);
    }

    /**
     * @see View#setTranslationX(float)
     */
    public static PropertyRule<Float> translationX(LiveVar<Float[]> data) {
        return new PropertyRule<>("translationX", data);
    }

    /**
     * @see View#setTranslationY(float)
     */
    public static PropertyRule<Float> translationY(LiveVar<Float[]> data) {
        return new PropertyRule<>("translationY", data);
    }

    /**
     * @see View#setTranslationZ(float)
     */
    public static PropertyRule<Float> translationZ(LiveVar<Float[]> data) {
        return new PropertyRule<>("translationZ", data);
    }

    /**
     * @see android.widget.TextView#setTextSize(float)
     */
    public static PropertyRule<Float> textSize(LiveVar<Float[]> data) {
        return new PropertyRule<>("textSize", data);
    }

    /**
     * @see View#setX(float)
     */
    public static PropertyRule<Float> x(LiveVar<Float[]> data) {
        return new PropertyRule<>("x", data);
    }

    /**
     * @see View#setY(float)
     */
    public static PropertyRule<Float> y(LiveVar<Float[]> data) {
        return new PropertyRule<>("y", data);
    }

    /**
     * @see View#setZ(float)
     */
    public static PropertyRule<Float> z(LiveVar<Float[]> data) {
        return new PropertyRule<>("z", data);
    }
}
