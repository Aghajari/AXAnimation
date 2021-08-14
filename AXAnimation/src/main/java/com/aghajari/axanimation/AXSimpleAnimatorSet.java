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
package com.aghajari.axanimation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.SkippedRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class to create an {@link Animator} for multi {@link Rule}s
 *
 * @author AmirHossein Aghajari
 * @see AXAnimation#createSimpleAnimator(View)
 */
@SuppressWarnings("ConstantConditions")
public class AXSimpleAnimatorSet extends AXSimpleAnimator {

    private final HashMap<Rule<?>, AXSimpleAnimator> animators = new HashMap<>();
    private Rule<?> mainRule;

    AXSimpleAnimatorSet(List<Rule<?>> rules, View target, ViewGroup.LayoutParams layoutParams) {
        super(new SkippedRule(null, "Doesn't have main rule"));

        long d = -2;

        for (Rule<?> rule : rules) {
            AXSimpleAnimator animator = AXSimpleAnimator.create(target, layoutParams, rule);
            animators.put(rule, animator);

            long d2 = animator.getTotalDuration();
            if (d2 > d) {
                d = d2;
                mainRule = rule;
            }
        }
    }

    public static AXSimpleAnimatorSet create(View target, Rule<?>... rules) {
        return AXSimpleAnimatorSet.create(target, null, rules);
    }

    public static AXSimpleAnimatorSet create(View target, ViewGroup.LayoutParams layoutParams, Rule<?>... rules) {
        return new AXSimpleAnimatorSet(new ArrayList<>(Arrays.asList(rules)), target, layoutParams);
    }

    @Override
    public long getStartDelay() {
        return animators.get(mainRule).getStartDelay();
    }

    @Override
    public void setStartDelay(long startDelay) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setStartDelay(startDelay);
    }

    @Override
    public AXSimpleAnimatorSet setDuration(long duration) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setDuration(duration);
        return this;
    }

    @Override
    public long getDuration() {
        return animators.get(mainRule).getDuration();
    }

    @Override
    public void setInterpolator(TimeInterpolator value) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setInterpolator(value);
    }

    @Override
    public boolean isRunning() {
        return animators.get(mainRule).isRunning();
    }

    @Override
    public void start() {
        for (AXSimpleAnimator animator : animators.values())
            animator.start();
    }

    @Override
    public void cancel() {
        for (AXSimpleAnimator animator : animators.values())
            animator.cancel();
    }

    @Override
    public void end() {
        for (AXSimpleAnimator animator : animators.values())
            animator.end();
    }

    @Override
    public void pause() {
        for (AXSimpleAnimator animator : animators.values())
            animator.pause();
    }

    @Override
    public void resume() {
        for (AXSimpleAnimator animator : animators.values())
            animator.resume();
    }

    @Override
    public boolean isPaused() {
        return animators.get(mainRule).isPaused();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public long getTotalDuration() {
        return animators.get(mainRule).getTotalDuration();
    }

    @Override
    public TimeInterpolator getInterpolator() {
        return animators.get(mainRule).getInterpolator();
    }

    @Override
    public boolean isStarted() {
        return animators.get(mainRule).isStarted();
    }

    @Override
    public void addListener(AnimatorListener listener) {
        animators.get(mainRule).addListener(listener);
    }

    @Override
    public void removeListener(AnimatorListener listener) {
        for (AXSimpleAnimator animator : animators.values())
            animator.removeListener(listener);
    }

    @Override
    public ArrayList<AnimatorListener> getListeners() {
        return animators.get(mainRule).getListeners();
    }

    @Override
    public void addPauseListener(AnimatorPauseListener listener) {
        animators.get(mainRule).addPauseListener(listener);
    }

    @Override
    public void removePauseListener(AnimatorPauseListener listener) {
        for (AXSimpleAnimator animator : animators.values())
            animator.removePauseListener(listener);
    }

    @Override
    public void removeAllListeners() {
        for (AXSimpleAnimator animator : animators.values())
            animator.removeAllListeners();
    }

    @Override
    public void setupStartValues() {
        for (AXSimpleAnimator animator : animators.values())
            animator.setupStartValues();
    }

    @Override
    public void setupEndValues() {
        for (AXSimpleAnimator animator : animators.values())
            animator.setupEndValues();
    }

    public void setTarget(@NonNull final View target) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setTarget(target);
    }

    public void setRepeatCount(int value) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setRepeatCount(value);
    }

    public int getRepeatCount() {
        return animators.get(mainRule).getRepeatCount();
    }

    public void setRepeatMode(int value) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setRepeatMode(value);
    }

    public int getRepeatMode() {
        return animators.get(mainRule).getRepeatMode();
    }

    public void setCurrentPlayTime(long playTime) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setCurrentPlayTime(playTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void setCurrentFraction(float fraction) {
        for (AXSimpleAnimator animator : animators.values())
            animator.setCurrentFraction(fraction);
    }

    public long getCurrentPlayTime() {
        return animators.get(mainRule).getCurrentPlayTime();
    }

    public Object getAnimatedValue() {
        if (animators.size() == 1) {
            return animators.get(mainRule).getAnimatedValue();
        } else
            return null;
    }

    public Object getAnimatedValue(String propertyName) {
        if (animators.size() == 1) {
            return animators.get(mainRule).getAnimatedValue(propertyName);
        } else
            return null;
    }

    public void addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        if (animators.size() == 1) {
            animators.get(mainRule).addUpdateListener(listener);
        } else
            throw new RuntimeException("AXSimpleAnimatorSet doesn't support addUpdateListener");
    }

    public void removeAllUpdateListeners() {
        for (AXSimpleAnimator animator : animators.values())
            animator.removeAllUpdateListeners();
    }

    public void removeUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        for (AXSimpleAnimator animator : animators.values())
            animator.removeUpdateListener(listener);
    }

    public void reverse() {
        for (AXSimpleAnimator animator : animators.values())
            animator.reverse();
    }

    public float getAnimatedFraction() {
        return animators.get(mainRule).getAnimatedFraction();
    }

    public Rule<?> getRules() {
        return (Rule<?>) animators.keySet();
    }

    public Collection<AXSimpleAnimator> getAnimators() {
        return animators.values();
    }

    public Map<Rule<?>, AXSimpleAnimator> getAnimatorsMap() {
        return animators;
    }

}
