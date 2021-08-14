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
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.listener.AXAnimatorListenerAdapter;

/**
 * A {@link Rule} to start an {@link AXAnimation} during the animation.
 *
 * @author AmirHossein Aghajari
 */
public class OtherAnimationRule extends Rule<AXAnimation> {

    private View targetView;
    private final int id;

    public OtherAnimationRule(AXAnimation data) {
        super(data);
        this.targetView = null;
        this.id = 0;
    }

    public OtherAnimationRule(AXAnimation data, View view) {
        super(data);
        this.targetView = view;
        this.id = 0;
    }

    public OtherAnimationRule(AXAnimation data, int viewID) {
        super(data);
        this.targetView = null;
        this.id = viewID;
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);

        if (targetView == null) {
            if (id == 0) {
                targetView = view;
            } else if (id == AXAnimation.PARENT_ID) {
                targetView = (View) view.getParent();
            } else {
                targetView = ((View) view.getParent()).findViewById(id);
            }
        }
    }

    @Override
    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        if (animatorValues != null) {
            animatorValues.setDuration(data.getTotalDuration());
            animatorValues.setInterpolator(null);
        }
        super.setAnimatorValues(animatorValues);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull final View view, @Nullable final LayoutSize target, @Nullable final LayoutSize original, @Nullable LayoutSize parentSize) {

        final CustomAnimator animator = new CustomAnimator(isReverse(), data, new CustomAnimator.OnStart() {
            @Override
            public void onAnimationStart(Animator animator, boolean inReverse) {
                data.start(targetView, null, inReverse, false);
            }
        });

        AXAnimatorListenerAdapter listener = new AXAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(AXAnimation animation) {
                if (targetView == view) {
                    if (target != null && animation.hasLayoutRule())
                        target.set(animation.getTargetSize());
                }

                if (animator.tryEnd())
                    animation.removeAnimatorListener(this);
            }

            @Override
            public void onAnimationCancel(AXAnimation animation) {
                if (targetView == view) {
                    if (target != null && animation.hasLayoutRule())
                        target.set(animation.getTargetSize());
                }

                animator.end();
                animation.removeAnimatorListener(this);
            }
        };

        if (data.isRunning())
            data.cancel();

        data.addAnimatorListener(listener);

        return animator;
    }

    @Override
    public void onBindAnimator(@NonNull View view, @NonNull Animator animator) {
        super.onBindAnimator(view, animator);
        ((CustomAnimator) animator).bind();
    }

    @Override
    public void setCurrentPlayTime(Animator animator, long playTime) {
        super.setCurrentPlayTime(animator, playTime);
        data.setCurrentPlayTime(playTime);
    }

    @Override
    public long getCurrentPlayTime(Animator animator) {
        return data.getCurrentPlayTime();
    }

    private static class CustomAnimator extends ValueAnimator {
        private int repeatCount = 0;
        private boolean inReverse;
        private final AXAnimation data;
        private final OnStart onStart;

        private interface OnStart {
            void onAnimationStart(Animator animator, boolean inReverse);
        }

        public CustomAnimator(boolean reverse, final AXAnimation data, final OnStart onStart) {
            setIntValues(0, 1);
            this.onStart = onStart;
            this.data = data;
            inReverse = reverse;
        }

        public void bind() {
            repeatCount = getRepeatCount();
            // Handle repeat internally
            setRepeatCount(0);
        }

        @Override
        public void start() {
            onStart.onAnimationStart(this, inReverse);
        }

        @Override
        public void reverse() {
            onStart.onAnimationStart(this, inReverse);
        }

        @Override
        public void pause() {
            data.pause();
        }

        @Override
        public void resume() {
            data.resume();
        }

        @Override
        public void cancel() {
            data.cancel();
        }

        @Override
        public void end() {
            repeatCount = -100;
            data.end();
            super.end();
        }

        public boolean tryEnd() {
            if (repeatCount == -100)
                return true;

            if (repeatCount == 0) {
                super.end();
                return true;
            } else {
                repeatCount--;
                if (getRepeatMode() == REVERSE)
                    inReverse = !inReverse;

                start();
                return false;
            }
        }
    }
}
