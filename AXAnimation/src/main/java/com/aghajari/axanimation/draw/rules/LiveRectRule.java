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
package com.aghajari.axanimation.draw.rules;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.evaluator.LayoutSizeEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;

import java.util.Map;

/**
 * A {@link DrawRule} to draw a rect ({@link LayoutSize}).
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawRect(Rect, Paint)
 */
public class LiveRectRule extends DrawRule<LayoutSize[], Object[], LayoutSize> implements LiveSizeDebugger {

    private final int gravity;

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rect to be drawn. (Animator values)
     */
    public LiveRectRule(Paint paint, String key, boolean drawOnFront, int gravity, LayoutSize... values) {
        super(paint, key, drawOnFront, values);

        animatedValue = null;
        this.gravity = gravity;
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (animatedValue != null && !animatedValue.isEmpty())
            canvas.drawRect(animatedValue.getRect(), getPaint());
    }

    @Override
    public Class<?> getEvaluatorClass() {
        return LayoutSizeEvaluator.class;
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            tmpData = new Object[data.length + 1];
            System.arraycopy(data, 0, tmpData, 1, data.length);
            tmpData[0] = null;

            int vw = view.getMeasuredWidth();
            int vh = view.getMeasuredHeight();

            for (Object o : tmpData) {
                if (o == null)
                    continue;

                ((LayoutSize) o).prepare(vw, vh, parentSize, target, original);
            }

            Point point = data[0].getPoint(gravity);
            tmpData[0] = new LayoutSize(point.x, point.y, point.x, point.y);
        }

        return ValueAnimator.ofObject(createEvaluator(), tmpData);
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(view, data);
    }
}