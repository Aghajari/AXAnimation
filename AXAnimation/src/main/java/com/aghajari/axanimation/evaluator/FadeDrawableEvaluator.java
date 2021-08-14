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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;


/**
 * This evaluator can be used to perform type interpolation between <code>Drawable</code> values.
 *
 * @author AmirHossein Aghajari
 */
public class FadeDrawableEvaluator implements TypeEvaluator<Drawable> {

    private Drawable[] drawables;

    private static final int START_ALPHA = 0;
    private static final int END_ALPHA = 1;
    private int[] savedValues;

    private final int mWidth;
    private final int mHeight;
    private int mTargetWidth;
    private int mTargetHeight;


    public FadeDrawableEvaluator() {
        this.mWidth = 100;
        this.mHeight = 100;
        mTargetWidth = mWidth;
        mTargetHeight = mHeight;
    }

    void init(Drawable startValue, Drawable endValue) {
        if (drawables == null || savedValues == null) {
            drawables = new Drawable[2];
            savedValues = new int[2];
        }

        drawables[0] = startValue;
        drawables[1] = endValue;
        savedValues[START_ALPHA] = startValue.getAlpha();
        savedValues[END_ALPHA] = endValue.getAlpha();

        Rect b1 = startValue.getBounds(), b2 = endValue.getBounds();
        Rect bounds;
        if (b1.width() > b2.width())
            bounds = b1;
        else
            bounds = b2;

        if (bounds.width() == 0 || bounds.height() == 0) {
            mTargetWidth = mWidth;
            mTargetHeight = mHeight;

            Rect r = new Rect(bounds);
            r.right = r.left + mTargetWidth;
            r.bottom = r.top + mTargetHeight;
            startValue.setBounds(r);
            endValue.setBounds(r);
        } else {
            mTargetWidth = bounds.width();
            mTargetHeight = bounds.height();
            startValue.setBounds(bounds);
            endValue.setBounds(bounds);
        }
    }

    @NonNull
    @Override
    public Drawable evaluate(float fraction, @NonNull Drawable startValue, @NonNull Drawable endValue) {
        boolean shouldCopy;
        if (drawables == null || savedValues == null) {
            drawables = new Drawable[2];
            savedValues = new int[2];
            shouldCopy = true;
        } else {
            shouldCopy = drawables[0] != startValue || drawables[1] != endValue;
        }

        if (shouldCopy)
            init(startValue, endValue);

        if (fraction >= 1) {
            endValue.setAlpha(savedValues[END_ALPHA]);
            return endValue;
        }

        int startAlpha = evaluate(fraction, savedValues[START_ALPHA], 0);
        int endAlpha = evaluate(fraction, 0, savedValues[END_ALPHA]);

        startValue.setAlpha(startAlpha);
        endValue.setAlpha(endAlpha);

        Bitmap targetBitmap = Bitmap.createBitmap(mTargetWidth, mTargetHeight, Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBitmap);
        startValue.draw(targetCanvas);
        endValue.draw(targetCanvas);

        return new BitmapDrawable(null, targetBitmap);
    }

    private int evaluate(float fraction, int startValue, int endValue) {
        return (int) (startValue + fraction * (endValue - startValue));
    }
}