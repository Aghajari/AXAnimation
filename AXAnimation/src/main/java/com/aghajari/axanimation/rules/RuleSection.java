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

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.listener.AXAnimatorEndListener;
import com.aghajari.axanimation.listener.AXAnimatorStartListener;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A RuleSection rules will start together but {@link com.aghajari.axanimation.AXAnimation}
 * will play sections one after another.
 *
 * @author AmirHossein Aghajari
 */
public class RuleSection implements Cloneable {

    private final Rule<?>[] rules;
    private AXAnimatorStartListener startListener;
    private AXAnimatorEndListener endListener;

    /**
     * If a rule's {@link Rule#animatorValues} was null,
     * Animator will use this one.
     */
    @Nullable
    protected AXAnimatorData animatorValues;

    public RuleSection(Rule<?>[] rules) {
        this(rules, null);
    }

    public RuleSection(Rule<?>[] rules, @Nullable AXAnimatorData animatorValues) {
        this.rules = rules;
        this.animatorValues = animatorValues;
    }

    public Rule<?>[] getRules() {
        return rules;
    }

    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        this.animatorValues = animatorValues;
    }

    @Nullable
    public AXAnimatorData getAnimatorValues() {
        return animatorValues;
    }

    public void onStart(AXAnimation animation){
        if (startListener != null)
            startListener.onAnimationStart(animation);
    }

    public void onEnd(AXAnimation animation){
        if (endListener != null)
            endListener.onAnimationEnd(animation);
    }

    public void withStartAction(AXAnimatorStartListener listener){
        this.startListener = listener;
    }

    public void withEndAction(AXAnimatorEndListener listener){
        this.endListener = listener;
    }

    public void debug(@NonNull final View view,
                      @Nullable final LayoutSize target,
                      @Nullable final LayoutSize original,
                      @Nullable final LayoutSize parentSize,
                      AXAnimation animation) {
    }

    public String getSectionName() {
        return getClass().getSimpleName();
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
}
