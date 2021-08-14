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

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A {@link RuleSectionWrapper} to debug the rule section.
 *
 * @author AmirHossein Aghajari
 */
public class DebugRuleSectionWrapper extends RuleSectionWrapper {

    public DebugRuleSectionWrapper(@NonNull RuleSection section) {
        super(section);

        log = "DebugRuleSectionWrapper: Section Created! \n" + "<-- " + getKey() + "\n";

        Rule<?>[] rules = section.getRules();
        if (rules != null && rules.length > 0) {
            for (int i = 1; i <= rules.length; i++) {
                Rule<?> rule = rules[i - 1];
                if (rule != null)
                    log(i + ". " + rule.getRuleName(), false);
            }
        } else {
            log("NO RULE", false);
        }
        done();
    }

    @Override
    public void onStart(AXAnimation animation) {
        super.onStart(animation);
        log("onStart", true);
    }

    @Override
    public void onEnd(AXAnimation animation) {
        super.onEnd(animation);
        log("onEnd", true);
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize,
                      AXAnimation animation) {
        super.debug(view, target, original, parentSize, animation);
        begin();

        if (getAnimatorValues() != null && getAnimatorValues().isClearOldInspectEnabled())
            log("ClearOldInspect: Enabled", false);

        if (getRuleSection() instanceof WaitRule)
            log("WaitRule: YES", false);

        log("TotalDuration: " + animation.getRuleSectionTotalDuration(getRuleSection()) + "ms", false);

        done();
    }

    // Logger

    protected String log;

    protected String getKey() {
        return getSectionName() + "@" + getRuleSection().hashCode();
    }

    protected void begin() {
        log = "DebugRuleSectionWrapper: \n" + "<-- " + getKey() + "\n";
    }

    protected void log(String log, boolean print) {
        log(log, true, print);
    }

    protected void log(String log, boolean attachKey, boolean print) {
        if (print) {
            if (attachKey) {
                Log.d("AXAnimation", getKey() + ": " + log);
            } else {
                Log.d("AXAnimation", log);
            }
        } else {
            this.log += "   " + log + "\n";
        }
    }

    protected void done() {
        log += "<-- END SECTION DEBUG " + getKey();
        log(this.log.trim(), false, true);
    }

}
