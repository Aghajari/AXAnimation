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
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.evaluator.LayoutSizeEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Rule;

import java.util.List;

/**
 * A {@link Rule} to change View's Layout using {@link LayoutSizeEvaluator}
 *
 * @see View#setLayoutParams(ViewGroup.LayoutParams)
 * @author AmirHossein Aghajari
 */
public class RulePreviousLayout extends RuleLayoutParams {

    private final int section;

    public RulePreviousLayout(int section) {
        super(null);
        this.section = section;
    }

    @Override
    public void getReady(@NonNull View view) {
    }

    @Override
    public void getReady(@NonNull List<LayoutSize> layouts) {
        int index = section;
        if (index < 0)
            index = layouts.size() + index;
        index = Math.max(index, 0);
        if (layouts.size() <= index)
            throw new RuntimeException("Couldn't find section's layout! index=" + index + " size=" + layouts.size());
        targetLayoutSize = layouts.get(index);
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }
}
