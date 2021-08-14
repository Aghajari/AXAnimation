package com.aghajari.axanimation.rules;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimatorData;
import com.aghajari.axanimation.livevar.LayoutSize;

import java.util.List;

/**
 * Wrapping a {@link Rule} will give you more accessibility to change rule's duty.
 * Use {@link ReverseRule} if you want to reverse a rule.
 *
 * @author AmirHossein Aghajari
 */
public class RuleWrapper extends Rule<Rule<?>> {

    public RuleWrapper(@NonNull Rule<?> data) {
        super(data);
    }

    public Rule<?> getRule() {
        return data;
    }

    @Override
    public void setAnimatorValues(@Nullable AXAnimatorData animatorValues) {
        data.setAnimatorValues(animatorValues);
    }

    @Nullable
    @Override
    public AXAnimatorData getAnimatorValues() {
        return data.getAnimatorValues();
    }

    @Override
    public boolean shouldReverseAnimator(boolean reverseMode) {
        return data.shouldReverseAnimator(reverseMode);
    }

    @Override
    public long shouldWait() {
        return data.shouldWait();
    }

    @Override
    public void update(@NonNull View view, LayoutSize target) {
        data.update(view, target);
    }

    @Override
    public Animator onCreateAnimator(@NonNull View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        return data.onCreateAnimator(view, target, original, parentSize);
    }

    @Override
    public void onBindAnimator(@NonNull View view, @NonNull Animator animator) {
        data.onBindAnimator(view, animator);
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return data.getEvaluatorClass();
    }

    @Override
    public TypeEvaluator<?> createEvaluator() {
        return data.createEvaluator();
    }

    @Override
    protected Animator initEvaluator(@NonNull Animator animator) {
        return data.initEvaluator(animator);
    }

    @Override
    public void getReady(@NonNull View view) {
        data.getReady(view);
    }

    @Override
    public void getReadyForReverse(@NonNull View view) {
        data.getReadyForReverse(view);
    }

    @Override
    public void getReady(@NonNull List<LayoutSize> layouts) {
        data.getReady(layouts);
    }

    @Override
    public void getFromLiveData() {
        super.getFromLiveData();
        data.getFromLiveData();
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return data.isLayoutSizeNecessary();
    }

    @Override
    public void setCurrentPlayTime(Animator animator, long playTime) {
        data.setCurrentPlayTime(animator, playTime);
    }

    @Override
    public long getCurrentPlayTime(Animator animator) {
        return data.getCurrentPlayTime(animator);
    }

    @Override
    public void debug(@NonNull View view, @Nullable LayoutSize target, @Nullable LayoutSize original, @Nullable LayoutSize parentSize) {
        data.debug(view, target, original, parentSize);
    }

    @Override
    public void debug(@Nullable Animator animator) {
        data.debug(animator);
    }

    @Override
    public void setStartedAsReverse(boolean isStartedAsReverse) {
        super.setStartedAsReverse(isStartedAsReverse);
        data.setStartedAsReverse(isStartedAsReverse);
    }

    @Override
    public boolean isReverse() {
        return data.isReverse();
    }


    @Override
    public Object getData() {
        return data.getData();
    }

    @Override
    public String getRuleName() {
        return data.getRuleName();
    }

    @Nullable
    @Override
    public Rule<?>[] createRules() {
        return data.createRules();
    }

    @Override
    public boolean isRuleSet() {
        return data.isRuleSet();
    }

    public boolean dutyHasChanged() {
        return false;
    }

}
