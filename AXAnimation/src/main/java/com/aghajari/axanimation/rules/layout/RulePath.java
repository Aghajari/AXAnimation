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
package com.aghajari.axanimation.rules.layout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Rule;

/**
 * A {@link Rule} to move view on the given Path
 *
 * @author AmirHossein Aghajari
 */
public class RulePath extends Rule<Path> {

    final boolean lockedX,lockedY;

    public RulePath(Path data,boolean lockedX, boolean lockedY) {
        super(data);
        this.lockedX = lockedX;
        this.lockedY = lockedY;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        final PathMeasure pathMeasure = new PathMeasure(data, false);
        final int w = original.getWidth();
        final int h = original.getHeight();

        ValueAnimator pathAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            final float[] point = new float[2];

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = animation.getAnimatedFraction();
                pathMeasure.getPosTan(pathMeasure.getLength() * val, point, null);
                target.left = (int) (point[0] + (target.getWidth()/2));
                if (lockedX)
                    target.right = target.left + w;
                target.top = (int) (point[1] + (target.getHeight()/2));
                if (lockedY)
                    target.bottom = target.top + h;
                update(view, target);
            }
        });
        return initEvaluator(pathAnimator);
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }
}
