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
package com.aghajari.axanimation.evaluator;

import android.animation.TypeEvaluator;
import android.content.res.ColorStateList;

import com.aghajari.axanimation.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This evaluator can be used to perform type interpolation between <code>ColorStateList</code> values.
 *
 * @author AmirHossein Aghajari
 */
public class ColorStateListEvaluator implements TypeEvaluator<ColorStateList> {
    private final ArgbEvaluator argbEvaluator = ArgbEvaluator.getInstance();
    private int[] startColors;
    private int[] endColors;
    private int[] realEndColors;
    private ColorStateList end_csl = null;
    private Field mColorsField;
    private Method onColorsChangedMethod;

    protected ColorStateList[] csl = null;

    @Override
    public ColorStateList evaluate(float fraction, ColorStateList startValue, ColorStateList endValue) {
        if (fraction == 0)
            return startValue;

        boolean shouldCopy;
        if (csl == null) {
            csl = new ColorStateList[2];
            shouldCopy = true;
        } else {
            shouldCopy = false;
        }

        if (shouldCopy) {
            csl[0] = startValue;
            csl[1] = endValue;

            ColorStateList start_csl = startValue;
            end_csl = endValue;

            mColorsField = ReflectionUtils.getPrivateField(start_csl, "mColors");
            onColorsChangedMethod = ReflectionUtils.getPrivateMethod(end_csl, "onColorsChanged");

            Object sColors = ReflectionUtils.getPrivateFieldValue(mColorsField, start_csl);
            if (sColors != null)
                startColors = (int[]) sColors;
            if (startColors == null)
                startColors = new int[]{start_csl.getDefaultColor()};

            Object eColors = ReflectionUtils.getPrivateFieldValue(mColorsField, end_csl);
            if (eColors != null)
                endColors = (int[]) eColors;

            if (endColors != null) {
                realEndColors = endColors;
                endColors = endColors.clone();
            } else {
                realEndColors = null;
            }

            if (endColors == null)
                endColors = new int[]{end_csl.getDefaultColor()};

            int length = Math.max(startColors.length, endColors.length);
            int lastColor;

            if (startColors.length != length) {
                int[] tmpColors = new int[length];
                lastColor = start_csl.getDefaultColor();
                for (int i = 0; i < tmpColors.length; i++) {
                    if (startColors.length > i) {
                        lastColor = startColors[i];
                        tmpColors[i] = startColors[i];
                    } else {
                        tmpColors[i] = lastColor;
                    }
                }
                startColors = tmpColors;
            }

            if (endColors.length != length) {
                int[] tmpColors = new int[length];
                lastColor = end_csl.getDefaultColor();
                for (int i = 0; i < tmpColors.length; i++) {
                    if (endColors.length > i) {
                        lastColor = endColors[i];
                        tmpColors[i] = endColors[i];
                    } else {
                        tmpColors[i] = lastColor;
                    }
                }
                endColors = tmpColors;
            }
        }

        if (fraction == 1) {
            ReflectionUtils.setPrivateFieldValue(mColorsField, end_csl, realEndColors);
            ReflectionUtils.invokePrivateMethod(onColorsChangedMethod, end_csl);
            return endValue;
        }

        int[] targetColors = new int[startColors.length];
        for (int i = 0; i < targetColors.length; i++) {
            targetColors[i] = (Integer) argbEvaluator.evaluate(fraction, startColors[i], endColors[i]);
        }

        ReflectionUtils.setPrivateFieldValue(mColorsField, end_csl, targetColors);
        ReflectionUtils.invokePrivateMethod(onColorsChangedMethod, end_csl);

        return end_csl;
    }

    public static boolean equals(ColorStateList first, ColorStateList second) {
        if (first == second)
            return true;
        if (first == null || second == null)
            return false;

        Field mColors = ReflectionUtils.getPrivateField(first, "mColors");
        Object o1 = ReflectionUtils.getPrivateFieldValue(mColors, first);
        Object o2 = ReflectionUtils.getPrivateFieldValue(mColors, second);
        if (o1 != o2 && (o1 == null || o2 == null))
            return false;

        if (o1 != null) {
            if (!Arrays.equals((int[]) o1, (int[]) o2))
                return false;
        }

        Field mStateSpecs = ReflectionUtils.getPrivateField(first, "mStateSpecs");
        Object s1 = ReflectionUtils.getPrivateFieldValue(mStateSpecs, first);
        Object s2 = ReflectionUtils.getPrivateFieldValue(mStateSpecs, second);

        if (s1 != s2 && (s1 == null || s2 == null))
            return false;

        if (s1 != null) {
            int[][] sv1 = (int[][]) s1;
            int[][] sv2 = (int[][]) s2;

            int length = sv1.length;
            if (sv2.length != length)
                return false;

            for (int i = 0; i < length; i++) {
                if (!Arrays.equals(sv1[i], sv2[i]))
                    return false;
            }
        }
        return true;
    }
}
