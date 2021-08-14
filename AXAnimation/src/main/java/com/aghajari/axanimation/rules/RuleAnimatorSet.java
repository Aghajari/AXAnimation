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
import android.animation.AnimatorSet;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.livevar.LayoutSize;

import java.util.HashMap;
import java.util.List;

/**
 * A {@link Rule} which uses {@link AnimatorSet}
 *
 * @author AmirHossein Aghajari
 */
public abstract class RuleAnimatorSet extends Rule<Rule<?>[]> {

    private final String ruleName;
    protected final HashMap<Rule<?>, Animator> animators = new HashMap<>();

    public RuleAnimatorSet(final String ruleName, @Nullable Rule<?>... data) {
        super(data);
        this.ruleName = ruleName;
    }

    public static RuleAnimatorSet sequentially(Rule<?>... rules) {
        return new RuleAnimatorSet("AnimatorSet.Sequentially", rules) {
            @Override
            protected void play(AnimatorSet animatorSet, Animator... animators) {
                animatorSet.playSequentially(animators);
            }
        };
    }

    public static RuleAnimatorSet together(Rule<?>... rules) {
        return new RuleAnimatorSet("AnimatorSet.Together", rules) {
            @Override
            protected void play(AnimatorSet animatorSet, Animator... animators) {
                animatorSet.playTogether(animators);
            }
        };
    }

    @Override
    public Animator onCreateAnimator(@NonNull View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        AnimatorSet animatorSet = new AnimatorSet();

        animators.clear();

        for (Rule<?> rule : data) {
            Animator animator = rule.onCreateAnimator(view, target, original, parentSize);

            if (animator != null) {
                animators.put(rule, animator);
            }
        }

        play(animatorSet, animators.values().toArray(new Animator[0]));
        animatorSet.start();
        return animatorSet;
    }

    protected abstract void play(AnimatorSet animatorSet, Animator... animators);

    @Override
    public void onBindAnimator(@NonNull View view, @NonNull Animator animator) {
        super.onBindAnimator(view, animator);
        for (Rule<?> rule : animators.keySet()) {
            Animator a = animators.get(rule);
            if (a != null)
                rule.onBindAnimator(view, a);
        }
        animators.clear();
    }

    @Override
    public long shouldWait() {
        for (Rule<?> rule : data) {
            long wait = rule.shouldWait();
            if (wait != -1)
                return wait;
        }

        return super.shouldWait();
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);

        for (Rule<?> rule : data) {
            rule.isReverseRule = isReverseRule;
            rule.getReady(view);
        }
    }

    @Override
    public void getReadyForReverse(@NonNull View view) {
        super.getReadyForReverse(view);

        for (Rule<?> rule : data) {
            rule.isReverseRule = isReverseRule;
            rule.getReadyForReverse(view);
        }
    }

    @Override
    public void getReady(@NonNull List<LayoutSize> layouts) {
        super.getReady(layouts);

        for (Rule<?> rule : data) {
            rule.isReverseRule = isReverseRule;
            rule.getReady(layouts);
        }
    }

    @Override
    public void setStartedAsReverse(boolean isStartedAsReverse) {
        super.setStartedAsReverse(isStartedAsReverse);

        for (Rule<?> rule : data)
            rule.setStartedAsReverse(isStartedAsReverse);
    }

    @Override
    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        super.setAnimatorValues(animatorValues);
        for (Rule<?> rule : data) {
            if (rule.getAnimatorValues() == null)
                rule.setAnimatorValues(animatorValues);
        }
    }

    @Nullable
    @Override
    public AXAnimatorData getAnimatorValues() {
        return super.getAnimatorValues();
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        for (Rule<?> rule : data) {
            if (rule.isLayoutSizeNecessary())
                return true;
        }

        return super.isLayoutSizeNecessary();
    }

    @Override
    public void debug(@NonNull View view, @Nullable LayoutSize target, @Nullable LayoutSize original, @Nullable LayoutSize parentSize) {
        super.debug(view, target, original, parentSize);

        for (Rule<?> rule : data)
            rule.debug(view, target, original, parentSize);
    }

    @Override
    public void debug(@Nullable Animator animator) {
        super.debug(animator);

        for (Rule<?> rule : data)
            rule.debug(animator);
    }

    @Override
    public void setCurrentPlayTime(Animator animator, long playTime) {
        if (animator instanceof AnimatorSet) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((AnimatorSet) animator).setCurrentPlayTime(playTime);
            }
        }

        super.setCurrentPlayTime(animator, playTime);
    }

    @Override
    public long getCurrentPlayTime(Animator animator) {
        if (animator instanceof AnimatorSet) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return ((AnimatorSet) animator).getCurrentPlayTime();
            }
        }

        return super.getCurrentPlayTime(animator);
    }

    @Override
    public void getFromLiveData() {
        super.getFromLiveData();

        for (Rule<?> rule : data)
            rule.getFromLiveData();
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }
}