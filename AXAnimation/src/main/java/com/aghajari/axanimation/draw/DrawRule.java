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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.RuleWithTmpData;

/**
 * A custom {@link com.aghajari.axanimation.rules.Rule} to draw something on a {@link DrawableLayout}
 * The view or it's parents must be a {@link DrawableLayout} to be able to draw.
 * There is a specific key for each DrawRule ({@link #getKey()}), the target DrawableLayout
 * must return true on {@link DrawableLayout#canDraw(String)} for it's key before drawing,
 * This will help you to find the correct {@link DrawableLayout} When the view and it's parents
 * all are an instance of DrawableLayout.
 *
 * @param <T> Data type
 * @param <V> tmpData type
 * @param <A> animatedValue type
 * @author AmirHossein Aghajari
 * @see View#dispatchDraw(Canvas)
 */
@SuppressWarnings("JavadocReference")
public abstract class DrawRule<T, V, A> extends RuleWithTmpData<T, V> implements OnDraw {

    protected A animatedValue;
    private final Paint paint;
    private final String key;
    private boolean drawOnFront;

    /**
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param data        the rule animator values.
     */
    public DrawRule(Paint paint, String key, boolean drawOnFront, T data) {
        super(data);
        this.paint = paint;
        this.key = key;
        this.drawOnFront = drawOnFront;
    }

    public DrawRule(Paint paint, String key, boolean drawOnFront, LiveVar<T> data) {
        super(data);
        this.paint = paint;
        this.key = key;
        this.drawOnFront = drawOnFront;
    }

    /**
     * DrawRule calls this whenever the animator updated the value,
     * Update the draw values here.
     *
     * @param target the target view, an instance of {@link DrawableLayout}
     * @param value  the animated value
     */
    protected void updateValue(float fraction, View target, A value) {
        this.animatedValue = value;
    }

    /**
     * @return Returns The paint used to draw
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * @see OnDraw#isDrawingOnFront()
     */
    @Override
    public boolean isDrawingOnFront() {
        return drawOnFront;
    }


    public void setDrawOnFront(boolean drawOnFront) {
        this.drawOnFront = drawOnFront;
    }

    /**
     * @return Returns the specific key for this DrawRule
     */
    public String getKey() {
        return key;
    }

    /**
     * Specifies whether the rule should be on {@link DrawHandler#onDraws}
     * When the rule has ended or not.
     * Usually reverse rules won't need to be on the list when it's done.
     */
    protected boolean shouldRemoveOnEnd() {
        return isStartedAsReverse;
    }

    @Override
    public void onBindAnimator(@NonNull final View view, @NonNull Animator animator) {
        super.onBindAnimator(view, animator);
        if (DrawHandler.canDraw(view, getKey(), true) && animator instanceof ValueAnimator) {

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (shouldRemoveOnEnd())
                        ((DrawableLayout) DrawHandler.getDrawableView(view, getKey())).getDrawHandler().remove(getKey());
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    addToSet(view, false);
                }
            });

            ((ValueAnimator) animator).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    View drawableView = DrawHandler.getDrawableView(view, getKey());
                    //noinspection unchecked
                    updateValue(animation.getAnimatedFraction(), drawableView, (A) animation.getAnimatedValue());
                    drawableView.invalidate();
                }
            });
        }
    }

    protected void addToSet(View view, boolean invalidate) {
        if (DrawHandler.canDraw(view, getKey(), true)) {
            View v = DrawHandler.getDrawableView(view, getKey());
            ((DrawableLayout) v).getDrawHandler().add(getKey(), getListener());
            if (invalidate)
                v.invalidate();
        }
    }

    protected void removeFromSet(View view) {
        if (DrawHandler.canDraw(view, getKey(), true))
            ((DrawableLayout) DrawHandler.getDrawableView(view, getKey()))
                    .getDrawHandler().add(getKey(), getListener());
    }

    protected OnDraw getListener() {
        return this;
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

    @Override
    public void onDraw(DrawableLayout target, Canvas canvas) {
        getFromLiveData();
    }
}
