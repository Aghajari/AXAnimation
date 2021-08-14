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
package com.aghajari.axanimation.rules.transformation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.listener.AXAnimatorUpdateListener;
import com.aghajari.axanimation.evaluator.MatrixEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;


/**
 * A {@link Rule} which works with {@link Matrix} and {@link MatrixEvaluator}
 *
 * @author AmirHossein Aghajari
 * @see View#getMatrix()
 */
public class RuleMatrix extends RuleWithTmpData<Object[], Matrix> {

    private final AXAnimatorUpdateListener<Matrix> listener;

    public RuleMatrix(AXAnimatorUpdateListener<Matrix> listener, Matrix... matrices) {
        super(matrices);
        this.listener = listener;
    }

    public Matrix getStartMatrix(View view) {
        if (!isReverse() || tmpData == null)
            tmpData = view.getMatrix();

        return tmpData;
    }

    public void apply(View view, Matrix matrix) {
        ApplyMatrix m = new ApplyMatrix(matrix);
        view.startAnimation(m);
    }

    public Object[] getMatrices(View view) {
        boolean getStartValue = animatorValues != null && animatorValues.isFirstValueFromView();
        Matrix startValue = getStartValue ? getStartMatrix(view) : null;

        if (!getStartValue && data != null) {
            if (data.length <= 1) {
                startValue = getStartMatrix(view);
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
        return MatrixEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), getMatrices(view));

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (listener != null) {
                    listener.onAnimationUpdate(view, valueAnimator, (Matrix) valueAnimator.getAnimatedValue());
                } else {
                    apply(view, (Matrix) valueAnimator.getAnimatedValue());
                }
            }
        });
        return animator;
    }

    private static class ApplyMatrix extends Animation {
        private final Matrix matrix;

        public ApplyMatrix(Matrix matrix) {
            this.matrix = matrix;
            setDuration(0);
            setFillAfter(true);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            t.getMatrix().set(matrix);
        }
    }

}