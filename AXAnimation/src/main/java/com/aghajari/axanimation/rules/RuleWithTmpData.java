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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LiveVar;

/**
 * A {@link Rule} which contains a Non-Cloneable tmpData
 *
 * @param <T> {@link Rule}'s type
 * @param <V> {@link RuleWithTmpData#tmpData}'s type
 * @author AmirHossein Aghajari
 */
public abstract class RuleWithTmpData<T, V> extends Rule<T> implements Cloneable {
    @Nullable
    protected V tmpData;

    public RuleWithTmpData(@Nullable T data) {
        super(data);
    }

    public RuleWithTmpData(@NonNull LiveVar<T> data) {
        super(data);
    }

    @NonNull
    @Override
    public Object clone() {
        RuleWithTmpData<?, ?> d = (RuleWithTmpData<?, ?>) super.clone();
        d.tmpData = null;
        return d;
    }
}