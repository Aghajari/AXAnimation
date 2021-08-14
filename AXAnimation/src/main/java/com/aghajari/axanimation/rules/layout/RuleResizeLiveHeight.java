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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.utils.InspectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link RuleLiveSize} to change View's height
 *
 * @author AmirHossein Aghajari
 * @see LiveSize
 */
public class RuleResizeLiveHeight extends RuleLiveSize<int[]> {

    private final int gravity;

    public RuleResizeLiveHeight(int gravity, LiveSize[] data) {
        super(data);
        this.gravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new int[data.length + 1];
            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();
            for (int i = 1; i < tmpData.length; i++) {
                tmpData[i] = (int) data[i - 1].calculate(vw, vh, parentSize, target, original, Gravity.FILL_VERTICAL);
            }
            tmpData[0] = original.getHeight();
        }

        ValueAnimator animator = ValueAnimator.ofInt(tmpData);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                update(target, (Integer) valueAnimator.getAnimatedValue());
                update(view, target);
            }
        });
        return initEvaluator(animator);
    }

    private void update(LayoutSize target, int height) {
        switch (gravity) {
            case Gravity.BOTTOM:
                target.bottom = target.top + height;
                break;
            case Gravity.CENTER_VERTICAL:
                int center = target.getCenterY();
                target.top = center - height / 2;
                target.bottom = center + height / 2;
                break;
            case Gravity.TOP:
            default:
                target.top = target.bottom - height;
                break;
        }
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize) {
        super.debug(view, target, original, parentSize);
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
        }
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            map.put("HeightLiveSize " + i, LiveSizeDebugHelper.translate(data[i], gravity, view.getContext()));
        }
        return map;
    }
}
