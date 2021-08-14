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

import androidx.annotation.NonNull;

/**
 * A helper class to store Animator values such as duration, delay and interpolator.
 *
 * @author AmirHossein Aghajari
 */
public class AXAnimatorData implements Cloneable {
    private long duration;
    private long delay;
    private TimeInterpolator interpolator;
    private boolean firstValueFromView;
    private boolean inspect, clearOldInspect;
    private int repeatCount;
    @AXAnimation.RepeatMode
    private int repeatMode;

    public AXAnimatorData() {
        reset();
    }

    public AXAnimatorData(AXAnimatorData data) {
        importAnimatorData(data);
    }

    public AXAnimatorData(long duration){
        reset();
        setDuration(duration);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    public TimeInterpolator getInterpolator() {
        return interpolator;
    }

    public boolean isFirstValueFromView() {
        return firstValueFromView;
    }

    public void setFirstValueFromView(boolean firstValueFromView) {
        this.firstValueFromView = firstValueFromView;
    }

    public boolean isInspectEnabled() {
        return inspect;
    }

    public void setInspectEnabled(boolean enabled) {
        this.inspect = enabled;
    }

    public boolean isClearOldInspectEnabled() {
        return clearOldInspect;
    }

    public void setClearOldInspectEnabled(boolean clearOldInspect) {
        this.clearOldInspect = clearOldInspect;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatMode(@AXAnimation.RepeatMode int repeatMode) {
        this.repeatMode = repeatMode;
    }

    @AXAnimation.RepeatMode
    public int getRepeatMode() {
        return repeatMode;
    }

    public void reset() {
        duration = 300;
        delay = 0;
        interpolator = null;
        firstValueFromView = true;
        inspect = false;
        clearOldInspect = false;
        repeatCount = 0;
        repeatMode = AXAnimation.RESTART;
    }

    public void importAnimatorData(AXAnimatorData data) {
        duration = data.duration;
        delay = data.delay;
        interpolator = data.interpolator;
        firstValueFromView = data.firstValueFromView;
        inspect = data.inspect;
        clearOldInspect = data.clearOldInspect;
        repeatMode = data.repeatMode;
        repeatCount = data.repeatCount;
    }

    public void apply(Animator animator) {
        animator.setStartDelay(getDelay());
        animator.setDuration(getDuration());
        if (getInterpolator() != null)
            animator.setInterpolator(getInterpolator());
        if (animator instanceof ValueAnimator) {
            ((ValueAnimator) animator).setRepeatCount(getRepeatCount());
            ((ValueAnimator) animator).setRepeatMode(getRepeatMode());
        }
    }

    public long getTotalDuration(){
        return getDelay() + (getDuration() * (getRepeatCount() + 1));
    }

    @NonNull
    @Override
    public AXAnimatorData clone() {
        try {
            return (AXAnimatorData) super.clone();
        } catch (CloneNotSupportedException e) {
            return new AXAnimatorData(this);
        }
    }
}
