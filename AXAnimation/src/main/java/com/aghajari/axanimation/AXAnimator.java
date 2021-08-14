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
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import com.aghajari.axanimation.layouts.AnimatedLayout;
import com.aghajari.axanimation.layouts.AnimatedLayoutParams;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.listener.AXAnimatorListener;
import com.aghajari.axanimation.livevar.LiveVarUpdater;
import com.aghajari.axanimation.prerule.PreRule;
import com.aghajari.axanimation.rules.ReverseWaitRule;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleSection;
import com.aghajari.axanimation.rules.RuleSectionWrapper;
import com.aghajari.axanimation.rules.SkippedRule;
import com.aghajari.axanimation.rules.WaitNotifyRule;
import com.aghajari.axanimation.rules.WaitRule;
import com.aghajari.axanimation.utils.InspectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to start animation of {@link AXAnimation}
 *
 * @author AmirHossein Aghajari
 */
class AXAnimator {
    final ArrayList<AXAnimatorListener> listeners = new ArrayList<>();
    final List<Animator> animators = new ArrayList<>();
    final List<LayoutSize> layouts = new ArrayList<>();
    final int[] indexes = new int[2];
    final int[] targetIndexes = new int[2];
    LayoutSize[] layoutSizes = new LayoutSize[2];
    View targetView;

    boolean end;
    boolean paused, running;
    boolean reverse = false;
    boolean needsEndFirst = false;
    LayoutSize targetSize;
    AXAnimation animation;
    long playTime;
    long customPlayTime = 0;
    long reverseDelay;
    int repeatCount;
    int repeatMode;

    public void pause() {
        running = false;
        paused = true;
        for (Animator animator : animators) {
            animator.pause();
        }

        for (AXAnimatorListener listener : listeners)
            listener.onAnimationPause(animation);
    }

    public void resume() {
        running = true;
        paused = false;
        for (Animator animator : animators) {
            animator.resume();
        }

        for (AXAnimatorListener listener : listeners)
            listener.onAnimationResume(animation);
    }

    public void cancel() {
        running = false;
        paused = false;
        try {
            for (Animator animator : animators) {
                animator.cancel();
            }
        } catch (Exception ignore) {
        }
        animators.clear();

        for (AXAnimatorListener listener : listeners)
            listener.onAnimationCancel(animation);

        AXAnimationSaver.clear(targetView, animation);
    }

    public void end() {
        pause();
        running = false;
        paused = false;
        end = true;
        start(targetView, layoutSizes[0], layoutSizes[1], animation, indexes[0]);
    }

    private boolean repeat() {
        if (repeatCount == 0)
            return true;

        if (repeatCount != AXAnimation.INFINITE)
            repeatCount--;
        if (repeatMode == AXAnimation.REVERSE)
            reverse = !reverse;

        start(targetView, layoutSizes[0], layoutSizes[1], animation, reverse, end, false);
        return false;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isRunning() {
        return running;
    }

    public void start(View view, LayoutSize parentSize, LayoutSize originalSize, AXAnimation animation, boolean reverse, boolean endMode) {
        start(view, parentSize, originalSize, animation, reverse, endMode, true);
    }

    public void start(View view, LayoutSize parentSize, LayoutSize originalSize, AXAnimation animation, boolean reverse, boolean endMode, boolean repeat) {
        this.animation = animation;
        running = true;
        paused = false;
        end = endMode;
        playTime = 0;
        reverseDelay = 0;
        layouts.clear();
        animators.clear();
        this.reverse = reverse;
        indexes[0] = 0;
        indexes[1] = 0;
        this.targetView = view;

        if (repeat) {
            repeatCount = animation.repeatCount;
            repeatMode = animation.repeatMode;
        }

        AXAnimationSaver.run(view, animation);

        LayoutSize targetSize = new LayoutSize(originalSize);
        if (view.getParent() != null && view.getParent() instanceof AnimatedLayout) {
            AnimatedLayoutParams lp = new AnimatedLayoutParams(view.getLayoutParams(), targetSize);
            lp.originalLayout = originalSize.clone();
            view.setLayoutParams(lp);
        }
        this.targetSize = targetSize;

        for (AXAnimatorListener listener : listeners)
            listener.onAnimationStart(animation);
        start(view, parentSize, originalSize, animation, 0);
    }

    private void start(final View view, final LayoutSize parentSize, final LayoutSize originalSize, final AXAnimation a, final int index) {
        if (indexes[0] != index)
            indexes[1] = 0;

        indexes[0] = index;
        layoutSizes[0] = parentSize;
        layoutSizes[1] = originalSize;
        if (!running)
            return;

        if (index == a.rules.size()) {
            done(view, a);
            return;
        }

        int ri = reverse ? a.rules.size() - index - 1 : index;
        final RuleSection section = a.rules.get(ri);
        section.debug(view, targetSize, originalSize, parentSize, a);
        section.onStart(a);

        final RuleSection info = section instanceof RuleSectionWrapper ?
                ((RuleSectionWrapper) section).getRuleSection() : section;

        for (AXAnimatorListener listener : listeners)
            listener.onRuleSectionChanged(a, section);

        List<LiveVarUpdater> updaters = a.getALlLiveVarUpdaters();
        if (updaters.size() > 0) {
            int realSectionIndex = -1;
            for (int s = 0; s <= index; s++) {
                if (!(a.getRuleSection(s) instanceof WaitRule))
                    realSectionIndex++;
            }
            if (reverse)
                realSectionIndex = a.getRealRuleSectionCount() - realSectionIndex;
            realSectionIndex = Math.max(realSectionIndex, 0);

            for (LiveVarUpdater updater : updaters) {
                updater.update(a, ri, realSectionIndex, section);
            }
        }

        boolean hasCustomPlayTime = targetIndexes[0] == index && customPlayTime > 0;

        if (info instanceof WaitNotifyRule) {
            resetLastAnimator();
            long d = ((WaitNotifyRule) info).duration;
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (((WaitNotifyRule) info).isDone(view)) {
                        playTime += ((WaitNotifyRule) info).duration;
                        section.onEnd(a);
                        start(view, parentSize, targetSize.clone(), a, index + 1);
                    } else {
                        view.postDelayed(this, ((WaitNotifyRule) info).duration);
                    }
                }
            }, hasCustomPlayTime ? d - customPlayTime : d);
            return;
        } else if (info instanceof ReverseWaitRule) {
            resetLastAnimator();
            start(view, parentSize, targetSize.clone(), a, index + 1);
            return;
        } else if (info instanceof WaitRule) {
            resetLastAnimator();
            lastAnimator = ((WaitRule) info).createAnimator();
            lastAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator a1) {
                    super.onAnimationEnd(a1);
                    playTime += a1.getDuration() + a1.getStartDelay();
                    resetLastAnimator();
                    section.onEnd(a);
                    start(view, parentSize, targetSize.clone(), a, index + 1);
                }
            });
            if (hasCustomPlayTime)
                ((ValueAnimator) lastAnimator).setCurrentPlayTime(customPlayTime);

            if (validateEnd())
                lastAnimator.end();
            else
                lastAnimator.start();
            return;
        }

        if (info.getRules() == null)
            throw new NullPointerException("Rules can't be null!");

        if (originalSize != null && !layouts.contains(originalSize))
            layouts.add(originalSize);

        if (info.getAnimatorValues() != null && info.getAnimatorValues().isClearOldInspectEnabled())
            InspectUtils.clearInspect(view);

        int rulesCount = info.getRules().length;
        startRule(view, parentSize, originalSize, a, info, section, rulesCount, indexes[1], index);
    }

    private Rule<?> lastRule;
    private Animator lastAnimator;
    private long durationOfLastAnimator = 0;

    private void startRule(final View view, final LayoutSize parentSize, final LayoutSize originalSize, final AXAnimation a, final RuleSection info, final RuleSection main, final int max, final int index, final int index2) {
        startRule(view, parentSize, originalSize, a, info, main, max, index, index2, true);
    }

    private void startRule(final View view, final LayoutSize parentSize, final LayoutSize originalSize, final AXAnimation a, final RuleSection info, final RuleSection main, final int max, final int index, final int index2, final boolean ready) {
        indexes[1] = index;
        if (!running)
            return;

        if (max == index) {
            if (lastAnimator != null) {
                if (!validateEnd() && a.rules.size() > index2 + 1 && a.getRuleSection(index2 + 1) instanceof ReverseWaitRule) {
                    long rd = ((ReverseWaitRule) a.getRuleSection(index2 + 1)).duration;
                    reverseDelay += rd;

                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetLastAnimator();
                            main.onEnd(a);
                            start(view, parentSize, targetSize.clone(), a, index2 + 1);
                        }
                    }, durationOfLastAnimator - rd);

                    lastAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator a1) {
                            super.onAnimationEnd(a1);
                            playTime += a1.getDuration() + a1.getStartDelay();
                        }
                    });
                } else {
                    if (validateEnd()) {
                        playTime += lastAnimator.getDuration() + lastAnimator.getStartDelay();
                        main.onEnd(a);
                        resetLastAnimator();
                        start(view, parentSize, targetSize.clone(), a, index2 + 1);
                    } else {
                        lastAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator a1) {
                                super.onAnimationEnd(a1);
                                playTime += a1.getDuration() + a1.getStartDelay();
                                resetLastAnimator();
                                main.onEnd(a);
                                start(view, parentSize, targetSize.clone(), a, index2 + 1);
                            }
                        });
                    }
                }
            } else {
                /* Couldn't get the animator which has the max duration */
                main.onEnd(a);
                start(view, parentSize, targetSize.clone(), a, index2 + 1);
            }
            return;
        }

        Rule<?> rule = info.getRules()[reverse ? info.getRules().length - 1 - index : index];

        if (rule instanceof SkippedRule) {
            startRule(view, parentSize, originalSize, a, info, main, max, index + 1, index2);
            return;
        }

        rule.getFromLiveData();
        boolean r = rule.shouldReverseAnimator(reverse);
        rule.setStartedAsReverse(r);
        if (ready) {
            rule.getReady(view);
            rule.getReady(layouts);
        }

        if (rule.shouldWait() >= 0) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (running)
                        startRule(view, parentSize, originalSize, a, info, main, max, index, index2, false);
                }
            }, rule.shouldWait());
        } else {
            final Animator animator = rule.onCreateAnimator(view, targetSize, originalSize, parentSize);
            rule.debug(view, targetSize, originalSize, parentSize);
            if (animator == null)
                rule.debug(null);

            if (animator != null) {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animators.remove(animator);
                    }
                });

                AXAnimatorData animatorValues = rule.getAnimatorValues();
                if (animatorValues == null)
                    animatorValues = info.getAnimatorValues();

                if (animatorValues != null) {
                    animatorValues.apply(animator);
                } else {
                    animator.setDuration(300);
                }

                if (index2 >= 0) {
                    if (lastAnimator == null || getTotalDuration(animator) >= durationOfLastAnimator) {
                        durationOfLastAnimator = getTotalDuration(animator);
                        lastAnimator = animator;
                        lastRule = rule;
                    }
                }

                rule.onBindAnimator(view, animator);
                rule.debug(animator);

                if (targetIndexes[0] == index2 && customPlayTime > 0) {
                    rule.setCurrentPlayTime(animator, customPlayTime);
                }

                if (validateEnd()) {
                    animator.end();
                } else {
                    if (r) {
                        if (animator instanceof ValueAnimator)
                            ((ValueAnimator) animator).reverse();
                        else
                            animator.start();
                    } else {
                        animator.start();
                    }
                }
                animators.add(animator);
            }
            if (rule.isRuleSet()) {
                RuleSection info2 = new RuleSection(rule.createRules(), rule.getAnimatorValues());
                if (info2.getRules() != null && info2.getRules().length > 0)
                    startRule(view, parentSize, originalSize, a, info2, main, info2.getRules().length, 0, index2);
            }
            startRule(view, parentSize, originalSize, a, info, main, max, index + 1, index2);
        }
    }

    private void done(View view, AXAnimation a) {
        if (repeat()) {
            running = false;
            paused = false;
            AXAnimationSaver.clear(view, a);

            if (a.getTargetLayoutParams() != null) {
                view.setLayoutParams(a.getTargetLayoutParams());
            } else if (view.getLayoutParams() instanceof AnimatedLayoutParams) {
                AnimatedLayoutParams lp = (AnimatedLayoutParams) view.getLayoutParams();
                if (lp.originalLayout.equals(lp))
                    view.setLayoutParams(lp.original);
            }

            for (AXAnimatorListener listener : listeners)
                listener.onAnimationEnd(a);
        } else {
            for (AXAnimatorListener listener : listeners)
                listener.onAnimationRepeat(a);
        }
    }

    public long getTotalDuration(Animator animator) {
        return animator.getDuration() + animator.getStartDelay();
    }

    public long getTotalDuration(AXAnimation animation) {
        long max = 0;
        for (RuleSection section : animation.rules) {
            if (section instanceof RuleSectionWrapper) {
                max += getTotalDuration(((RuleSectionWrapper) section).getRuleSection());
            } else {
                max += getTotalDuration(section);
            }
        }
        return max;
    }

    public long getTotalDuration(RuleSection section) {
        if (section instanceof ReverseWaitRule)
            return -((ReverseWaitRule) section).duration;
        if (section instanceof WaitRule)
            return ((WaitRule) section).duration;

        if (section == null || section.getRules() == null) return 0;
        long max = 0;
        for (Rule<?> rule : section.getRules()) {
            long d = 100;
            if (rule.getAnimatorValues() != null) {
                d = rule.getAnimatorValues().getTotalDuration();
            } else if (section.getAnimatorValues() != null) {
                d = section.getAnimatorValues().getTotalDuration();
            }
            max = Math.max(max, d);
        }
        return max;
    }

    private void resetLastAnimator() {
        lastAnimator = null;
        lastRule = null;
    }

    public long getCurrentPlayTime() {
        if (lastRule != null)
            return playTime + lastRule.getCurrentPlayTime(lastAnimator) - reverseDelay;
        return playTime;
    }

    public void setCurrentPlayTime(long time) {
        boolean run = isRunning();

        if (isRunning()) {
            cancel();
            animators.clear();
        }

        boolean ok = false;
        long max = 0;
        for (int i = 0; i < animation.rules.size(); i++) {
            RuleSection section = animation.getRuleSection(i);

            max += getTotalDuration(section);
            if (max >= time) {
                ok = true;
                targetIndexes[0] = i;
                targetIndexes[1] = 0;
                customPlayTime = max - time;
                if (indexes[0] > i || indexes[0] == i && indexes[1] == 0) {
                    indexes[0] = i;
                    indexes[1] = 0;
                    customPlayTime = 0;
                } else if (indexes[0] == i && indexes[1] > 0) {
                    indexes[1] = 0;
                } else {
                    needsEndFirst = true;
                }
                break;
            }
        }

        if (run)
            resume();

        if (!ok) {
            end();
        } else if (run) {
            start(targetView, layoutSizes[0], layoutSizes[1], animation, indexes[0]);
        }
    }

    private boolean validateEnd() {
        if (end)
            return true;
        if (needsEndFirst) {
            if (targetIndexes[0] > indexes[0]) {
                return true;
            }
        }
        needsEndFirst = false;
        return false;
    }

    public float getAnimatedFraction(AXAnimation animation) {
        return (float) getCurrentPlayTime() / (float) getTotalDuration(animation);
    }

    static boolean hasLayoutRule(AXAnimation animation) {
        for (RuleSection section : animation.rules) {
            if (section.getRules() != null) {
                for (Rule<?> rule : section.getRules()) {
                    if (rule.isLayoutSizeNecessary())
                        return true;
                }
            }
        }
        for (PreRule preRule : animation.preRules) {
            if (preRule.isLayoutSizeNecessary())
                return true;
        }
        return false;
    }

    static boolean hasInspect(AXAnimation animation) {
        for (RuleSection section : animation.rules) {
            if (section.getAnimatorValues() != null && section.getAnimatorValues().isInspectEnabled())
                return true;

            if (section.getRules() != null) {
                for (Rule<?> rule : section.getRules()) {
                    if (rule.getAnimatorValues() != null && rule.getAnimatorValues().isInspectEnabled())
                        return true;
                }
            }
        }
        return false;
    }
}
