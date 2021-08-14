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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * RuleSet is just a {@link Rule} which can contains multi {@link Rule}s.
 *
 * @author AmirHossein Aghajari
 */
public abstract class RuleSet<T> extends Rule<T> {

    public RuleSet(@Nullable T data) {
        super(data);
    }

    @Override
    public Animator onCreateAnimator(@NonNull View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        return null;
    }

    @Override
    @Nullable
    public abstract Rule<?>[] createRules();

    @Override
    public boolean isRuleSet() {
        return true;
    }
}