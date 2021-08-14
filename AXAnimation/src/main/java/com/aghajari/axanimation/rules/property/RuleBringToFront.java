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

import com.aghajari.axanimation.rules.NotAnimatedRule;


// Well, you can easily use reflection.InvokeRule:
// new InvokeRule("bringToFront");
// Or AXAnimation....invoke("bringToFront")...
// But this one is faster, so i'm going to keep using this one for now.

/**
 * A {@link NotAnimatedRule} to bring view to front
 *
 * @author AmirHossein Aghajari
 * @see View#bringToFront()
 */
public class RuleBringToFront extends NotAnimatedRule<Void> {

    public RuleBringToFront() {
        super(null);
    }

    public RuleBringToFront(int viewID) {
        super(viewID, null);
    }

    public RuleBringToFront(View view) {
        super(view, null);
    }

    @Override
    public void apply(View view) {
        view.bringToFront();
    }

}
