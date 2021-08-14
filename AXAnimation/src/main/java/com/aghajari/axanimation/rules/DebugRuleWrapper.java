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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;

import java.util.Arrays;
import java.util.Map;

/**
 * A {@link RuleWrapper} to debug the rule.
 *
 * @author AmirHossein Aghajari
 */
public class DebugRuleWrapper extends RuleWrapper {

    public DebugRuleWrapper(@NonNull Rule<?> data) {
        super(data);
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize) {
        super.debug(view, target, original, parentSize);

        begin();

        if (isLayoutSizeNecessary())
            log("LayoutSize: Necessary", false);

        if (getAnimatorValues() != null && getAnimatorValues().isInspectEnabled())
            log("Inspect: Enabled", false);

        if (data instanceof LiveSizeDebugger) {
            Map<String, String> debugLiveSize = ((LiveSizeDebugger) data).debugLiveSize(view);
            if (debugLiveSize != null && debugLiveSize.size() > 0) {
                log("LiveSize: " + debugLiveSize.size(), false);
                for (String key : debugLiveSize.keySet()) {
                    log(key + ": " + debugLiveSize.get(key), false);
                }
            } else {
                log("LiveSize: " + "Supports", false);
            }
        }

        if (data instanceof Debugger) {
            Map<String, String> debugValues = ((Debugger) data).debugValues(view);
            for (String key : debugValues.keySet()) {
                log(key + ": " + debugValues.get(key), false);
            }
        }
    }

    @Override
    public void debug(@Nullable final Animator animator) {
        super.debug(animator);

        if (animator == null) {
            log("Animator: NULL", false);
        } else {

            if (animator instanceof ObjectAnimator)
                log("PropertyName: " + ((ObjectAnimator) animator).getPropertyName(), false);

            if (getAnimatorValues() != null && !getAnimatorValues().isFirstValueFromView())
                log("FirstValueFromView: Disabled", false);

            if (animator instanceof ValueAnimator)
                log("AnimatorValues: " + Arrays.toString(((ValueAnimator) animator).getValues()), false);

            debugAnimator(animator);

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    log("onAnimationCancel", true);
                    animator.removeListener(this);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    log("onAnimationEnd", true);
                    animator.removeListener(this);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    log("onAnimationStart", true);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                    log("onAnimationPause", true);
                }

                @Override
                public void onAnimationResume(Animator animation) {
                    super.onAnimationResume(animation);
                    log("onAnimationResume", true);
                }
            });
        }

        done();
    }

    // Logger

    protected void debugAnimator(Animator animator) {
        log("Duration: " + animator.getDuration(), false);
        log("StartDelay: " + animator.getStartDelay(), false);
        log("Interpolator: " + debugInterpolator(animator.getInterpolator()), false);

        Class<?> cls = data.getEvaluatorClass();
        if (cls != null)
            log("Evaluator: " + cls.getSimpleName(), false);

        if (animator instanceof ValueAnimator) {
            log("RepeatCount: " + ((ValueAnimator) animator).getRepeatCount(), false);

            if (((ValueAnimator) animator).getRepeatCount() != 0) {
                log("RepeatMode: " + (((ValueAnimator) animator).getRepeatMode() == ValueAnimator.REVERSE
                        ? "Reverse" : "Restart"), false);
            }
        }
    }

    protected String debugInterpolator(TimeInterpolator interpolator) {
        if (interpolator == null)
            return "null";
        return interpolator.getClass().getSimpleName();
    }

    protected String log;

    protected String getKey() {
        return getRuleName() + "@" + data.hashCode();
    }

    protected void begin() {
        log = "DebugRuleWrapper: \n" + "--> " + getKey() + "\n";

        if (data.getData() != null) {
            debugData("Data", data.getData());
        }
        if (data instanceof RuleWithTmpData) {
            if (((RuleWithTmpData<?, ?>) data).tmpData != null) {
                //noinspection ConstantConditions
                debugData("TmpData", ((RuleWithTmpData<?, ?>) data).tmpData);
            }
        }

        if (data.isReverse())
            log("Reverse: True", false);
        if (data.isRuleSet())
            log("RuleSet: True", false);
    }

    private void debugData(String name, Object data) {
        if (!data.getClass().isArray()) {
            log(name + ": " + data.toString(), false);
            return;
        }

        Class<?> cls = data.getClass();
        if (cls == int[].class) {
            Class<?> clsEvaluator = this.data.getEvaluatorClass();
            if (clsEvaluator != null && clsEvaluator.isAssignableFrom(ArgbEvaluator.class)) {
                log(name + ": " + toStringColors((int[]) data), false);
            } else {
                log(name + ": " + Arrays.toString((int[]) data), false);
            }
        } else if (cls == float[].class) {
            log(name + ": " + Arrays.toString((float[]) data), false);
        } else if (cls == char[].class) {
            log(name + ": " + Arrays.toString((char[]) data), false);
        } else if (cls == byte[].class) {
            log(name + ": " + Arrays.toString((byte[]) data), false);
        } else if (cls == long[].class) {
            log(name + ": " + Arrays.toString((long[]) data), false);
        } else if (cls == double[].class) {
            log(name + ": " + Arrays.toString((double[]) data), false);
        } else if (cls == short[].class) {
            log(name + ": " + Arrays.toString((short[]) data), false);
        } else if (cls == boolean[].class) {
            log(name + ": " + Arrays.toString((boolean[]) data), false);
        } else {
            log(name + ": " + Arrays.toString((Object[]) data), false);
        }
    }

    private static String toStringColors(int[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            int c = a[i];
            b.append("RGB(")
                    .append(Color.red(c))
                    .append(",")
                    .append(Color.green(c))
                    .append(",")
                    .append(Color.blue(c))
                    .append(")");
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    protected void log(String log, boolean print) {
        log(log, true, print);
    }

    protected void log(String log, boolean attachKey, boolean print) {
        if (print) {
            if (attachKey) {
                log(getKey() + ": " + log);
            } else {
                log(log);
            }
        } else {
            this.log += "   " + log + "\n";
        }
    }

    protected void done() {
        log += "--> END RULE DEBUG " + getKey();
        log(this.log.trim(), false, true);
    }

    protected void log(String log) {
        Log.d("AXAnimation", log);
    }

}
