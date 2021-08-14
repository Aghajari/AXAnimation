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
package com.aghajari.axanimation.draw.rules;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.evaluator.MatrixEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;


/**
 * A {@link DrawRule} to set {@link Canvas} transformation matrix.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#setMatrix(Matrix)
 */
public class MatrixRule extends DrawRule<Matrix[], Object[], Matrix> {

    private Matrix startMatrix;

    /**
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be called before calling
     *                    the super.dispatchDraw(Canvas).
     * @param values      The matrix to replace the canvas matrix with. (AnimatorValues)
     */
    public MatrixRule(String key, boolean drawOnFront, Matrix... values) {
        super(null, key, drawOnFront, values);
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);
        if (isReverse() && tmpData != null)
            return;

        startMatrix = null;
        animatedValue = null;

        if (animatorValues == null || animatorValues.isFirstValueFromView())
            addToSet(view, true);
    }

    @Override
    public long shouldWait() {
        if (isReverse() && tmpData != null)
            return super.shouldWait();

        if (animatorValues != null && !animatorValues.isFirstValueFromView())
            return super.shouldWait();

        return startMatrix != null ? super.shouldWait() : 0;
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (startMatrix == null) {
            startMatrix = new Matrix();
            canvas.getMatrix(startMatrix);
        }

        if (animatedValue != null) {
            canvas.concat(animatedValue);
        }
    }

    public Matrix getMatrix() {
        return animatedValue;
    }

    public Matrix getStartMatrix() {
        return startMatrix;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return MatrixEvaluator.class;
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);

            if (startMatrix == null)
                startMatrix = new Matrix();

            tmpData[0] = startMatrix;

            for (int i = 0; i < tmpData.length; i++) {
                if (tmpData[i] == null)
                    tmpData[i] = startMatrix;
            }
        }

        return ValueAnimator.ofObject(createEvaluator(), tmpData);
    }
}