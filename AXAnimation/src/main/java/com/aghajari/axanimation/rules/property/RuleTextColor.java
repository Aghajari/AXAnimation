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
package com.aghajari.axanimation.rules.property;

import android.animation.ArgbEvaluator;
import android.view.View;
import android.widget.TextView;

import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.PropertyRule;
import com.aghajari.axanimation.rules.Rule;

/**
 * A {@link Rule} to change TextView's textColor using {@link ArgbEvaluator}
 *
 * @author AmirHossein Aghajari
 * @see TextView#setTextColor(int)
 */
public class RuleTextColor extends PropertyRule<Integer> {

    public RuleTextColor(Integer... data) {
        super("textColor", new ArgbEvaluator(), data);
    }

    public RuleTextColor(LiveVar<Integer[]> data) {
        super("textColor", new ArgbEvaluator(), data);
    }

    @Override
    public Object getStartValue(View view) {
        return ((TextView) view).getCurrentTextColor();
    }

}
