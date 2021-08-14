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

import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimatorData;

import java.lang.ref.WeakReference;

/**
 * ReverseRuleSection will reverse a {@link RuleSection}
 *
 * @author AmirHossein Aghajari
 */
public class ReverseRuleSection extends RuleSectionWrapper {

    private final boolean keepOldData;
    private WeakReference<Rule<?>[]> rules;

    public ReverseRuleSection(RuleSection section) {
        super(section);
        this.keepOldData = true;
    }

    public ReverseRuleSection(RuleSection section, boolean keepOldData) {
        super(section);
        this.keepOldData = keepOldData;
    }

    public ReverseRuleSection(Rule<?>[] rules, boolean keepOldData) {
        super(new RuleSection(rules));
        this.keepOldData = keepOldData;
    }

    public ReverseRuleSection(Rule<?>[] rules, AXAnimatorData animatorValues, boolean keepOldData) {
        super(new RuleSection(rules, animatorValues));
        this.keepOldData = keepOldData;
    }

    @Override
    public Rule<?>[] getRules() {
        if (rules != null) {
            Rule<?>[] r = rules.get();
            if (r != null)
                return r;
        }
        Rule<?>[] r = ReverseRule.reverseRules(super.getRules(), keepOldData);
        rules = new WeakReference<>(r);
        return r;
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
    public boolean dutyHasChanged() {
        return true;
    }

    @Override
    public String getSectionName() {
        return "ReverseSection_" + super.getSectionName();
    }

}
