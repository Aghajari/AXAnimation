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
package com.aghajari.axanimation.livevar;

import android.graphics.PointF;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * It's a {@link android.graphics.PointF} with LiveSize!
 *
 * @author AmirHossein Aghajari
 */
public class LiveSizePoint implements LiveSizeDebugger {

    private float xValue;
    private float yValue;
    public LiveSize x;
    public LiveSize y;

    public LiveSizePoint(LiveSize x, LiveSize y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the point's x and y coordinates
     */
    public void set(LiveSize x, LiveSize y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Call this before using {@link #getPointF()}
     */
    public void prepare(int viewWidth, int viewHeight,
                        LayoutSize parent, LayoutSize target, LayoutSize original) {
        xValue = x.calculate(viewWidth, viewHeight, parent, target, original, Gravity.CENTER_HORIZONTAL);
        yValue = y.calculate(viewWidth, viewHeight, parent, target, original, Gravity.CENTER_VERTICAL);
    }

    public PointF getPointF() {
        return new PointF(xValue, yValue);
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(x, y, view, Gravity.CENTER);
    }
}
