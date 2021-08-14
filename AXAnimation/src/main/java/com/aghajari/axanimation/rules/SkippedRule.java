package com.aghajari.axanimation.rules;

import android.animation.Animator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * The rule has skipped on animator but still you can find it on rules,
 * in this way the skipped rule won't change indexes.
 *
 * @author AmirHossein Aghajari
 */
public class SkippedRule extends Rule<Rule<?>> {
    private final String reason;

    public SkippedRule(Rule<?> data, String reason) {
        super(data);
        this.reason = reason;
    }

    public Rule<?> getRule() {
        return data;
    }

    public String getReason() {
        return reason;
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view, @Nullable LayoutSize target, @Nullable LayoutSize original, @Nullable LayoutSize parentSize) {
        return null;
    }

    @Override
    public String getRuleName() {
        return "Skipped_" + data.getRuleName();
    }
}
