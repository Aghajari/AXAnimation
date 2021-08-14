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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.evaluator.LayoutSizeEvaluator;
import com.aghajari.axanimation.layouts.AnimatedLayout;
import com.aghajari.axanimation.layouts.AnimatedLayoutParams;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.layouts.OnLayoutSizeReadyListener;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

/**
 * A {@link Rule} to change View's Layout using {@link LayoutSizeEvaluator}
 *
 * @author AmirHossein Aghajari
 * @see View#setLayoutParams(ViewGroup.LayoutParams)
 */
public class RuleLayoutParams extends RuleWithTmpData<ViewGroup.LayoutParams, Object[]> {

    protected LayoutSize targetLayoutSize;

    public RuleLayoutParams(ViewGroup.LayoutParams data) {
        super(data);
        targetLayoutSize = null;
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);
        if (data == null) {
            targetLayoutSize = ((AnimatedLayoutParams) view.getLayoutParams()).originalLayout;
        } else {
            AnimatedLayout layout = (AnimatedLayout) view.getParent();
            layout.getLayoutSize(view, data, new OnLayoutSizeReadyListener() {
                @Override
                public void onReady(View view, LayoutSize size) {
                    targetLayoutSize = size;
                }
            });
        }
    }

    @Override
    public long shouldWait() {
        return targetLayoutSize != null ? super.shouldWait() : 0;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return LayoutSizeEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[2];
            tmpData[0] = target.clone();
            tmpData[1] = targetLayoutSize.clone();
        }

        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                target.set((LayoutSize) valueAnimator.getAnimatedValue());
                update(view, target);
            }
        });
        return animator;
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize) {
        super.debug(view, target, original, parentSize);
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            InspectUtils.inspect(view, view, target, Gravity.FILL, false);
            InspectUtils.inspect(view, view, target, Gravity.TOP, false);
            InspectUtils.inspect(view, view, target, Gravity.LEFT, false);
            InspectUtils.inspect(view, view, target, Gravity.RIGHT, false);
            InspectUtils.inspect(view, view, target, Gravity.BOTTOM, false);
        }
    }

}

/*
 * it was an stupid idea to use 4 position rules to resize the view!
 * but that stupid idea forced me to create RuleSet! Yeh this one is useful later.
 * anyway, RuleLayoutParams has been updated and now it works just like RuleLayoutSize.
 * I'll keep this commented code for a sample of RuleSet.
 */

/*public class RuleLayoutParams extends RuleSet<ViewGroup.LayoutParams> {

    private LayoutSize targetLayoutSize;

    public RuleLayoutParams(ViewGroup.LayoutParams data) {
        super(data);
        targetLayoutSize = null;
    }

    @Override
    public void getReady(View view) {
        super.getReady(view);
        if (data == null) {
            targetLayoutSize = ((AnimatedLayoutParams) view.getLayoutParams()).originalLayout;
        } else {
            AnimatedLayout layout = (AnimatedLayout) view.getParent();
            layout.getLayoutSize(view, data, new OnLayoutSizeReadyListener() {
                @Override
                public void onReady(View view, LayoutSize size) {
                    targetLayoutSize = size;
                }
            });
        }
    }

    @Override
    public long shouldWait() {
        return targetLayoutSize != null ? super.shouldWait() : 0;
    }

    @Override
    public Rule<?>[] createRules() {
        return new Rule[]{
                new RulePosition(Gravity.LEFT, false, false, targetLayoutSize.l),
                new RulePosition(Gravity.TOP, false, false, targetLayoutSize.t),
                new RulePosition(Gravity.RIGHT, false, false, targetLayoutSize.r),
                new RulePosition(Gravity.BOTTOM, false, false, targetLayoutSize.b)
        };
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

}*/
