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

import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * This evaluator can be used to perform type interpolation between <code>LayoutSize</code> values.
 */
public class LayoutSizeEvaluator implements TypeEvaluator<LayoutSize> {

    /**
     * When null, a new Rect is returned on every evaluate call. When non-null,
     * mRect will be modified and returned on every evaluate.
     */
    private final LayoutSize mSize;

    /**
     * Construct a RectEvaluator that returns a new Rect on every evaluate call.
     * To avoid creating an object for each evaluate call,
     * {@link LayoutSizeEvaluator#LayoutSizeEvaluator(LayoutSize)} should be used
     * whenever possible.
     */
    public LayoutSizeEvaluator() {
        mSize = new LayoutSize();
    }

    /**
     * Constructs a RectEvaluator that modifies and returns <code>reuseRect</code>
     * in {@link #evaluate(float, LayoutSize, LayoutSize)} calls.
     * The value returned from
     * {@link #evaluate(float, LayoutSize, LayoutSize)} should
     * not be cached because it will change over time as the object is reused on each
     * call.
     *
     * @param reuseSize A Rect to be modified and returned by evaluate.
     */
    public LayoutSizeEvaluator(LayoutSize reuseSize) {
        mSize = reuseSize;
    }

    /**
     * This function returns the result of linearly interpolating the start and
     * end Rect values, with <code>fraction</code> representing the proportion
     * between the start and end values. The calculation is a simple parametric
     * calculation on each of the separate components in the Rect objects
     * (left, top, right, and bottom).
     *
     * <p>If {@link #LayoutSizeEvaluator(LayoutSize)} was used to construct
     * this RectEvaluator, the object returned will be the <code>reuseRect</code>
     * passed into the constructor.</p>
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue The start LayoutSize
     * @param endValue   The end LayoutSize
     * @return A linear interpolation between the start and end values, given the
     * <code>fraction</code> parameter.
     */
    @Override
    public LayoutSize evaluate(float fraction, LayoutSize startValue, LayoutSize endValue) {
        int left = startValue.left + (int) ((endValue.left - startValue.left) * fraction);
        int top = startValue.top + (int) ((endValue.top - startValue.top) * fraction);
        int right = startValue.right + (int) ((endValue.right - startValue.right) * fraction);
        int bottom = startValue.bottom + (int) ((endValue.bottom - startValue.bottom) * fraction);
        if (mSize == null) {
            return new LayoutSize(left, top, right, bottom);
        } else {
            mSize.set(left, top, right, bottom);
            return mSize;
        }
    }
}
