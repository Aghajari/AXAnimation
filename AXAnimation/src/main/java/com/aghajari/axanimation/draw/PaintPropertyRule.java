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

import android.animation.TypeEvaluator;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.PropertyRule;

/**
 * A {@link PropertyRule} to set a {@link Paint} property.
 *
 * @author AmirHossein Aghajari
 */
public class PaintPropertyRule<T> extends PropertyRule<T> {

    private final Paint target;
    private final boolean resetAtTheEnd;

    @SafeVarargs
    public PaintPropertyRule(Paint target, String property, boolean resetAtTheEnd, @Nullable T... data) {
        super(property, data);
        this.target = target;
        this.resetAtTheEnd = resetAtTheEnd;
    }

    @SafeVarargs
    public PaintPropertyRule(Paint target, String property, boolean resetAtTheEnd, TypeEvaluator<?> evaluator, @Nullable T... data) {
        super(property, evaluator, data);
        this.target = target;
        this.resetAtTheEnd = resetAtTheEnd;
    }

    public PaintPropertyRule(Paint target, String property, boolean resetAtTheEnd, @NonNull LiveVar<T[]> data) {
        super(property, data);
        this.target = target;
        this.resetAtTheEnd = resetAtTheEnd;
    }

    public PaintPropertyRule(Paint target, String property, boolean resetAtTheEnd, TypeEvaluator<?> evaluator, @NonNull LiveVar<T[]> data) {
        super(property, evaluator, data);
        this.target = target;
        this.resetAtTheEnd = resetAtTheEnd;
    }

    @Override
    protected boolean shouldResetWhenDone() {
        return resetAtTheEnd;
    }

    @Override
    protected Object getTarget(@NonNull View view) {
        return target;
    }
}
