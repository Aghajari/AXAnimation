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

import android.view.View;
import android.view.ViewGroup;

import com.aghajari.axanimation.rules.NotAnimatedRule;

/**
 * A {@link NotAnimatedRule} to send view to back
 *
 * @author AmirHossein Aghajari
 */
public class RuleSendToBack extends NotAnimatedRule<Void> {

    public RuleSendToBack() {
        super(null);
    }

    public RuleSendToBack(int viewID) {
        super(viewID, null);
    }

    public RuleSendToBack(View view) {
        super(view, null);
    }

    @Override
    public void apply(View view) {
        if (view.getParent() != null) {
            final ViewGroup parent = (ViewGroup) view.getParent();
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            parent.removeView(view);
            parent.addView(view, 0);
            // You have too reset layoutParams here
            // ViewGroup won't support AnimatedLayoutParams
            // So you have to set it again here
            view.setLayoutParams(lp);
        }
    }
}
