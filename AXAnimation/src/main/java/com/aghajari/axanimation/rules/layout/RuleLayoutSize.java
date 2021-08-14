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
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link Rule} to change View's Layout using {@link LayoutSizeEvaluator}
 * Supports {@link LiveSize}
 *
 * @author AmirHossein Aghajari
 * @see View#setLayoutParams(ViewGroup.LayoutParams)
 * @see LiveSize
 */
public class RuleLayoutSize extends RuleWithTmpData<LayoutSize[], Object[]> implements LiveSizeDebugger {

    private final boolean horizontal;
    private final boolean vertical;

    private final RuleLiveSize.LiveSizeHandler handler = new RuleLiveSize.LiveSizeHandler();

    public RuleLayoutSize(LayoutSize... data) {
        this(true, true, data);
    }

    public RuleLayoutSize(boolean horizontal, boolean vertical, LayoutSize... data) {
        super(data);
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public long shouldWait() {
        return handler.shouldWait();
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);

        ArrayList<LiveSize> allLiveSizes = new ArrayList<>();
        for (LayoutSize ls : data) {
            List<LiveSize> nl = getLiveSizeValues(ls);
            if (!nl.isEmpty())
                allLiveSizes.addAll(nl);
        }

        handler.getReady(view, allLiveSizes);
    }

    private List<LiveSize> getLiveSizeValues(LayoutSize l) {
        ArrayList<LiveSize> liveSizes = new ArrayList<>();
        if (l.liveLeft != null) liveSizes.add(l.liveLeft);
        if (l.liveRight != null) liveSizes.add(l.liveRight);
        if (l.liveBottom != null) liveSizes.add(l.liveBottom);
        if (l.liveTop != null) liveSizes.add(l.liveTop);
        return liveSizes;
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return LayoutSizeEvaluator.class;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];
            // do not use System.arraycopy here
            for (int i = 0; i < data.length; i++)
                tmpData[i + 1] = new LayoutSize(data[i]);
            tmpData[0] = target.clone();

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();

            for (Object o : tmpData) {
                LayoutSize size = (LayoutSize) o;
                size.prepare(vw, vh, parentSize, target, original);
            }
        }

        ValueAnimator animator = ValueAnimator.ofObject(createEvaluator(), tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (horizontal && vertical) {
                    target.setOnlyTarget((LayoutSize) valueAnimator.getAnimatedValue());
                } else if (horizontal) {
                    target.setHorizontal((LayoutSize) valueAnimator.getAnimatedValue());
                } else {
                    target.setVertical((LayoutSize) valueAnimator.getAnimatedValue());
                }
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
            handler.debug(view, target, original, parentSize, Gravity.NO_GRAVITY);
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
            InspectUtils.inspect(view, view, target, Gravity.TOP, false);
            InspectUtils.inspect(view, view, target, Gravity.LEFT, false);
            InspectUtils.inspect(view, view, target, Gravity.RIGHT, false);
            InspectUtils.inspect(view, view, target, Gravity.BOTTOM, false);
        }
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(view, data);
    }
}
