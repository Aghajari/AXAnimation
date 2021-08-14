package com.aghajari.axanimation.rules;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * Wrapping a {@link RuleSection} will give you more accessibility to change ruleSection's duty.
 * Use {@link ReverseRuleSection} if you want to reverse a section.
 *
 * @author AmirHossein Aghajari
 */
public class RuleSectionWrapper extends RuleSection {

    private final RuleSection section;

    public RuleSectionWrapper(RuleSection section) {
        super(null);
        this.section = section;
    }

    public RuleSection getRuleSection() {
        if (dutyHasChanged())
            return this;

        return section;
    }

    @Override
    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        section.setAnimatorValues(animatorValues);
    }

    @Nullable
    @Override
    public AXAnimatorData getAnimatorValues() {
        return section.getAnimatorValues();
    }

    @Override
    public Rule<?>[] getRules() {
        return section.getRules();
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize,
                      AXAnimation animation) {
        section.debug(view, target, original, parentSize, animation);
    }

    @Override
    public String getSectionName() {
        return section.getSectionName();
    }

    @Override
    public void onStart(AXAnimation animation) {
        section.onStart(animation);
    }

    @Override
    public void onEnd(AXAnimation animation) {
        section.onEnd(animation);
    }

    public boolean dutyHasChanged() {
        return false;
    }

}
