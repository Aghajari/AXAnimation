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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A not animated {@link Rule}, it doesn't have Animator.
 * You can use {@link #apply(View)} to do something with the target view.
 * Only supports delay, duration will be 0.
 * <p>
 * Example: {@link com.aghajari.axanimation.rules.property.RuleBringToFront}
 * Example: {@link com.aghajari.axanimation.rules.property.RuleSendToBack}
 *
 * @author AmirHossein Aghajari
 */
public abstract class NotAnimatedRule<T> extends Rule<T> {
    private final int id;
    private final View view;

    public NotAnimatedRule(T data) {
        super(data);
        id = -1;
        view = null;
    }

    public NotAnimatedRule(int viewID, T data) {
        super(data);
        this.id = viewID;
        view = null;
    }

    public NotAnimatedRule(View view, T data) {
        super(data);
        id = -1;
        this.view = view;
    }

    public abstract void apply(View targetView);

    @Override
    public void getReady(@NonNull final View view) {
        super.getReady(view);

        final View child;
        if (this.view != null)
            child = this.view;
        else if (id != -1 && view.getParent() != null)
            child = ((ViewGroup) view.getParent()).findViewById(id);
        else
            child = view;

        if (animatorValues != null && animatorValues.getDelay() > 0) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    apply(child);
                }
            }, animatorValues.getDelay());
        } else {
            apply(child);
        }
    }

    @Override
    public Animator onCreateAnimator(@NonNull View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        return null;
    }

    // You should update duration to 0,
    // So getTotalDuration() won't calculate the duration of a not animated rule
    @Override
    public void setAnimatorValues(AXAnimatorData animatorValues) {
        if (animatorValues != null) {
            animatorValues.setDuration(0);
            animatorValues.setRepeatCount(0);
            animatorValues.setInterpolator(null);
        }
        super.setAnimatorValues(animatorValues);
    }

}
