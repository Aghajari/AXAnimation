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

import com.aghajari.axanimation.layouts.AnimatedLayout;
import com.aghajari.axanimation.inspect.InspectLayout;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.layouts.OnLayoutSizeReadyListener;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.utils.InspectUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A helper class to create an {@link Animator} for a {@link Rule}
 *
 * @author AmirHossein Aghajari
 * @see AXAnimation#createSimpleAnimator(View)
 */
public class AXSimpleAnimator extends Animator {

    protected final Rule<?> rule;
    protected Animator animator;
    protected final ViewGroup.LayoutParams fromLayoutParams;
    protected final AXAnimatorData animatorValues;

    private final ArrayList<AnimatorListener> listeners = new ArrayList<>();
    private final ArrayList<AnimatorPauseListener> pauseListeners = new ArrayList<>();
    private final ArrayList<ValueAnimator.AnimatorUpdateListener> updateListeners = new ArrayList<>();

    private final static int STATE_NONE = 0;
    private final static int STATE_START = 1;
    private final static int STATE_REVERSE = 2;
    private final static int STATE_END = 3;
    private final static int STATE_PAUSE = 4;
    private int state = STATE_NONE;

    private float fraction = 0;
    private long playTime = 0;
    private boolean isReverse = false;
    private View view;
    private boolean isReady = false;
    private LayoutSize size = null;

    private final OnLayoutSizeReadyListener layoutSizeReadyListener = new OnLayoutSizeReadyListener() {
        @Override
        public void onReady(View view, LayoutSize size) {
            isReady = true;
            AXSimpleAnimator.this.size = size;
            createAnimator(view, size);
        }
    };

    AXSimpleAnimator(Rule<?> rule) {
        this.rule = rule;
        this.fromLayoutParams = null;
        this.animatorValues = new AXAnimatorData();
    }

    private AXSimpleAnimator(Rule<?> rule, View target, ViewGroup.LayoutParams fromLayoutParams) {
        this.rule = rule;
        this.fromLayoutParams = fromLayoutParams;

        animatorValues = new AXAnimatorData();
        if (rule.getAnimatorValues() != null)
            animatorValues.importAnimatorData(rule.getAnimatorValues());

        setTarget(target);
    }

    public static AXSimpleAnimator create(View target, Rule<?> rule) {
        return new AXSimpleAnimator(rule, target, null);
    }

    public static AXSimpleAnimator create(View target, ViewGroup.LayoutParams layoutParams, Rule<?> rule) {
        return new AXSimpleAnimator(rule, target, layoutParams);
    }

    public Animator getAnimator() {
        return animator;
    }

    public Rule<?> getRule() {
        return rule;
    }

    @Override
    public long getStartDelay() {
        return animatorValues.getDelay();
    }

    @Override
    public void setStartDelay(long startDelay) {
        if (animator != null)
            animator.setStartDelay(startDelay);
        animatorValues.setDelay(startDelay);
    }

    @Override
    public AXSimpleAnimator setDuration(long duration) {
        if (animator != null)
            animator.setDuration(duration);
        animatorValues.setDuration(duration);
        return this;
    }

    @Override
    public long getDuration() {
        return animatorValues.getDuration();
    }

    @Override
    public void setInterpolator(TimeInterpolator value) {
        if (animator != null)
            animator.setInterpolator(value);
        animatorValues.setInterpolator(value);
    }

    @Override
    public boolean isRunning() {
        if (animator != null)
            return animator.isRunning();
        return state == STATE_START || state == STATE_REVERSE;
    }

    public void release() {
        animator = null;
        isReady = false;
        setTarget(view);
    }

    @Override
    public void start() {
        if (animator != null) {
            state = STATE_NONE;
            if (isReverse) {
                rule.setStartedAsReverse(true);
                ((ValueAnimator) animator).reverse();
            } else {
                animator.start();
            }
        } else {
            state = STATE_START;

            if (isReverse)
                rule.setStartedAsReverse(true);

            if (isReady)
                createAnimator(view, size);
        }
    }

    @Override
    public void cancel() {
        state = STATE_NONE;

        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    public void end() {
        if (animator != null) {
            state = STATE_NONE;
            animator.end();
        } else {
            state = STATE_END;

            if (isReady)
                createAnimator(view, size);
        }
    }

    @Override
    public void pause() {
        if (animator != null) {
            state = STATE_NONE;
            animator.pause();
        } else {
            state = STATE_PAUSE;

            if (isReady)
                createAnimator(view, size);
        }
    }

    @Override
    public void resume() {
        if (animator != null) {
            state = STATE_NONE;
            animator.resume();
        } else {
            state = STATE_START;

            if (isReady)
                createAnimator(view, size);
        }
    }

    @Override
    public boolean isPaused() {
        if (animator != null)
            return animator.isPaused();
        return state == STATE_PAUSE;
    }

    @Override
    public long getTotalDuration() {
        if (animatorValues.getRepeatCount() == ValueAnimator.INFINITE) {
            return -1;
        } else {
            return animatorValues.getTotalDuration();
        }
    }

    @Override
    public TimeInterpolator getInterpolator() {
        return animatorValues.getInterpolator();
    }

    @Override
    public boolean isStarted() {
        if (animator != null)
            return animator.isStarted();

        return state != STATE_NONE;
    }

    @Override
    public void addListener(AnimatorListener listener) {
        listeners.add(listener);

        if (animator != null)
            animator.addListener(listener);
    }

    @Override
    public void removeListener(AnimatorListener listener) {
        listeners.remove(listener);
        if (animator != null)
            animator.removeListener(listener);
    }

    @Override
    public ArrayList<AnimatorListener> getListeners() {
        if (animator != null)
            return animator.getListeners();
        return listeners;
    }

    @Override
    public void addPauseListener(AnimatorPauseListener listener) {
        pauseListeners.add(listener);
        if (animator != null)
            animator.addPauseListener(listener);
    }

    @Override
    public void removePauseListener(AnimatorPauseListener listener) {
        pauseListeners.add(listener);

        if (animator != null)
            animator.removePauseListener(listener);
    }

    @Override
    public void removeAllListeners() {
        if (animator != null)
            animator.removeAllListeners();
    }

    @Override
    public void setupStartValues() {
        if (animator != null)
            animator.setupStartValues();
    }

    @Override
    public void setupEndValues() {
        if (animator != null)
            animator.setupEndValues();
    }

    public void setTarget(@NonNull final View target) {
        if (view == null || view != target || animator != null) {
            isReady = false;
        }
        this.view = target;

        if (isReady) {
            createAnimator(target, size);
            return;
        }

        if (animator != null) {
            if (animator instanceof ValueAnimator) {
                fraction = ((ValueAnimator) animator).getAnimatedFraction();
                playTime = ((ValueAnimator) animator).getCurrentPlayTime();
            }

            if (animator.isRunning())
                animator.cancel();
            state = STATE_NONE;
        }
        animator = null;
        isReverse = rule.shouldReverseAnimator(false);

        if (rule.isLayoutSizeNecessary()) {
            if (target.getParent() instanceof AnimatedLayout) {
                if (fromLayoutParams == null) {
                    ((AnimatedLayout) target.getParent()).getLayoutSize(target, layoutSizeReadyListener);
                } else {
                    ((AnimatedLayout) target.getParent()).getLayoutSize(target, fromLayoutParams, layoutSizeReadyListener);
                }
            } else {
                throw new RuntimeException("View's parent must be AnimatedLayout!");
            }
        } else {
            isReady = true;
            createAnimator(target, null);
        }
    }

    private void createAnimator(final View view, final LayoutSize size) {
        createAnimator(view, size, true);
    }

    private void createAnimator(final View view, final LayoutSize size, boolean getReady) {
        if (state == STATE_NONE)
            return;

        if (view.getParent() instanceof InspectLayout) {
            if (animatorValues.isClearOldInspectEnabled())
                InspectUtils.clearInspect(view);

            if (animatorValues.isInspectEnabled()) {
                ((InspectLayout) view.getParent()).getReadyForInspect(true);
            }
        }

        rule.getFromLiveData();
        if (getReady) {
            rule.getReady(view);
            //noinspection unchecked
            rule.getReady(Collections.EMPTY_LIST);
        }

        if (rule.shouldWait() >= 0) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createAnimator(view, size, false);
                }
            }, rule.shouldWait());
            return;
        }

        LayoutSize parentSize = null;
        if (view.getParent() instanceof AnimatedLayout) {
            parentSize = ((AnimatedLayout) view.getParent()).getLayoutSize();
        }

        animator = rule.onCreateAnimator(view, size, size, parentSize);

        if (animator == null)
            throw new RuntimeException(rule.getRuleName() + " Doesn't support SimpleAnimator!");

        animator.setDuration(animatorValues.getDuration());
        animator.setStartDelay(animatorValues.getDelay());
        animator.setInterpolator(animatorValues.getInterpolator());

        for (AnimatorListener listener : listeners)
            animator.addListener(listener);

        for (AnimatorPauseListener pauseListener : pauseListeners)
            animator.addPauseListener(pauseListener);

        if (animator instanceof ValueAnimator) {
            for (ValueAnimator.AnimatorUpdateListener updateListener : updateListeners)
                ((ValueAnimator) animator).addUpdateListener(updateListener);

            ((ValueAnimator) animator).setRepeatCount(animatorValues.getRepeatCount());
            ((ValueAnimator) animator).setRepeatMode(animatorValues.getRepeatMode());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                ((ValueAnimator) animator).setCurrentFraction(fraction);
            }
            ((ValueAnimator) animator).setCurrentPlayTime(playTime);
        }

        rule.debug(view, size, size, parentSize);
        rule.onBindAnimator(view, animator);
        rule.debug(animator);

        switch (state) {
            case STATE_PAUSE:
                start();
                pause();
                break;
            case STATE_REVERSE:
                reverse();
                break;
            case STATE_START:
                start();
                break;
            case STATE_END:
                end();
                break;
        }
    }

    public void setRepeatCount(int value) {
        if (animator != null)
            ((ValueAnimator) animator).setRepeatCount(value);
        animatorValues.setRepeatCount(value);
    }

    public int getRepeatCount() {
        if (animator != null)
            return ((ValueAnimator) animator).getRepeatCount();
        return animatorValues.getRepeatCount();
    }

    public void setRepeatMode(int value) {
        if (animator != null)
            ((ValueAnimator) animator).setRepeatMode(value);
        animatorValues.setRepeatMode(value);
    }

    public int getRepeatMode() {
        if (animator != null)
            return ((ValueAnimator) animator).getRepeatMode();
        return animatorValues.getRepeatMode();
    }

    public void setCurrentPlayTime(long playTime) {
        if (animator != null)
            ((ValueAnimator) animator).setCurrentPlayTime(playTime);
        this.playTime = playTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void setCurrentFraction(float fraction) {
        if (animator != null) {
            ((ValueAnimator) animator).setCurrentFraction(fraction);
        }
        this.fraction = fraction;
    }

    public long getCurrentPlayTime() {
        if (animator != null)
            return ((ValueAnimator) animator).getCurrentPlayTime();
        return playTime;
    }

    public Object getAnimatedValue() {
        if (animator != null)
            return ((ValueAnimator) animator).getAnimatedValue();
        return null;
    }

    public Object getAnimatedValue(String propertyName) {
        if (animator != null)
            return ((ValueAnimator) animator).getAnimatedValue(propertyName);
        return null;
    }

    public void addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        if (animator != null) {
            ((ValueAnimator) animator).addUpdateListener(listener);
        }
        updateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        if (animator != null) {
            ((ValueAnimator) animator).removeAllUpdateListeners();
        }
        updateListeners.clear();
    }

    public void removeUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        updateListeners.remove(listener);
        if (animator != null)
            ((ValueAnimator) animator).removeUpdateListener(listener);
    }

    public void reverse() {
        if (animator != null) {
            state = STATE_NONE;
            if (isReverse) {
                animator.start();
            } else {
                rule.setStartedAsReverse(true);
                ((ValueAnimator) animator).reverse();
            }
        } else {
            state = STATE_REVERSE;

            if (!isReverse)
                rule.setStartedAsReverse(true);

            if (isReady)
                createAnimator(view, size);
        }
    }

    public float getAnimatedFraction() {
        if (animator != null)
            return ((ValueAnimator) animator).getAnimatedFraction();
        return fraction;
    }
}
