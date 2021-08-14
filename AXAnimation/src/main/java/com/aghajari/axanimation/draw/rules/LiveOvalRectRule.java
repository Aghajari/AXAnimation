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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A {@link DrawRule} to draw an oval.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawOval(RectF, Paint)
 */
public class LiveOvalRectRule extends LiveRectRule {

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rectangle bounds of the oval to be drawn. (Animator values)
     */
    public LiveOvalRectRule(Paint paint, String key, boolean drawOnFront, int gravity, LayoutSize... values) {
        super(paint, key, drawOnFront, gravity, values);
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);

        if (animatedValue != null && !animatedValue.isEmpty())
            canvas.drawOval(animatedValue.getRectF(), getPaint());
    }
}