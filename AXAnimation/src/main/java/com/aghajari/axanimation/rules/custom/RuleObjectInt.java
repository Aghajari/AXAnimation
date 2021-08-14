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
package com.aghajari.axanimation.rules.custom;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.Rule;

/**
 * A custom {@link Rule} using {@link ObjectAnimator}
 *
 * @author AmirHossein Aghajari
 */
public class RuleObjectInt extends Rule<int[]> {
    final String propertyName;

    public RuleObjectInt(String propertyName, int... data) {
        super(data);
        this.propertyName = propertyName;
    }

    public RuleObjectInt(String propertyName, LiveVar<int[]> data) {
        super(data);
        this.propertyName = propertyName;
    }

    @Override
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        return initEvaluator(ObjectAnimator.ofInt(view, propertyName, data));
    }
}
