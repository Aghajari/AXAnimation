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

import com.aghajari.axanimation.AXAnimatorData;


/**
 * A wrapper for {@link Rule} to reverse it.
 *
 * @author AmirHossein Aghajari
 */
public class ReverseRule extends RuleWrapper {
    boolean keepOldData;

    public ReverseRule(@NonNull Rule<?> data) {
        this(data, true);
    }

    public ReverseRule(@NonNull Rule<?> data, boolean keepOldData) {
        super(data);
        this.keepOldData = keepOldData;
        this.animatorValues = data.animatorValues;
    }

    // notify the rule to prepare for reverse animation
    @Override
    public boolean shouldReverseAnimator(boolean reverseMode) {
        return data.shouldReverseAnimator(!reverseMode);
    }

    @Override
    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        this.animatorValues = animatorValues;
    }

    @Nullable
    @Override
    public AXAnimatorData getAnimatorValues() {
        return animatorValues;
    }

    @Override
    public void getReady(@NonNull View view) {
        isReverseRule = false;
        if (keepOldData) {
            data.getReadyForReverse(view);
            data.isReverseRule = true;
        } else {
            data.getReady(view);
            data.isReverseRule = false;
        }
    }

    @Override
    public void getReadyForReverse(@NonNull View view) {
        isReverseRule = true;
        data.isReverseRule = false;
        data.getReady(view);
    }

    @Override
    public String getRuleName() {
        return "Reverse_" + super.getRuleName();
    }

    @Override
    public boolean dutyHasChanged() {
        return true;
    }

    @Override
    public Rule<?>[] createRules() {
        Rule<?>[] rules = data.createRules();
        if (rules != null)
            return ReverseRule.reverseRules(rules, keepOldData);
        return null;
    }

    /**
     * @return reverse of the given rule
     */
    public static Rule<?> reverseRule(Rule<?> rule, boolean keepOldData) {
        if (rule instanceof RuleWrapper) {
            if (!((RuleWrapper) rule).dutyHasChanged()) {
                return reverseRule(((RuleWrapper) rule).getRule(), keepOldData);
            }
        }

        // ReverseRule should keep old data as tmpData...
        // Btw, Reversing a ReverseRule makes it a normal rule without loosing the tmpData

        /*Rule<?> r;
        if (rule instanceof ReverseRule) {
            r = ((ReverseRule) rule).getRule();
        } else {
            r = new ReverseRule(rule, keepOldData);
        }*/

        // DO NOT clone Rule!
        // ReverseRule needs to use tmpData to reverse it
        //return r;
        return new ReverseRule(rule, keepOldData);
    }

    /**
     * @return reverse rules of the given rules
     */
    public static Rule<?>[] reverseRules(Rule<?>[] rules, boolean keepOldData) {
        if (rules == null)
            return new Rule<?>[0];

        Rule<?>[] reverseRules = new ReverseRule[rules.length];
        for (int i = 0; i < rules.length; i++) {
            reverseRules[i] = reverseRule(rules[i], keepOldData);
        }
        return reverseRules;
    }
}
