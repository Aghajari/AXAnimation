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
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.draw.DrawRule;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.livevar.LiveSizeDebugHelper;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;
import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.utils.SizeUtils;

import java.util.Map;

/**
 * A {@link DrawRule} to draw a text.
 *
 * @author AmirHossein Aghajari
 * @see Canvas#drawText(CharSequence, int, int, float, float, Paint)
 */
public class TextRule extends DrawRule<CharSequence, Void, Float> implements LiveSizeDebugger {

    private final int gravity;
    private float x, y;
    private final LiveSize liveX, liveY;
    private final Rect rect = new Rect();
    private final boolean typingAnimation;

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param typing      True if should type it during animation, False otherwise.
     * @param gravity     Gravity of the origin corner.
     * @param x           The x-coordinate of origin for where to draw the text
     * @param y           The y-coordinate of origin for where to draw the text
     * @param text        The text to be drawn
     */
    public TextRule(Paint paint, String key, boolean drawOnFront, boolean typing, int gravity, float x, float y, CharSequence text) {
        super(paint, key, drawOnFront, text);
        this.gravity = gravity == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravity;
        this.x = x;
        this.y = y;
        this.liveX = null;
        this.liveY = null;
        this.typingAnimation = typing;
    }

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param typing      True if should type it during animation, False otherwise.
     * @param gravity     Gravity of the origin corner.
     * @param x           The x-coordinate of origin for where to draw the text
     * @param y           The y-coordinate of origin for where to draw the text
     * @param text        The text to be drawn
     */
    public TextRule(Paint paint, String key, boolean drawOnFront, boolean typing, int gravity, LiveSize x, LiveSize y, CharSequence text) {
        super(paint, key, drawOnFront, text);
        this.gravity = gravity == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravity;
        this.liveX = x;
        this.liveY = y;
        this.typingAnimation = typing;
    }

    public TextRule(Paint paint, String key, boolean drawOnFront, boolean typing, int gravity, float x, float y, LiveVar<CharSequence> text) {
        super(paint, key, drawOnFront, text);
        this.gravity = gravity == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravity;
        this.x = x;
        this.y = y;
        this.liveX = null;
        this.liveY = null;
        this.typingAnimation = typing;
    }

    public TextRule(Paint paint, String key, boolean drawOnFront, boolean typing, int gravity, LiveSize x, LiveSize y, LiveVar<CharSequence> text) {
        super(paint, key, drawOnFront, text);
        this.gravity = gravity == Gravity.NO_GRAVITY ? Gravity.TOP | Gravity.LEFT : gravity;
        this.liveX = x;
        this.liveY = y;
        this.typingAnimation = typing;
    }

    private final PointF pointF = new PointF();

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        super.onDraw(target, canvas);
        Paint paint = getPaint();
        getPoint(paint, pointF);
        canvas.drawText(data, 0, getLength(data.length()), pointF.x, pointF.y, paint);
    }

    protected void getPoint(Paint paint, PointF pointF) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            paint.getTextBounds(data, 0, getLength(data.length()), rect);
        } else {
            String t = data.toString();
            paint.getTextBounds(t, 0, getLength(t.length()), rect);
        }

        float x = this.x, y = this.y;
        final int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        switch (hg) {
            case Gravity.RIGHT:
                x -= rect.width();
                break;
            case Gravity.CENTER_HORIZONTAL:
                x -= rect.centerX();
                break;
        }

        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        switch (verticalGravity) {
            case Gravity.BOTTOM:
                y -= rect.bottom;
                break;
            case Gravity.CENTER_VERTICAL:
                y -= rect.centerY();
                break;
        }

        pointF.set(x, y);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(@NonNull View view,
                                     @Nullable LayoutSize target,
                                     @Nullable LayoutSize original,
                                     @Nullable LayoutSize parentSize) {
        int vw = view.getMeasuredWidth();
        int vh = view.getMeasuredWidth();

        if (liveX == null) {
            x = SizeUtils.calculate(x, vw, vh, parentSize, target, original, gravity & Gravity.HORIZONTAL_GRAVITY_MASK);
        } else {
            x = liveX.calculate(vw, vh, parentSize, target, original, gravity & Gravity.HORIZONTAL_GRAVITY_MASK);
        }
        if (liveY == null) {
            y = SizeUtils.calculate(y, vw, vh, parentSize, target, original, gravity & Gravity.VERTICAL_GRAVITY_MASK);
        } else {
            y = liveY.calculate(vw, vh, parentSize, target, original, gravity & Gravity.VERTICAL_GRAVITY_MASK);
        }

        return ValueAnimator.ofFloat(0, 1);
    }

    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(liveX, liveY, view, gravity);
    }

    protected int getLength(int len){
        if (typingAnimation)
            return (int) (len * animatedValue);
        return len;
    }
}