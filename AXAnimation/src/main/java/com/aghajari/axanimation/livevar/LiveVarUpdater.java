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
package com.aghajari.axanimation.livevar;

import android.animation.ValueAnimator;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.rules.RuleSection;

/**
 * LiveVarUpdater will update a {@link LiveVar} whenever section changed on animation.
 * {@link com.aghajari.axanimation.listener.AXAnimatorListenerAdapter#onRuleSectionChanged(AXAnimation, RuleSection)}
 *
 * @author AmirHossein Aghajari
 */
public abstract class LiveVarUpdater {

    protected final LiveVar<?> target;

    public LiveVarUpdater(LiveVar<?> target) {
        this.target = target;
    }

    public abstract void update(AXAnimation animation, int sectionIndex, int realSectionIndex, RuleSection section);

    /**
     * Updates target on each section.
     */
    @SafeVarargs
    public static <T> LiveVarUpdater forEachSection(LiveVar<T> target, final T... values) {
        return new LiveVarUpdater(target) {
            @Override
            public void update(AXAnimation animation, int sectionIndex, int realSectionIndex, RuleSection section) {
                if (values.length > realSectionIndex)
                    target.update(values[realSectionIndex]);
            }
        };
    }

    /**
     * Updates target with a duration.
     */
    @SafeVarargs
    public static <T> LiveVarUpdater forDuration(LiveVar<T> target, final int startSection, final long duration, final T... values) {
        return forAnimator(target, startSection, new AXAnimatorData(duration), values);
    }

    /**
     * Updates target with starting a {@link ValueAnimator}
     */
    @SafeVarargs
    public static <T> LiveVarUpdater forAnimator(LiveVar<T> target, final int startSection, final AXAnimatorData animatorValues, final T... values) {
        return new LiveVarUpdater(target) {
            ValueAnimator animator;

            @Override
            public void update(AXAnimation animation, int sectionIndex, int realSectionIndex, RuleSection section) {
                if (realSectionIndex == startSection) {
                    if (animator != null) {
                        animator.cancel();
                    }
                    animator = ValueAnimator.ofFloat(0, 1);
                    animatorValues.apply(animator);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float f = animation.getAnimatedFraction();
                            int index = (int) (f * values.length);
                            target.update(values[index]);
                        }
                    });
                    animator.start();
                }
            }
        };
    }
}