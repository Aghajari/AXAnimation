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

import android.animation.ValueAnimator;

import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimatorData;

/**
 * Will stop Animator a while before starting the next section.
 *
 * @author AmirHossein Aghajari
 */
public class WaitRule extends RuleSection {
    public final long duration;

    public WaitRule(long duration) {
        super(null);
        this.duration = duration;
    }

    public ValueAnimator createAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        return animator;
    }

    @Override
    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        super.setAnimatorValues(null);
    }
}
