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
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.layouts.AnimatedLayoutParams;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveVar;

import java.util.List;

/**
 * Animator Rule will create an special animator {@link #onCreateAnimator(View, LayoutSize, LayoutSize, LayoutSize)}
 * for each rule.
 * Some rules such as {@link NotAnimatedRule} don't have Animator,
 * This rules can update target view on {@link #getReady(View)} event.
 * <p>
 * Use {@link NotAnimatedRule} If you don't want to create any Animator.
 * Use {@link RuleWithTmpData} If you want to store a tmpData. (Non-Cloneable tmpData)
 * Use {@link RuleSet} If you want to create multi rules in one rule.
 * Use {@link com.aghajari.axanimation.draw.DrawRule} If you want to draw on a
 * {@link com.aghajari.axanimation.draw.DrawableLayout} with {@link android.graphics.Canvas}
 * Also see {@link PropertyRule} and {@link PropertyValueRule}
 *
 * @author AmirHossein Aghajari
 */
public abstract class Rule<T> implements Cloneable {

    /**
     * Store Animator values here
     */
    protected T data;

    /**
     * Store Live Animator values here
     * Should update {@link #data} on {@link #getFromLiveData()}
     */
    protected LiveVar<T> liveData;

    /**
     * A {@link ReverseRule} will set this filed true
     */
    protected boolean isReverseRule = false;

    /**
     * Will be true if {@link #shouldReverseAnimator(boolean)} returned true
     */
    public boolean isStartedAsReverse = false;

    /**
     * Animator values (duration, delay, interpolator)
     * You don't need to set them on {@link #onCreateAnimator(View, LayoutSize, LayoutSize, LayoutSize)}
     */
    @Nullable
    protected AXAnimatorData animatorValues;

    public Rule(T data) {
        this.data = data;
    }

    public Rule(LiveVar<T> data) {
        this.data = data.get();
        this.liveData = data;
    }

    public void getFromLiveData(){
        if (liveData != null)
            data = liveData.get();
    }

    /**
     * Create an Animator or skip
     */
    @Nullable
    public abstract Animator onCreateAnimator(@NonNull final View view,
                                              @Nullable final LayoutSize target,
                                              @Nullable final LayoutSize original,
                                              @Nullable final LayoutSize parentSize);

    /**
     * Customize final options to the created animator.
     * Note: changing the duration, delay here won't apply on {@link AXAnimation#getTotalDuration()}
     */
    public void onBindAnimator(@NonNull final View view, final @NonNull Animator animator) {
    }

    /**
     * @return Evaluator class here if Animator uses a custom Evaluator.
     * RuleWrappers should have access to the evaluator!
     */
    public Class<?> getEvaluatorClass() {
        return null;
    }

    /**
     * Creates an Evaluator base on {@link #getEvaluatorClass()}
     *
     * @return the created Evaluator.
     */
    public TypeEvaluator<?> createEvaluator() {
        Class<?> cls = getEvaluatorClass();
        if (cls != null) {
            try {
                return (TypeEvaluator<?>) cls.newInstance();
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * @return True if the {@link android.animation.ValueAnimator} must be reverse.
     */
    public boolean shouldReverseAnimator(boolean reverseMode) {
        return reverseMode;
    }

    /**
     * return -1 to keep going or return a delay value
     */
    public long shouldWait() {
        return -1;
    }

    /**
     * prepare for creating an Animator for this rule
     */
    public void getReady(@NonNull final View view) {
        isReverseRule = false;
    }

    /**
     * prepare for creating an Animator for this rule
     * Note: This is a ReverseRule!
     */
    public void getReadyForReverse(@NonNull final View view) {
        getReady(view);
        isReverseRule = true;
    }

    /**
     * prepare for creating an Animator for this rule
     *
     * @param layouts Layouts of old sections.
     */
    public void getReady(@NonNull List<LayoutSize> layouts) {
    }

    /**
     * Update view's layout
     * Use this on the created Animator for updating view's layout.
     */
    public void update(@NonNull View view, final LayoutSize target) {
        if (target != null && view.getLayoutParams() instanceof AnimatedLayoutParams) {
            AnimatedLayoutParams lp = (AnimatedLayoutParams) view.getLayoutParams();
            lp.left = target.left;
            lp.right = target.right;
            lp.bottom = target.bottom;
            lp.top = target.top;
        }
        view.requestLayout();
    }

    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        this.animatorValues = animatorValues;
    }

    @Nullable
    public AXAnimatorData getAnimatorValues() {
        return animatorValues;
    }

    /**
     * @return True if this rule needs real {@link LayoutSize}s on {@link #onCreateAnimator(View, LayoutSize, LayoutSize, LayoutSize)},
     * false otherwise. View's parent must be an instance of {@link com.aghajari.axanimation.layouts.AnimatedLayout}
     * to get exact {@link LayoutSize}
     */
    public boolean isLayoutSizeNecessary() {
        return false;
    }

    /**
     * AXAnimator will call this after {@link #onCreateAnimator(View, LayoutSize, LayoutSize, LayoutSize)}
     * Layout rules should apply inspection if {@link AXAnimatorData#isInspectEnabled()}
     * returned True on {@link #animatorValues}.
     * Other rules can use this for debugging themselves.
     * <p>
     * Note: Debug starts from here
     */
    public void debug(@NonNull final View view,
                      @Nullable final LayoutSize target,
                      @Nullable final LayoutSize original,
                      @Nullable final LayoutSize parentSize) {
    }

    /**
     * AXAnimator will call this after {@link #onBindAnimator(View, Animator)}}
     * If animator was null, AXAnimator will call debug after {@link #debug(View, LayoutSize, LayoutSize, LayoutSize)}
     * Rules can use this for debugging themselves.
     * <p>
     * Note: Debug ends from here
     */
    public void debug(@Nullable Animator animator) {
    }

    public void setStartedAsReverse(boolean isStartedAsReverse) {
        this.isStartedAsReverse = isStartedAsReverse;
    }

    public boolean isReverse() {
        return isReverseRule || isStartedAsReverse;
    }

    public void setCurrentPlayTime(Animator animator, long playTime) {
        if (animator instanceof ValueAnimator)
            ((ValueAnimator) animator).setCurrentPlayTime(playTime - animator.getStartDelay());
    }

    public long getCurrentPlayTime(Animator animator) {
        if (animator instanceof ValueAnimator)
            return ((ValueAnimator) animator).getCurrentPlayTime();
        return 0;
    }

    @NonNull
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Only a {@link RuleSet} can use this.
     */
    @Nullable
    public Rule<?>[] createRules() {
        return null;
    }

    /**
     * @return True if this rule is a {@link RuleSet} and wants to use {@link #createRules()},
     * False otherwise.
     */
    public boolean isRuleSet() {
        return false;
    }

    public String getRuleName() {
        return getClass().getSimpleName();
    }

    public Object getData() {
        return data;
    }

    /**
     * Sets the animator evaluator from {@link #createEvaluator()}
     */
    protected Animator initEvaluator(@NonNull Animator animator) {
        if (animator instanceof ValueAnimator) {
            TypeEvaluator<?> evaluator = createEvaluator();
            if (evaluator != null)
                ((ValueAnimator) animator).setEvaluator(evaluator);
        }
        return animator;
    }

}