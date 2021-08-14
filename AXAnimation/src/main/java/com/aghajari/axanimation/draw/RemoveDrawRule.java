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
package com.aghajari.axanimation.draw;

import android.view.View;

import com.aghajari.axanimation.rules.NotAnimatedRule;

/**
 * A {@link NotAnimatedRule} to remove a {@link DrawRule}.
 *
 * @author AmirHossein Aghajari
 */
public class RemoveDrawRule extends NotAnimatedRule<String> {

    public RemoveDrawRule(String key) {
        super(key);
    }

    @Override
    public void apply(View targetView) {
        if (DrawHandler.canDraw(targetView, data, true)) {
            View v = DrawHandler.getDrawableView(targetView, data);
            ((DrawableLayout) v).getDrawHandler().remove(data);
            v.invalidate();
        }
    }
}
