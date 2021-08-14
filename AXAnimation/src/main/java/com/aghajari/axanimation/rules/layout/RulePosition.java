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
package com.aghajari.axanimation.rules.layout;

import android.view.View;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.utils.SizeUtils;

/**
 * A {@link RulePositionBase} to move axis of view to the given value.
 *
 * @author AmirHossein Aghajari
 */
public class RulePosition extends RulePositionBase<Integer> {

    public RulePosition(int gravity, boolean lockedWidth, boolean lockedHeight, int data) {
        super(gravity, lockedWidth, lockedHeight, data);
    }

    protected int calculate(View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        return SizeUtils.calculate(data, view.getMeasuredWidth(), view.getMeasuredHeight(),
                parentSize, target, original, gravity);
    }

}
