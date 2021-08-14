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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;

import java.util.Collections;
import java.util.Map;

/**
 * A {@link RulePositionBase} to move axis of view to the given LiveSize.
 *
 * @author AmirHossein Aghajari
 * @see LiveSize
 */
public class RuleLivePosition extends RulePositionBase<LiveSize> implements LiveSizeDebugger {

    private final RuleLiveSize.LiveSizeHandler handler = new RuleLiveSize.LiveSizeHandler();

    public RuleLivePosition(int gravity, boolean lockedWidth, boolean lockedHeight, LiveSize data) {
        super(gravity, lockedWidth, lockedHeight, data);
    }

    protected int calculate(View view, LayoutSize target, LayoutSize original, LayoutSize parentSize) {
        return (int) data.calculate(view.getMeasuredWidth(), view.getMeasuredHeight(),
                parentSize, target, original, gravity);
    }

    @Override
    public long shouldWait() {
        return handler.shouldWait();
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);
        handler.getReady(view, Collections.singletonList(data));
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize) {
        super.debug(view, target, original, parentSize);
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            handler.debug(view, target, original, parentSize, gravity);
        }
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(data, view, gravity);
    }
}
