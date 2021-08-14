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
package com.aghajari.axanimation;

import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.aghajari.axanimation.annotation.LineGravity;
import com.aghajari.axanimation.draw.*;
import com.aghajari.axanimation.draw.rules.*;
import com.aghajari.axanimation.layouts.*;
import com.aghajari.axanimation.listener.*;
import com.aghajari.axanimation.livevar.*;
import com.aghajari.axanimation.prerule.*;
import com.aghajari.axanimation.rules.*;
import com.aghajari.axanimation.rules.custom.*;
import com.aghajari.axanimation.rules.layout.*;
import com.aghajari.axanimation.rules.property.*;
import com.aghajari.axanimation.rules.reflect.*;
import com.aghajari.axanimation.rules.transformation.*;
import com.aghajari.axanimation.inspect.InspectLayout;
import com.aghajari.axanimation.utils.SizeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AXAnimation is an Android Library which can simply animate views and everything!
 *
 * @author AmirHossein Aghajari
 * @version 1.0.1
 * @see <a href="https://github.com/Aghajari/AXAnimation">GitHub</a>
 */
@SuppressWarnings({"UnusedReturnValue", "unused", "RtlHardcoded"})
public class AXAnimation implements BaseAnimation, Cloneable {

    // *************** Public Fields ***************

    public final static int PARENT_ID = -1;
    public final static int MATCH_PARENT = -1;
    public final static int WRAP_CONTENT = -2;
    public final static int CONTENT_HEIGHT = -10004;
    public final static int CONTENT_WIDTH = -10002;
    public static final int PARENT = SizeUtils.PARENT;
    public static final int TARGET = SizeUtils.TARGET;
    public static final int ORIGINAL = SizeUtils.ORIGINAL;
    public static final int PARENT_WIDTH = SizeUtils.PARENT | Gravity.FILL_HORIZONTAL;
    public static final int PARENT_HEIGHT = SizeUtils.TARGET | Gravity.FILL_VERTICAL;

    /**
     * @hide
     */
    @IntDef({ValueAnimator.RESTART, ValueAnimator.REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    @interface RepeatMode {
    }

    /**
     * When the rule reaches the end and <code>repeatCount</code> is INFINITE
     * or a positive value, the rule restarts from the beginning.
     */
    public static final int RESTART = ValueAnimator.RESTART;

    /**
     * When the rule reaches the end and <code>repeatCount</code> is INFINITE
     * or a positive value, the rule reverses direction on every iteration.
     */
    public static final int REVERSE = ValueAnimator.REVERSE;

    /**
     * This value used used with the {@link #repeatCount(int)} property to repeat
     * the rule indefinitely.
     */
    public static final int INFINITE = -1;

    // *************** Private Fields ***************

    private final AXAnimatorData data = new AXAnimatorData();
    private final List<Rule<?>> tmpRules = new ArrayList<>();
    private final List<LiveVarUpdater> liveVarUpdaters = new ArrayList<>();
    final List<PreRule> preRules = new ArrayList<>();
    final List<RuleSection> rules = new ArrayList<>();
    private final AXAnimator animator = new AXAnimator();
    private boolean widthLocked = true, heightLocked = true;
    private ViewGroup.LayoutParams targetLayoutParams = null;
    private ViewGroup.LayoutParams originalLayoutParams = null;
    private boolean reverseMode, endMode;
    int repeatCount;
    @RepeatMode
    int repeatMode;

    private float density = 1f;
    private boolean measureUnitEnabled = false;
    private boolean applyNewAnimatorForReverseRules = false;
    private boolean shouldReverseRulesKeepOldData = true;
    private int nextRuleRequiresApi = -1;
    private Class<? extends RuleWrapper> wrapper = null;
    private Class<? extends RuleSectionWrapper> wrapperSection = null;
    private boolean wrapDelays = false;

    private AXAnimatorStartListener sectionStartListener = null;
    private AXAnimatorEndListener sectionEndListener = null;

    private final OnLayoutSizeReadyListener sizeReadyListener = new OnLayoutSizeReadyListener() {
        @Override
        public void onReady(View view, LayoutSize size) {
            start(view, size, reverseMode, endMode);
        }
    };

    // *************** AXAnimation ***************

    private AXAnimation() {
    }

    public static AXAnimation create() {
        return new AXAnimation();
    }

    // *************** X,Y Lock ***************

    /**
     * If x is locked, {@link AXAnimation#toRight(int)} , {@link AXAnimation#toLeft(int)} ,...
     * won't change the width of view!
     *
     * @see AXAnimation#unlockX()
     */
    public AXAnimation lockX() {
        widthLocked = true;
        return this;
    }

    /**
     * If y is locked, {@link AXAnimation#toTop(int)} , {@link AXAnimation#toBottom(int)} ,...
     * won't change the height of view!
     *
     * @see AXAnimation#unlockY()
     */
    public AXAnimation lockY() {
        heightLocked = true;
        return this;
    }

    /**
     * If x is locked, {@link AXAnimation#toRight(int)} , {@link AXAnimation#toLeft(int)} ,...
     * won't change the width of view!
     *
     * @see AXAnimation#lockX()
     */
    public AXAnimation unlockX() {
        widthLocked = false;
        return this;
    }

    /**
     * If y is locked, {@link AXAnimation#toTop(int)} , {@link AXAnimation#toBottom(int)} ,...
     * won't change the height of view!
     *
     * @see AXAnimation#lockY()
     */
    public AXAnimation unlockY() {
        heightLocked = false;
        return this;
    }


    // *************** AXAnimatorData ***************

    /**
     * @return Returns an instance of {@link AXAnimatorData} which
     * contains the next rule animatorValues.
     * @see Rule#setAnimatorValues(AXAnimatorData)
     */
    public AXAnimatorData getNextRuleAnimatorValues() {
        return data;
    }

    /**
     * Sets the duration of next rules.
     *
     * @param duration in milliseconds
     */
    public AXAnimation duration(@IntRange(from = 1) long duration) {
        data.setDuration(duration);
        return this;
    }

    /**
     * Sets the start delay of next rules.
     *
     * @param delay in milliseconds
     */
    public AXAnimation delay(@IntRange(from = 0) long delay) {
        data.setDelay(delay);
        return this;
    }

    /**
     * Increases or decreases (if param was a negative number) the amount of delay
     *
     * @param additionalDelay in milliseconds
     * @see AXAnimation#delay(long)
     */
    public AXAnimation delayPlus(long additionalDelay) {
        return delay(data.getDelay() + additionalDelay);
    }

    /**
     * Specifies whether the rule should add first view's state to animator or not.
     *
     * @param firstValueFromView True if Rules should add first view's state to animator, false otherwise.
     *                           by default it's True
     */
    public AXAnimation firstValueFromView(boolean firstValueFromView) {
        data.setFirstValueFromView(firstValueFromView);
        return this;
    }

    /**
     * Inspect animated view & related views for a better debug.
     */
    public AXAnimation inspect(boolean enabled) {
        data.setInspectEnabled(enabled);
        return this;
    }

    /**
     * Clear old inspect before drawing new section inspect.
     * Only works when section changed.
     */
    public AXAnimation clearOldInspect(boolean enabled) {
        data.setClearOldInspectEnabled(enabled);
        return this;
    }

    /**
     * Sets how many times the rule should be repeated. If the repeat
     * count is 0, the rule is never repeated. If the repeat count is
     * greater than 0 or {@link #INFINITE}, the repeat mode will be taken
     * into account. The repeat count is 0 by default.
     * <p>
     * Note: Only Effects on rule animatorValues.
     *
     * @param count the number of times the rule should be repeated
     */
    public AXAnimation repeatCount(int count) {
        data.setRepeatCount(count);
        return this;
    }

    /**
     * Sets how many times the animation should be repeated. If the repeat
     * count is 0, the animation is never repeated. If the repeat count is
     * greater than 0 or {@link #INFINITE}, the repeat mode will be taken
     * into account. The repeat count is 0 by default.
     * <p>
     * Note: Repeats all animation rules again
     *
     * @param count the number of times the animation should be repeated
     */
    public AXAnimation animationRepeatCount(int count) {
        repeatCount = count;
        return this;
    }

    /**
     * Defines what this rule should do when it reaches the end. This
     * setting is applied only when the repeat count is either greater than
     * 0 or {@link #INFINITE}. Defaults to {@link #RESTART}.
     * <p>
     * Note: Only Effects on rule animatorValues.
     *
     * @param mode {@link #RESTART} or {@link #REVERSE}
     */
    public AXAnimation repeatMode(@RepeatMode int mode) {
        data.setRepeatMode(mode);
        return this;
    }

    /**
     * Defines what this animation should do when it reaches the end. This
     * setting is applied only when the repeat count is either greater than
     * 0 or {@link #INFINITE}. Defaults to {@link #RESTART}.
     * <p>
     * Note: Effects on all animation
     *
     * @param mode {@link #RESTART} or {@link #REVERSE}
     */
    public AXAnimation animationRepeatMode(@RepeatMode int mode) {
        repeatMode = mode;
        return this;
    }

    /**
     * sets the interpolator of next rules.
     * <p>
     * The time interpolator used in calculating the elapsed fraction of this animation. The
     * interpolator determines whether the animation runs with linear or non-linear motion,
     * such as acceleration and deceleration. The default value is
     * {@link android.view.animation.AccelerateDecelerateInterpolator}
     *
     * @param value the interpolator to be used by this animation. A value of <code>null</code>
     *              will result in linear interpolation.
     */
    public AXAnimation interpolator(TimeInterpolator value) {
        data.setInterpolator(value);
        return this;
    }

    /**
     * An interpolator where the rate of change starts out slowly and
     * and then accelerates.
     *
     * @see AccelerateInterpolator
     */
    public AXAnimation accelerateInterpolator() {
        return interpolator(new AccelerateInterpolator());
    }

    /**
     * An interpolator where the rate of change starts out quickly and
     * and then decelerates.
     *
     * @see DecelerateInterpolator
     */
    public AXAnimation decelerateInterpolator() {
        return interpolator(new DecelerateInterpolator());
    }

    /**
     * An interpolator where the rate of change is constant
     *
     * @see LinearInterpolator
     */
    public AXAnimation linearInterpolator() {
        return interpolator(new LinearInterpolator());
    }

    /**
     * An interpolator where the change flings forward and overshoots the last value
     * then comes back.
     *
     * @see OvershootInterpolator
     */
    public AXAnimation overshootInterpolator() {
        return interpolator(new OvershootInterpolator());
    }

    /**
     * An interpolator where the change bounces at the end.
     *
     * @see BounceInterpolator
     */
    public AXAnimation bounceInterpolator() {
        return interpolator(new BounceInterpolator());
    }

    /**
     * Repeats the animation for a specified number of cycles. The
     * rate of change follows a sinusoidal pattern.
     *
     * @see CycleInterpolator
     */
    public AXAnimation cycleInterpolator(float cycles) {
        return interpolator(new CycleInterpolator(cycles));
    }

    /**
     * An interpolator where the rate of change starts and ends slowly but
     * accelerates through the middle.
     *
     * @see AccelerateDecelerateInterpolator
     */
    public AXAnimation accelerateDecelerateInterpolator() {
        return interpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * An interpolator where the change starts backward then flings forward.
     *
     * @see AnticipateInterpolator
     */
    public AXAnimation anticipateInterpolator() {
        return interpolator(new AnticipateInterpolator());
    }

    /**
     * An interpolator where the change starts backward then flings forward and overshoots
     * the target value and finally goes back to the final value.
     *
     * @see AnticipateOvershootInterpolator
     */
    public AXAnimation anticipateOvershootInterpolator() {
        return interpolator(new AnticipateOvershootInterpolator());
    }

    // *************** Transformation Rules ***************

    /**
     * Animates the horizontal location of this view relative to its {@link View#getLeft() left} position.
     * This effectively positions the object post-layout, in addition to wherever the object's
     * layout placed it.
     *
     * @param x Animator values
     * @see View#setTranslationY(float)
     */
    public AXAnimation translationX(Float... x) {
        createRule(PropertyRule.translationX(getValues(x)));
        return this;
    }

    /**
     * Animates the horizontal location of this view relative to its {@link View#getLeft() left} position.
     * This effectively positions the object post-layout, in addition to wherever the object's
     * layout placed it. ({@link LiveVar})
     *
     * @param x Animator values
     * @see View#setTranslationY(float)
     */
    public AXAnimation translationX(LiveVar<Float[]> x) {
        createRule(PropertyRule.translationX(x));
        return this;
    }

    /**
     * Animates the vertical location of this view relative to its {@link View#getTop() top} position.
     * This effectively positions the object post-layout, in addition to wherever the object's
     * layout placed it.
     *
     * @param y Animator values
     * @see View#setTranslationY(float)
     */
    public AXAnimation translationY(Float... y) {
        createRule(PropertyRule.translationY(getValues(y)));
        return this;
    }

    /**
     * Animates the vertical location of this view relative to its {@link View#getTop() top} position.
     * This effectively positions the object post-layout, in addition to wherever the object's
     * layout placed it. ({@link LiveVar})
     *
     * @param y Animator values
     * @see View#setTranslationY(float)
     */
    public AXAnimation translationY(LiveVar<Float[]> y) {
        createRule(PropertyRule.translationY(y));
        return this;
    }

    /**
     * @see #translationX(Float...)
     * @see #translationY(Float...)
     */
    public AXAnimation translation(float x, float y) {
        translationX(x);
        translationY(y);
        return this;
    }

    /**
     * Animates the depth location of this view relative to its {@link View#getElevation() elevation}.
     *
     * @param z Animator values
     * @see View#setTranslationZ(float)
     * @see ViewCompat#setTranslationZ(View, float)
     */
    public AXAnimation translationZ(Float... z) {
        requiresApi(Math.max(nextRuleRequiresApi, Build.VERSION_CODES.LOLLIPOP));
        createRule(PropertyRule.translationZ(getValues(z)));
        return this;
    }

    /**
     * Animates the depth location of this view relative to its {@link View#getElevation() elevation}.
     * ({@link LiveVar})
     *
     * @param z Animator values
     * @see View#setTranslationZ(float)
     * @see ViewCompat#setTranslationZ(View, float)
     */
    public AXAnimation translationZ(LiveVar<Float[]> z) {
        requiresApi(Math.max(nextRuleRequiresApi, Build.VERSION_CODES.LOLLIPOP));
        createRule(PropertyRule.translationZ(z));
        return this;
    }

    /**
     * Animates the visual x position of this view, in pixels. This is equivalent to setting the
     * {@link View#setTranslationX(float) translationX} property to be the difference between
     * the x value passed in and the current {@link View#getLeft() left} property.
     *
     * @param x Animator values
     * @see View#setX(float)
     */
    public AXAnimation x(Float... x) {
        createRule(PropertyRule.x(getValues(x)));
        return this;
    }

    /**
     * Animates the visual x position of this view, in pixels. This is equivalent to setting the
     * {@link View#setTranslationX(float) translationX} property to be the difference between
     * the x value passed in and the current {@link View#getLeft() left} property. ({@link LiveVar})
     *
     * @param x Animator values
     * @see View#setX(float)
     */
    public AXAnimation x(LiveVar<Float[]> x) {
        createRule(PropertyRule.x(x));
        return this;
    }

    /**
     * Animates the visual y position of this view, in pixels. This is equivalent to setting the
     * {@link View#setTranslationY(float) translationY} property to be the difference between
     * the y value passed in and the current {@link View#getTop() top} property.
     *
     * @param y Animator values
     * @see View#setY(float)
     */
    public AXAnimation y(Float... y) {
        createRule(PropertyRule.y(getValues(y)));
        return this;
    }

    /**
     * Animates the visual y position of this view, in pixels. This is equivalent to setting the
     * {@link View#setTranslationY(float) translationY} property to be the difference between
     * the y value passed in and the current {@link View#getTop() top} property. ({@link LiveVar})
     *
     * @param y Animator values
     * @see View#setY(float)
     */
    public AXAnimation y(LiveVar<Float[]> y) {
        createRule(PropertyRule.y(y));
        return this;
    }

    /**
     * Animates the visual z position of this view, in pixels. This is equivalent to setting the
     * {@link View#setTranslationZ(float) translationZ} property to be the difference between
     * the z value passed in and the current {@link View#getElevation() elevation} property.
     *
     * @param z Animator values
     * @see View#setZ(float)
     */
    public AXAnimation z(Float... z) {
        requiresApi(Math.max(nextRuleRequiresApi, Build.VERSION_CODES.LOLLIPOP));
        createRule(PropertyRule.z(getValues(z)));
        return this;
    }

    /**
     * Animates the visual z position of this view, in pixels. This is equivalent to setting the
     * {@link View#setTranslationZ(float) translationZ} property to be the difference between
     * the z value passed in and the current {@link View#getElevation() elevation} property.
     * ({@link LiveVar})
     *
     * @param z Animator values
     * @see View#setZ(float)
     */
    public AXAnimation z(LiveVar<Float[]> z) {
        requiresApi(Math.max(nextRuleRequiresApi, Build.VERSION_CODES.LOLLIPOP));
        createRule(PropertyRule.z(z));
        return this;
    }

    /**
     * @see #x(Float...)
     * @see #y(Float...)
     * @see #z(Float...)
     */
    public AXAnimation xyz(float x, float y, float z) {
        x(x);
        y(y);
        z(z);
        return this;
    }

    /**
     * @see #x(Float...)
     * @see #y(Float...)
     * @see #z(Float...)
     * @see #xyz(float, float, float)
     */
    public AXAnimation xyz(Float[] x, Float[] y, Float[] z) {
        x(x);
        y(y);
        z(z);
        return this;
    }

    /**
     * Animates the x location of the point around which the view is
     * {@link View#setRotation(float) rotated} and {@link View#setScaleX(float) scaled}.
     * By default, the pivot point is centered on the object.
     * Setting this property disables this behavior and causes the view to use only the
     * explicitly set pivotX and pivotY values.
     *
     * @param pivotX Animator values
     * @see View#setPivotX(float)
     */
    public AXAnimation pivotX(Float... pivotX) {
        createRule(PropertyRule.pivotX(getValues(pivotX)));
        return this;
    }

    /**
     * Animates the x location of the point around which the view is
     * {@link View#setRotation(float) rotated} and {@link View#setScaleX(float) scaled}.
     * By default, the pivot point is centered on the object.
     * Setting this property disables this behavior and causes the view to use only the
     * explicitly set pivotX and pivotY values. ({@link LiveVar})
     *
     * @param pivotX Animator values
     * @see View#setPivotX(float)
     */
    public AXAnimation pivotX(LiveVar<Float[]> pivotX) {
        createRule(PropertyRule.pivotX(pivotX));
        return this;
    }

    /**
     * Animates the y location of the point around which the view is {@link View#setRotation(float) rotated}
     * and {@link View#setScaleY(float) scaled}. By default, the pivot point is centered on the object.
     * Setting this property disables this behavior and causes the view to use only the
     * explicitly set pivotX and pivotY values.
     *
     * @param pivotY Animator values
     * @see View#setPivotY(float)
     */
    public AXAnimation pivotY(Float... pivotY) {
        createRule(PropertyRule.pivotY(getValues(pivotY)));
        return this;
    }

    /**
     * Animates the y location of the point around which the view is {@link View#setRotation(float) rotated}
     * and {@link View#setScaleY(float) scaled}. By default, the pivot point is centered on the object.
     * Setting this property disables this behavior and causes the view to use only the
     * explicitly set pivotX and pivotY values. ({@link LiveVar})
     *
     * @param pivotY Animator values
     * @see View#setPivotY(float)
     */
    public AXAnimation pivotY(LiveVar<Float[]> pivotY) {
        createRule(PropertyRule.pivotY(pivotY));
        return this;
    }

    /**
     * Animates the opacity of the view to a value from 0 to 1, where 0 means the view is
     * completely transparent and 1 means the view is completely opaque.
     *
     * @param alpha Animator values
     * @see View#setAlpha(float)
     */
    public AXAnimation alpha(Float... alpha) {
        createRule(PropertyRule.alpha(alpha));
        return this;
    }

    /**
     * Animates the opacity of the view to a value from 0 to 1, where 0 means the view is
     * completely transparent and 1 means the view is completely opaque. ({@link LiveVar})
     *
     * @param alpha Animator values
     * @see View#setAlpha(float)
     */
    public AXAnimation alpha(LiveVar<Float[]> alpha) {
        createRule(PropertyRule.alpha(alpha));
        return this;
    }

    /**
     * Animates the amount that the view is scaled in Y and X around the pivot point, as a proportion of
     * the view's unscaled width. A value of 1 means that no scaling is applied.
     *
     * @param scale The scaling factor. (Animator values)
     * @see View#setScaleX(float)
     * @see View#setScaleY(float)
     * @see #scaleX(Float...)
     * @see #scaleY(Float...)
     */
    public AXAnimation scale(Float... scale) {
        scaleX(scale);
        scaleY(scale);
        return this;
    }

    /**
     * Animates the amount that the view is scaled in Y and X around the pivot point, as a proportion of
     * the view's unscaled width. A value of 1 means that no scaling is applied. ({@link LiveVar})
     *
     * @param scale The scaling factor. (Animator values)
     * @see View#setScaleX(float)
     * @see View#setScaleY(float)
     * @see #scaleX(Float...)
     * @see #scaleY(Float...)
     */
    public AXAnimation scale(LiveVar<Float[]> scale) {
        scaleX(scale);
        scaleY(scale);
        return this;
    }

    /**
     * Animates the amount that the view is scaled in x around the pivot point, as a proportion of
     * the view's unscaled width. A value of 1 means that no scaling is applied.
     *
     * @param scaleX The scaling factor. (Animator values)
     * @see View#setScaleX(float)
     */
    public AXAnimation scaleX(Float... scaleX) {
        createRule(PropertyRule.scaleX(scaleX));
        return this;
    }

    /**
     * Animates the amount that the view is scaled in x around the pivot point, as a proportion of
     * the view's unscaled width. A value of 1 means that no scaling is applied. ({@link LiveVar})
     *
     * @param scaleX The scaling factor. (Animator values)
     * @see View#setScaleX(float)
     */
    public AXAnimation scaleX(LiveVar<Float[]> scaleX) {
        createRule(PropertyRule.scaleX(scaleX));
        return this;
    }

    /**
     * Animates the amount that the view is scaled in Y around the pivot point, as a proportion of
     * the view's unscaled width. A value of 1 means that no scaling is applied.
     *
     * @param scaleY The scaling factor. (Animator values)
     * @see View#setScaleY(float)
     */
    public AXAnimation scaleY(Float... scaleY) {
        createRule(PropertyRule.scaleY(scaleY));
        return this;
    }

    /**
     * Animates the amount that the view is scaled in Y around the pivot point, as a proportion of
     * the view's unscaled width. A value of 1 means that no scaling is applied. ({@link LiveVar})
     *
     * @param scaleY The scaling factor. (Animator values)
     * @see View#setScaleY(float)
     */
    public AXAnimation scaleY(LiveVar<Float[]> scaleY) {
        createRule(PropertyRule.scaleY(scaleY));
        return this;
    }

    /**
     * Set the View's matrix to skew by sx and sy
     */
    public AXAnimation skew(float kx, float ky) {
        return skew(new PointF(kx, ky));
    }

    /**
     * Set the View's matrix to skew by sx and sy
     *
     * @param skewValues skew kx & ky: PointF(kx,ky) (Animator values)
     */
    public AXAnimation skew(PointF... skewValues) {
        createRule(new RuleSkew(skewValues));
        return this;
    }

    /**
     * Set the ImageView's ImageMatrix to skew by sx and sy
     */
    public AXAnimation imageSkew(float kx, float ky) {
        return imageSkew(new PointF(kx, ky));
    }

    /**
     * Set the View's matrix to skew by sx and sy
     *
     * @param skewValues skew kx & ky: PointF(kx,ky) (Animator values)
     */
    public AXAnimation imageSkew(PointF... skewValues) {
        createRule(new RuleImageSkew(skewValues));
        return this;
    }

    // *************** Rotation Rules ***************

    /**
     * Animates the degrees that the view is rotated around the pivot point. Increasing values
     * result in clockwise rotation.
     *
     * @param rotation The degrees of rotations (Animator values)
     * @see View#setRotation(float)
     */
    public AXAnimation rotation(Float... rotation) {
        createRule(PropertyRule.rotation(rotation));
        return this;
    }

    /**
     * Animates the degrees that the view is rotated around the pivot point. Increasing values
     * result in clockwise rotation. ({@link LiveVar})
     *
     * @param rotation The degrees of rotations (Animator values)
     * @see View#setRotation(float)
     */
    public AXAnimation rotation(LiveVar<Float[]> rotation) {
        createRule(PropertyRule.rotation(rotation));
        return this;
    }

    /**
     * Animates the degrees that the view is rotated around the horizontal axis through the pivot point.
     * Increasing values result in clockwise rotation from the viewpoint of looking down the
     * x axis.
     * <p>
     * When rotating large views, it is recommended to adjust the camera distance
     * accordingly. Refer to {@link #cameraDistance(Float...)} for more information.
     *
     * @param rotationX The degrees of X rotations (Animator values)
     * @see View#setRotationX(float)
     */
    public AXAnimation rotationX(Float... rotationX) {
        createRule(PropertyRule.rotationX(rotationX));
        return this;
    }

    /**
     * Animates the degrees that the view is rotated around the horizontal axis through the pivot point.
     * Increasing values result in clockwise rotation from the viewpoint of looking down the
     * x axis. ({@link LiveVar})
     * <p>
     * When rotating large views, it is recommended to adjust the camera distance
     * accordingly. Refer to {@link #cameraDistance(Float...)} for more information.
     *
     * @param rotationX The degrees of X rotations (Animator values)
     * @see View#setRotationX(float)
     */
    public AXAnimation rotationX(LiveVar<Float[]> rotationX) {
        createRule(PropertyRule.rotationX(rotationX));
        return this;
    }

    /**
     * Animates the degrees that the view is rotated around the vertical axis through the pivot point.
     * Increasing values result in counter-clockwise rotation from the viewpoint of looking
     * down the y axis.
     * <p>
     * When rotating large views, it is recommended to adjust the camera distance
     * accordingly. Refer to {@link #cameraDistance(Float...)} for more information.
     *
     * @param rotationY The degrees of Y rotations (Animator values)
     * @see View#setRotationY(float)
     */
    public AXAnimation rotationY(Float... rotationY) {
        createRule(PropertyRule.rotationY(rotationY));
        return this;
    }

    /**
     * Animates the degrees that the view is rotated around the vertical axis through the pivot point.
     * Increasing values result in counter-clockwise rotation from the viewpoint of looking
     * down the y axis. ({@link LiveVar})
     * <p>
     * When rotating large views, it is recommended to adjust the camera distance
     * accordingly. Refer to {@link #cameraDistance(Float...)} for more information.
     *
     * @param rotationY The degrees of Y rotations (Animator values)
     * @see View#setRotationY(float)
     */
    public AXAnimation rotationY(LiveVar<Float[]> rotationY) {
        createRule(PropertyRule.rotationY(rotationY));
        return this;
    }

    /**
     * <p>Animates the distance along the Z axis (orthogonal to the X/Y plane on which
     * views are drawn) from the camera to this view. The camera's distance
     * affects 3D transformations, for instance rotations around the X and Y
     * axis. If the rotationX or rotationY properties are changed and this view is
     * large (more than half the size of the screen), it is recommended to always
     * use a camera distance that's greater than the height (X axis rotation) or
     * the width (Y axis rotation) of this view.</p>
     *
     * @param distance The distance in "depth pixels", if negative the opposite
     *                 value is used (Animator values)
     * @see View#setCameraDistance(float)
     * @see #rotationX(Float...)
     * @see #rotationY(Float...)
     */
    public AXAnimation cameraDistance(Float... distance) {
        createRule(PropertyRule.cameraDistance(distance));
        return this;
    }

    /**
     * <p>Animates the distance along the Z axis (orthogonal to the X/Y plane on which
     * views are drawn) from the camera to this view. The camera's distance
     * affects 3D transformations, for instance rotations around the X and Y
     * axis. If the rotationX or rotationY properties are changed and this view is
     * large (more than half the size of the screen), it is recommended to always
     * use a camera distance that's greater than the height (X axis rotation) or
     * the width (Y axis rotation) of this view.</p> ({@link LiveVar})
     *
     * @param distance The distance in "depth pixels", if negative the opposite
     *                 value is used (Animator values)
     * @see View#setCameraDistance(float)
     * @see #rotation(LiveVar)
     * @see #rotationY(LiveVar)
     */
    public AXAnimation cameraDistance(LiveVar<Float[]> distance) {
        createRule(PropertyRule.cameraDistance(distance));
        return this;
    }

    // *************** Property Rules ***************

    /**
     * Animates the visibility state of this view (using alpha).
     *
     * @param visibility One of {@link View#VISIBLE}, {@link View#INVISIBLE}, or {@link View#GONE}.
     * @see View#setVisibility(int)
     * @see #alpha(Float...)
     */
    public AXAnimation visibility(int visibility) {
        createRule(new RuleVisibility(visibility));
        return this;
    }

    /**
     * Animates the background color for this view.
     *
     * @param colors the color of the background (Animator values)
     * @see View#setBackgroundColor(int)
     */
    public AXAnimation backgroundColor(Integer... colors) {
        createRule(new RuleBackgroundColor(colors));
        return this;
    }

    /**
     * Animates the background color for this view. ({@link LiveVar})
     *
     * @param colors the color of the background (Animator values)
     * @see View#setBackgroundColor(int)
     */
    public AXAnimation backgroundColor(LiveVar<Integer[]> colors) {
        createRule(new RuleBackgroundColor(colors));
        return this;
    }

    /**
     * Animates the background drawable.
     * Supports {@link ColorDrawable} and {@link GradientDrawable}
     * Other types of drawable will animate by {@link #backgroundFade(Drawable...)}
     *
     * @param backgrounds background drawables to animate (Animator values)
     * @see View#setBackground(Drawable)
     */
    public AXAnimation background(Drawable... backgrounds) {
        createRule(new RuleBackgroundDrawable(backgrounds));
        return this;
    }

    /**
     * Animates the background drawable using alpha.
     *
     * @param backgrounds background drawables to animate (Animator values)
     * @see View#setBackground(Drawable)
     */
    public AXAnimation backgroundFade(Drawable... backgrounds) {
        createRule(new RuleFadeBackgroundDrawable(backgrounds));
        return this;
    }

    /**
     * Animates the text color for all the states (normal, selected,
     * focused) to be this color.
     *
     * @param colors Color values in the form 0xAARRGGBB. (Animator values)
     * @see android.widget.TextView#setTextColor(int)
     */
    public AXAnimation textColor(Integer... colors) {
        createRule(new RuleTextColor(colors));
        return this;
    }

    /**
     * Animates the text color for all the states (normal, selected,
     * focused) to be this color. ({@link LiveVar})
     *
     * @param colors Color values in the form 0xAARRGGBB. (Animator values)
     * @see android.widget.TextView#setTextColor(int)
     */
    public AXAnimation textColor(LiveVar<Integer[]> colors) {
        createRule(new RuleTextColor(colors));
        return this;
    }

    /**
     * Animates the default text size to the given value, interpreted as "scaled
     * pixel" units.  This size is adjusted based on the current density and
     * user font size preference.
     *
     * @param sizes The scaled pixel size. (Animator values)
     * @see android.widget.TextView#setTextSize(float)
     */
    public AXAnimation textSize(Float... sizes) {
        createRule(PropertyRule.textSize(sizes));
        return this;
    }

    /**
     * Animates the default text size to the given value, interpreted as "scaled
     * pixel" units.  This size is adjusted based on the current density and
     * user font size preference. ({@link LiveVar})
     *
     * @param sizes The scaled pixel size. (Animator values)
     * @see android.widget.TextView#setTextSize(float)
     */
    public AXAnimation textSize(LiveVar<Float[]> sizes) {
        createRule(PropertyRule.textSize(sizes));
        return this;
    }

    /**
     * Animates the default text size to a given unit and value. See {@link
     * TypedValue} for the possible dimension units.
     *
     * @param unit  The desired dimension unit.
     * @param sizes The desired size in the given units. (Animator values)
     * @see android.widget.TextView#setTextSize(int, float)
     */
    public AXAnimation textSize(int unit, Float... sizes) {
        createRule(new RuleTextSizeUnit(unit, sizes));
        return this;
    }

    /**
     * Animates the default text size to a given unit and value. See {@link
     * TypedValue} for the possible dimension units. ({@link LiveVar})
     *
     * @param unit  The desired dimension unit.
     * @param sizes The desired size in the given units. (Animator values)
     * @see android.widget.TextView#setTextSize(int, float)
     */
    public AXAnimation textSize(int unit, LiveVar<Float[]> sizes) {
        createRule(new RuleTextSizeUnit(unit, sizes));
        return this;
    }

    // *************** LayoutParams ***************

    /**
     * Sets the View's final layoutParams,
     * When animator is done the final LayoutParams will be set to view.
     * You can animate this by using {@link AXAnimation#toLayoutParams(ViewGroup.LayoutParams, boolean)}
     *
     * @param targetLayoutParams final LayoutParams
     * @see AXAnimation#toLayoutParams(ViewGroup.LayoutParams)
     */
    public AXAnimation setTargetLayoutParams(ViewGroup.LayoutParams targetLayoutParams) {
        this.targetLayoutParams = targetLayoutParams;
        return this;
    }

    /**
     * Gets the View's final layoutParams
     *
     * @return final LayoutParams
     */
    public ViewGroup.LayoutParams getTargetLayoutParams() {
        return targetLayoutParams;
    }


    /**
     * Sets start view's layoutParams
     *
     * @param layoutParams The start layout parameters for this view, cannot be null
     */
    public AXAnimation fromLayoutParams(@NonNull ViewGroup.LayoutParams layoutParams) {
        originalLayoutParams = layoutParams;
        return this;
    }

    // *************** Layout Rules ***************

    /**
     * Add a rule for changing view's LayoutParams
     *
     * @param layoutParams The target layout parameters for this view, cannot be null
     * @see View#setLayoutParams(ViewGroup.LayoutParams)
     */
    public AXAnimation toLayoutParams(@NonNull ViewGroup.LayoutParams layoutParams) {
        return toLayoutParams(layoutParams, false);
    }

    /**
     * Add a rule for changing view's LayoutParams
     *
     * @param layoutParams The target layout parameters for this view, cannot be null
     * @param markAsTarget will set this layoutParams as final layoutParams
     * @see AXAnimation#setTargetLayoutParams(ViewGroup.LayoutParams)
     */
    public AXAnimation toLayoutParams(ViewGroup.LayoutParams layoutParams, boolean markAsTarget) {
        createRule(new RuleLayoutParams(layoutParams));
        if (markAsTarget)
            setTargetLayoutParams(layoutParams);
        return this;
    }

    /**
     * Add a rule for changing view's LayoutParams to the original view's layoutParams.
     */
    public AXAnimation backToFirstPlace() {
        return backToFirstPlace(false);
    }

    /**
     * Add a rule for changing view's LayoutParams to the original view's layoutParams.
     *
     * @param markAsTarget will set original view's layoutParams as final layoutParams
     * @see AXAnimation#setTargetLayoutParams(ViewGroup.LayoutParams)
     */
    public AXAnimation backToFirstPlace(boolean markAsTarget) {
        return toLayoutParams(null, markAsTarget);
    }

    /**
     * Add a rule for changing view's {@link LayoutSize} to the previous {@link LayoutSize}.
     */
    public AXAnimation backToPreviousPlace() {
        createRule(new RulePreviousLayout(-2));
        return this;
    }

    /**
     * Add a rule for changing view's {@link LayoutSize} to the another section's {@link LayoutSize}.
     *
     * @param sectionIndex section index to get layout size
     */
    public AXAnimation backToSectionPlace(int sectionIndex) {
        createRule(new RulePreviousLayout(sectionIndex));
        return this;
    }

    /**
     * Moves view on a path.
     *
     * @param path path to move view on it
     */
    public AXAnimation moveOnPath(Path path) {
        createRule(new RulePath(path, widthLocked, heightLocked));
        return this;
    }

    // *************** Move Rules ***************

    /**
     * Moves a corner of the view to the given point
     *
     * @param gravity gravity of view's corner
     * @param x       target x
     * @param y       target y
     */
    public AXAnimation move(int gravity, int x, int y) {
        return move(gravity, new Point(getValue(false, false, x), getValue(false, false, y)));
    }

    /**
     * Moves a corner of the view to the given point
     *
     * @param gravity gravity of view's corner
     * @param x       target x
     * @param y       target y
     */
    public AXAnimation move(int gravity, LiveSize x, LiveSize y) {
        createRule(new RuleLiveMove(gravity, widthLocked, heightLocked, getValue(false, x), getValue(false, y)));
        return this;
    }

    /**
     * Moves a corner of the view to the given point
     *
     * @param gravity gravity of view's corner
     * @param points  target points (Animator values)
     */
    public AXAnimation move(int gravity, Point... points) {
        createRule(new RuleMove(gravity, widthLocked, heightLocked, getValues(points)));
        return this;
    }

    /**
     * Moves a corner of the view to another point based on a relative view.
     *
     * @param viewID        id of related view
     * @param sourceGravity gravity of view's corner
     * @param targetGravity gravity of related view's corner
     * @param delta         increases the values of x and y at the end point
     */
    public AXAnimation relativeMove(int viewID, int sourceGravity, int targetGravity, Point delta) {
        createRule(new RuleRelativeMove(sourceGravity, targetGravity, viewID, getValue(delta), widthLocked, heightLocked));
        return this;
    }

    /**
     * Moves a corner of the view to another point based on a relative view.
     *
     * @param view          related view
     * @param sourceGravity gravity of view's corner
     * @param targetGravity gravity of related view's corner
     * @param delta         increases the values of x and y at the end point
     */
    public AXAnimation relativeMove(View view, int sourceGravity, int targetGravity, Point delta) {
        createRule(new RuleRelativeMove(sourceGravity, targetGravity, view, getValue(delta), widthLocked, heightLocked));
        return this;
    }

    /**
     * Moves a corner of the view to another point based on a relative view.
     *
     * @param viewID        id of related view
     * @param sourceGravity gravity of view's corner
     * @param targetGravity gravity of related view's corner
     * @param dx            increases the values of x at the end point
     * @param dy            increases the values of y at the end point
     */
    public AXAnimation relativeMove(int viewID, int sourceGravity, int targetGravity, int dx, int dy) {
        createRule(new RuleRelativeMove(sourceGravity, targetGravity, viewID, new Point(getValue(false, false, dx), getValue(false, false, dy)), widthLocked, heightLocked));
        return this;
    }

    /**
     * Moves a corner of the view to another point based on a relative view.
     *
     * @param view          related view
     * @param sourceGravity gravity of view's corner
     * @param targetGravity gravity of related view's corner
     * @param dx            increases the values of x at the end point
     * @param dy            increases the values of y at the end point
     */
    public AXAnimation relativeMove(View view, int sourceGravity, int targetGravity, int dx, int dy) {
        createRule(new RuleRelativeMove(sourceGravity, targetGravity, view, new Point(getValue(false, false, dx), getValue(false, false, dy)), widthLocked, heightLocked));
        return this;
    }

    // *************** Position Rules ***************

    /**
     * Moves the left of view.
     *
     * @param left target left
     */
    public AXAnimation toLeft(int left) {
        createRule(new RulePosition(Gravity.LEFT, widthLocked, heightLocked, getValue(true, left)));
        return this;
    }

    /**
     * Moves the right of view.
     *
     * @param right target right
     */
    public AXAnimation toRight(int right) {
        createRule(new RulePosition(Gravity.RIGHT, widthLocked, heightLocked, getValue(true, right)));
        return this;
    }

    /**
     * Moves the top of view.
     *
     * @param top target top
     */
    public AXAnimation toTop(int top) {
        createRule(new RulePosition(Gravity.TOP, widthLocked, heightLocked, getValue(true, top)));
        return this;
    }

    /**
     * Moves the bottom of view.
     *
     * @param bottom target bottom
     */
    public AXAnimation toBottom(int bottom) {
        createRule(new RulePosition(Gravity.BOTTOM, widthLocked, heightLocked, getValue(true, bottom)));
        return this;
    }

    /**
     * Moves the center horizontal of view.
     *
     * @param center target center horizontal
     */
    public AXAnimation toCenterHorizontal(int center) {
        createRule(new RulePosition(Gravity.CENTER_HORIZONTAL, widthLocked, heightLocked, getValue(true, center)));
        return this;
    }

    /**
     * Moves the center vertical of view.
     *
     * @param center target center vertical
     */
    public AXAnimation toCenterVertical(int center) {
        createRule(new RulePosition(Gravity.CENTER_VERTICAL, widthLocked, heightLocked, getValue(true, center)));
        return this;
    }

    /**
     * Moves the left of view.
     *
     * @param left target left
     */
    public AXAnimation toLeft(LiveSize left) {
        createRule(new RuleLivePosition(Gravity.LEFT, widthLocked, heightLocked, getValue(true, left)));
        return this;
    }

    /**
     * Moves the right of view.
     *
     * @param right target right
     */
    public AXAnimation toRight(LiveSize right) {
        createRule(new RuleLivePosition(Gravity.RIGHT, widthLocked, heightLocked, getValue(true, right)));
        return this;
    }

    /**
     * Moves the top of view.
     *
     * @param top target top
     */
    public AXAnimation toTop(LiveSize top) {
        createRule(new RuleLivePosition(Gravity.TOP, widthLocked, heightLocked, getValue(true, top)));
        return this;
    }

    /**
     * Moves the bottom of view.
     *
     * @param bottom target bottom
     */
    public AXAnimation toBottom(LiveSize bottom) {
        createRule(new RuleLivePosition(Gravity.BOTTOM, widthLocked, heightLocked, getValue(true, bottom)));
        return this;
    }

    /**
     * Moves the center horizontal of view.
     *
     * @param center target center horizontal
     */
    public AXAnimation toCenterHorizontal(LiveSize center) {
        createRule(new RuleLivePosition(Gravity.CENTER_HORIZONTAL, widthLocked, heightLocked, getValue(true, center)));
        return this;
    }

    /**
     * Moves the center vertical of view.
     *
     * @param center target center vertical
     */
    public AXAnimation toCenterVertical(LiveSize center) {
        createRule(new RuleLivePosition(Gravity.CENTER_VERTICAL, widthLocked, heightLocked, getValue(true, center)));
        return this;
    }

    /**
     * Moves the left of view based on a relative view.
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of left at the end point
     */
    public AXAnimation toLeftOf(int viewID, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.LEFT, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the left of view based on a relative view.
     *
     * @param view    related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of left at the end point
     */
    public AXAnimation toLeftOf(View view, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.LEFT, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the right of view based on a relative view.
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of right at the end point
     */
    public AXAnimation toRightOf(int viewID, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.RIGHT, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the right of view based on a relative view.
     *
     * @param view    related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of right at the end point
     */
    public AXAnimation toRightOf(View view, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.RIGHT, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the top of view based on a relative view.
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of top at the end point
     */
    public AXAnimation toTopOf(int viewID, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.TOP, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the top of view based on a relative view.
     *
     * @param view    related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of top at the end point
     */
    public AXAnimation toTopOf(View view, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.TOP, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the bottom of view based on a relative view.
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of bottom at the end point
     */
    public AXAnimation toBottomOf(int viewID, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.BOTTOM, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the bottom of view based on a relative view.
     *
     * @param view    related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of bottom at the end point
     */
    public AXAnimation toBottomOf(View view, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.BOTTOM, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the center horizontal of view based on a relative view.
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of center horizontal at the end point
     */
    public AXAnimation toCenterHorizontalOf(int viewID, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.CENTER_HORIZONTAL, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the center horizontal of view based on a relative view.
     *
     * @param view    related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of center horizontal at the end point
     */
    public AXAnimation toCenterHorizontalOf(View view, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.CENTER_HORIZONTAL, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the center vertical of view based on a relative view.
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of center vertical at the end point
     */
    public AXAnimation toCenterVerticalOf(int viewID, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.CENTER_VERTICAL, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves the center vertical of view based on a relative view.
     *
     * @param view    related view
     * @param gravity gravity of view's axis
     * @param delta   increases the values of center vertical at the end point
     */
    public AXAnimation toCenterVerticalOf(View view, int gravity, int delta) {
        createRule(new RuleRelativePosition(gravity, Gravity.CENTER_VERTICAL, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves center of view to center of related view
     *
     * @param view related view
     */
    public AXAnimation toCenterOf(View view) {
        return toCenterOf(view, Gravity.CENTER);
    }

    /**
     * Moves a corner of view to center of related view
     *
     * @param view    related view
     * @param gravity gravity of view's corner
     */
    public AXAnimation toCenterOf(View view, int gravity) {
        return toCenterOf(view, gravity, 0, 0);
    }

    /**
     * Moves a corner of view to center of related view
     *
     * @param view            related view
     * @param gravity         gravity of view's corner
     * @param horizontalDelta increases the values of x at the end point
     * @param verticalData    increases the values of y at the end point
     */
    public AXAnimation toCenterOf(View view, int gravity, int horizontalDelta, int verticalData) {
        toCenterHorizontalOf(view, gravity & Gravity.HORIZONTAL_GRAVITY_MASK, getValue(false, false, horizontalDelta));
        toCenterVerticalOf(view, gravity & Gravity.VERTICAL_GRAVITY_MASK, getValue(false, false, verticalData));
        return this;
    }

    /**
     * Moves center of view to center of related view
     *
     * @param viewID id of related view
     */
    public AXAnimation toCenterOf(int viewID) {
        return toCenterOf(viewID, Gravity.CENTER);
    }

    /**
     * Moves a corner of view to center of related view
     *
     * @param viewID  id of related view
     * @param gravity gravity of view's corner
     */
    public AXAnimation toCenterOf(int viewID, int gravity) {
        return toCenterOf(viewID, gravity, 0, 0);
    }

    /**
     * Moves a corner of view to center of related view
     *
     * @param viewID          id of related view
     * @param gravity         gravity of view's corner
     * @param horizontalDelta increases the values of x at the end point
     * @param verticalData    increases the values of y at the end point
     */
    public AXAnimation toCenterOf(int viewID, int gravity, int horizontalDelta, int verticalData) {
        toCenterHorizontalOf(viewID, gravity & Gravity.HORIZONTAL_GRAVITY_MASK, getValue(false, false, horizontalDelta));
        toCenterVerticalOf(viewID, gravity & Gravity.VERTICAL_GRAVITY_MASK, getValue(false, false, verticalData));
        return this;
    }

    /**
     * Moves a axis of view to the given value.
     *
     * @param gravity  gravity of view's axis
     * @param position target position
     */
    public AXAnimation toPosition(int gravity, int position) {
        createRule(new RulePosition(gravity, widthLocked, heightLocked, getValue(true, position)));
        return this;
    }

    /**
     * Moves a corner of view to the given point.
     *
     * @param gravity gravity of view's corner
     * @param x       target X position
     * @param y       target Y position
     */
    public AXAnimation toPosition(int gravity, int x, int y) {
        createRule(new RulePosition(gravity & Gravity.HORIZONTAL_GRAVITY_MASK, widthLocked, heightLocked, getValue(true, x)));
        createRule(new RulePosition(gravity & Gravity.VERTICAL_GRAVITY_MASK, widthLocked, heightLocked, getValue(true, y)));
        return this;
    }

    /**
     * Moves a axis of view based on a relative view.
     *
     * @param viewID        id of related view
     * @param sourceGravity gravity of view's axis
     * @param targetGravity gravity of related view's axis
     * @param delta         increases the position at the end point
     */
    public AXAnimation toPositionOf(int viewID, int sourceGravity, int targetGravity, int delta) {
        createRule(new RuleRelativePosition(sourceGravity, targetGravity, viewID, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    /**
     * Moves a axis of view based on a relative view.
     *
     * @param view          related view
     * @param sourceGravity gravity of view's axis
     * @param targetGravity gravity of related view's axis
     * @param delta         increases the position at the end point
     */
    public AXAnimation toPositionOf(View view, int sourceGravity, int targetGravity, int delta) {
        createRule(new RuleRelativePosition(sourceGravity, targetGravity, view, widthLocked, heightLocked, getValue(false, false, delta)));
        return this;
    }

    // *************** Resize Rules ***************

    /**
     * Resize view
     *
     * @param left   target left
     * @param top    target top
     * @param right  target right
     * @param bottom target bottom
     */
    public AXAnimation resize(int left, int top, int right, int bottom) {
        return resize(new Rect(left, top, right, bottom));
    }

    /**
     * Resize view
     *
     * @param left   target left
     * @param top    target top
     * @param right  target right
     * @param bottom target bottom
     */
    public AXAnimation resize(LiveSize left, LiveSize top, LiveSize right, LiveSize bottom) {
        return resize(new LayoutSize(left, top, right, bottom));
    }

    /**
     * Resize view
     *
     * @param values target layouts (Animator values)
     */
    public AXAnimation resize(Rect... values) {
        createRule(new RuleRect(getValues(values)));
        return this;
    }

    /**
     * Resize view
     *
     * @param values target layouts (Animator values)
     */
    public AXAnimation resize(LayoutSize... values) {
        createRule(new RuleLayoutSize(getValues(values)));
        return this;
    }

    /**
     * Resize left and right of View
     *
     * @param left  target left
     * @param right target right
     */
    public AXAnimation resizeHorizontal(int left, int right) {
        createRule(new RuleRect(true, false, new Rect(getValue(left), 0, getValue(true, right), 0)));
        return this;
    }

    /**
     * Resize left and right of View
     *
     * @param left  target left
     * @param right target right
     */
    public AXAnimation resizeHorizontal(LiveSize left, LiveSize right) {
        createRule(new RuleLayoutSize(true, false, new LayoutSize(getValue(false, left), null, getValue(true, right), null)));
        return this;
    }

    /**
     * Resize left and right of View
     *
     * @param values target horizontal layouts (Animator values)
     *               Only left & right will be updated
     */
    public AXAnimation resizeHorizontal(Rect... values) {
        createRule(new RuleRect(true, false, getValues(values)));
        return this;
    }

    /**
     * Resize left and right of View
     *
     * @param values target horizontal layouts (Animator values)
     *               Only left & right will be updated
     */
    public AXAnimation resizeHorizontal(LayoutSize... values) {
        createRule(new RuleLayoutSize(true, false, getValues(values)));
        return this;
    }

    /**
     * Resize top and bottom of View
     *
     * @param top    target top
     * @param bottom target bottom
     */
    public AXAnimation resizeVertical(int top, int bottom) {
        createRule(new RuleRect(false, true, new Rect(0, getValue(top), 0, getValue(true, bottom))));
        return this;
    }

    /**
     * Resize top and bottom of View
     *
     * @param top    target top
     * @param bottom target bottom
     */
    public AXAnimation resizeVertical(LiveSize top, LiveSize bottom) {
        createRule(new RuleLayoutSize(false, true, new LayoutSize(null, getValue(false, top), null, getValue(true, bottom))));
        return this;
    }

    /**
     * Resize top and bottom of View
     *
     * @param values target horizontal layouts (Animator values)
     *               Only top & bottom will be updated
     */
    public AXAnimation resizeVertical(Rect... values) {
        createRule(new RuleRect(false, true, getValues(values)));
        return this;
    }

    /**
     * Resize top and bottom of View
     *
     * @param values target horizontal layouts (Animator values)
     *               Only top & bottom will be updated
     */
    public AXAnimation resizeVertical(LayoutSize... values) {
        createRule(new RuleLayoutSize(false, true, getValues(values)));
        return this;
    }

    /**
     * Resize width of View
     *
     * @param gravity One of {@link Gravity#LEFT}, {@link Gravity#RIGHT}, or {@link Gravity#CENTER_HORIZONTAL}.
     * @param width   target width (Animator values)
     */
    public AXAnimation resizeWidth(int gravity, int... width) {
        createRule(new RuleResizeWidth(gravity, getValues(true, width)));
        return this;
    }

    /**
     * Resize height of View
     *
     * @param gravity One of {@link Gravity#TOP}, {@link Gravity#BOTTOM}, or {@link Gravity#CENTER_VERTICAL}.
     * @param height  target width (Animator values)
     */
    public AXAnimation resizeHeight(int gravity, int... height) {
        createRule(new RuleResizeHeight(gravity, getValues(true, height)));
        return this;
    }

    /**
     * Resize width of View
     *
     * @param gravity One of {@link Gravity#LEFT}, {@link Gravity#RIGHT}, or {@link Gravity#CENTER_HORIZONTAL}.
     * @param width   target width (Animator values)
     */
    public AXAnimation resizeWidth(int gravity, LiveSize... width) {
        createRule(new RuleResizeLiveWidth(gravity, getValues(width)));
        return this;
    }

    /**
     * Resize height of View
     *
     * @param gravity One of {@link Gravity#TOP}, {@link Gravity#BOTTOM}, or {@link Gravity#CENTER_VERTICAL}.
     * @param height  target width (Animator values)
     */
    public AXAnimation resizeHeight(int gravity, LiveSize... height) {
        createRule(new RuleResizeLiveHeight(gravity, getValues(height)));
        return this;
    }

    /**
     * Resize width and height of View
     *
     * @param gravity gravity of view's corner to resize
     * @param width   target width
     * @param height  target height
     */
    public AXAnimation resize(int gravity, int width, int height) {
        resizeWidth(gravity, width);
        resizeHeight(gravity, height);
        return this;
    }

    /**
     * Resize width and height of View
     *
     * @param gravity gravity of view's corner to resize
     * @param width   target width
     * @param height  target height
     */
    public AXAnimation resize(int gravity, LiveSize width, LiveSize height) {
        resizeWidth(gravity, width);
        resizeHeight(gravity, height);
        return this;
    }

    /**
     * Sets the padding
     *
     * @param values target paddings (Animator values)
     * @see View#setPadding(int, int, int, int)
     */
    public AXAnimation padding(Rect... values) {
        createRule(new RulePadding(getValues(values)));
        return this;
    }

    /**
     * Sets the padding
     *
     * @param left   the left padding
     * @param top    the top padding
     * @param right  the right padding
     * @param bottom the bottom padding
     * @see View#setPadding(int, int, int, int)
     */
    public AXAnimation padding(int left, int top, int right, int bottom) {
        return padding(new Rect(left, top, right, bottom));
    }

    // *************** Matrix Rules ***************

    /**
     * Adds a transformation {@link Matrix} that is applied
     * to the view's drawable when it is drawn.  Allows custom scaling,
     * translation, and perspective distortion.
     *
     * @param matrices The transformation parameters in matrix form. (Animator values)
     * @see View#getMatrix()
     */
    public AXAnimation matrix(Matrix... matrices) {
        createRule(new RuleMatrix(null, matrices));
        return this;
    }

    /**
     * Adds a transformation {@link Matrix} that is applied
     * to the view's drawable when it is drawn.  Allows custom scaling,
     * translation, and perspective distortion.
     *
     * @param matrices The transformation parameters in matrix form. (Animator values)
     * @see android.widget.ImageView#setImageMatrix(Matrix)
     */
    public AXAnimation imageMatrix(Matrix... matrices) {
        createRule(new RuleImageMatrix(matrices));
        return this;
    }

    /**
     * Add custom {@link Matrix} rule
     *
     * @param listener listen to animated value (matrix)
     * @param matrices The transformation parameters in matrix form. (Animator values)
     */
    public AXAnimation customMatrix(AXAnimatorUpdateListener<Matrix> listener, Matrix... matrices) {
        createRule(new RuleMatrix(listener, matrices));
        return this;
    }

    // *************** Custom Rules ***************

    /**
     * Add custom rules
     *
     * @param rules the rules to be added to the current list of rules for this animation.
     * @see Rule
     * @see RuleSet
     */
    public AXAnimation addRule(Rule<?>... rules) {
        for (Rule<?> rule : rules)
            createRule(rule);
        return this;
    }

    /**
     * Add custom rules as reverse
     *
     * @param rules the rules to be reversed and added to the current list of rules for this animation.
     * @see Rule
     * @see RuleSet
     */
    public AXAnimation addReverseRule(Rule<?>... rules) {
        for (Rule<?> rule : rules)
            reverseRule(rule);
        return this;
    }

    /**
     * Add custom rule section (before the opened section)
     *
     * @param ruleSections the ruleSections to be added to the current list of sections for this animation.
     * @see RuleSection
     */
    public AXAnimation addRuleSection(RuleSection... ruleSections) {
        for (RuleSection section : ruleSections) {
            if (section.getAnimatorValues() == null)
                section.setAnimatorValues(data.clone());
            addSection(section);
        }
        return this;
    }

    /**
     * Add a custom property rule
     *
     * @param propertyName The name of the property being animated.
     * @param values       A set of values that the animation will animate between over time.
     * @see android.animation.ObjectAnimator
     */
    public AXAnimation property(String propertyName, float... values) {
        createRule(new RuleObjectFloat(propertyName, values));
        return this;
    }

    /**
     * Add a custom property rule
     *
     * @param propertyName The name of the property being animated.
     * @param values       A set of values that the animation will animate between over time.
     * @see android.animation.ObjectAnimator
     */
    public AXAnimation property(String propertyName, int... values) {
        createRule(new RuleObjectInt(propertyName, values));
        return this;
    }

    /**
     * Add a custom property rule
     *
     * @param propertyName The name of the property being animated.
     * @param values       A set of values that the animation will animate between over time.
     *                     the values will be updated by {@link AXAnimation#measureUnit(float)}
     * @see android.animation.ObjectAnimator
     */
    public AXAnimation propertySize(String propertyName, float... values) {
        createRule(new RuleObjectFloat(propertyName, getValues(values)));
        return this;
    }

    /**
     * Add a custom property rule
     *
     * @param propertyName The name of the property being animated.
     * @param values       A set of values that the animation will animate between over time.
     *                     the values will be updated by {@link AXAnimation#measureUnit(float)}
     * @see android.animation.ObjectAnimator
     */
    public AXAnimation propertySize(String propertyName, int... values) {
        createRule(new RuleObjectInt(propertyName, getValues(values)));
        return this;
    }

    /**
     * Add a custom property rule using {@link android.animation.ArgbEvaluator}
     *
     * @param propertyName The name of the property being animated.
     * @param colors       A set of colors that the animation will animate between over time.
     * @see android.animation.ObjectAnimator
     */
    public AXAnimation propertyColor(String propertyName, int... colors) {
        createRule(new RuleObjectColor(propertyName, colors));
        return this;
    }


    /**
     * Add a custom property rule
     *
     * @param propertyName The name of the property being animated.
     * @param evaluator    A TypeEvaluator that will be called on each animation frame to
     *                     provide the necessary interpolation between the Object values to derive the animated
     *                     value.
     * @param values       A set of values that the animation will animate between over time.
     * @see android.animation.ObjectAnimator
     */
    @SafeVarargs
    public final <T> AXAnimation property(String propertyName, TypeEvaluator<T> evaluator, T... values) {
        createRule(new RuleObject<>(evaluator, propertyName, values));
        return this;
    }

    /**
     * Add custom rule
     *
     * @param listener listen to animated value (float)
     * @param values   A set of values that the animation will animate between over time.
     * @see AXAnimatorUpdateListener
     * @see android.animation.ValueAnimator
     */
    public AXAnimation custom(AXAnimatorUpdateListener<Float> listener, float... values) {
        createRule(new SimpleRuleFloat(listener, values));
        return this;
    }

    /**
     * Add custom rule
     *
     * @param listener listen to animated value (int)
     * @param values   A set of values that the animation will animate between over time.
     * @see AXAnimatorUpdateListener
     * @see android.animation.ValueAnimator
     */
    public AXAnimation custom(AXAnimatorUpdateListener<Integer> listener, int... values) {
        createRule(new SimpleRuleInt(listener, values));
        return this;
    }

    /**
     * Add custom color rule using {@link android.animation.ArgbEvaluator}
     *
     * @param listener listen to animated value (color)
     * @param values   A set of values that the animation will animate between over time.
     * @see AXAnimatorUpdateListener
     * @see android.animation.ValueAnimator
     */
    public AXAnimation customArgb(AXAnimatorUpdateListener<Integer> listener, int... values) {
        createRule(new SimpleRuleArgb(listener, values));
        return this;
    }

    /**
     * Add a custom rule
     *
     * @param listener  listen to animated value
     * @param evaluator A TypeEvaluator that will be called on each animation frame to
     *                  provide the necessary interpolation between the Object values to derive the animated
     *                  value.
     * @param values    A set of values that the animation will animate between over time.
     * @see AXAnimatorUpdateListener
     * @see android.animation.ValueAnimator
     */
    @SafeVarargs
    public final <T> AXAnimation custom(TypeEvaluator<T> evaluator, AXAnimatorUpdateListener<T> listener, T... values) {
        createRule(new SimpleRule<>(evaluator, listener, values));
        return this;
    }

    // *************** Reflection Rules ***************

    /**
     * Add a not animated rule to invoke a {@link java.lang.reflect.Method} of View
     *
     * @param methodName the name of the method
     * @param args       the arguments used for the method call
     * @see InvokeRule
     */
    public AXAnimation invoke(String methodName, Object... args) {
        createRule(new InvokeRule(methodName, args));
        return this;
    }

    /**
     * Add a not animated rule to invoke a {@link java.lang.reflect.Method} of View
     *
     * @param viewID     target view's id
     * @param methodName the name of the method
     * @param args       the arguments used for the method call
     * @see InvokeRule
     */
    public AXAnimation invoke(int viewID, String methodName, Object... args) {
        createRule(new InvokeRule(viewID, methodName, args));
        return this;
    }

    /**
     * Add a not animated rule to invoke a {@link java.lang.reflect.Method} of View
     *
     * @param view       target view
     * @param methodName the name of the method
     * @param args       the arguments used for the method call
     * @see InvokeRule
     */
    public AXAnimation invoke(View view, String methodName, Object... args) {
        createRule(new InvokeRule(view, methodName, args));
        return this;
    }

    /**
     * Add a not animated rule to set a {@link java.lang.reflect.Field} of View
     *
     * @param fieldName the field name
     * @param value     the new value for the field of targetView
     *                  being modified
     * @see UpdateFieldRule
     */
    public AXAnimation fieldSet(String fieldName, Object value) {
        createRule(new UpdateFieldRule(value, fieldName));
        return this;
    }

    /**
     * Add a not animated rule to set a {@link java.lang.reflect.Field} of View
     *
     * @param viewID    target view's id
     * @param fieldName the field name
     * @param value     the new value for the field of targetView
     *                  being modified
     * @see UpdateFieldRule
     */
    public AXAnimation fieldSet(int viewID, String fieldName, Object value) {
        createRule(new UpdateFieldRule(viewID, value, fieldName));
        return this;
    }

    /**
     * Add a not animated rule to set a {@link java.lang.reflect.Field} of View
     *
     * @param view      target view
     * @param fieldName the field name
     * @param value     the new value for the field of targetView
     *                  being modified
     * @see UpdateFieldRule
     */
    public AXAnimation fieldSet(View view, String fieldName, Object value) {
        createRule(new UpdateFieldRule(view, value, fieldName));
        return this;
    }

    /**
     * Add a custom rule to animate a {@link java.lang.reflect.Field} of View
     *
     * @param fieldName  the field name
     * @param invalidate True if this view should invalidate by updating the field, false otherwise.
     * @param evaluator  A TypeEvaluator that will be called on each animation frame to
     *                   provide the necessary interpolation between the Object values to derive the animated
     *                   value.
     * @param values     A set of values that the animation will animate between over time.
     * @see UpdateFieldAnimatorRule
     */
    @SafeVarargs
    public final <T> AXAnimation fieldAnimatorSet(String fieldName, boolean invalidate, TypeEvaluator<T> evaluator, T... values) {
        createRule(new UpdateFieldAnimatorRule<>(fieldName, null, evaluator, invalidate, values));
        return this;
    }

    /**
     * Add a custom rule to animate a {@link java.lang.reflect.Field} of View
     *
     * @param fieldName  the field name
     * @param listener   listen to Animated value
     * @param invalidate True if this view should invalidate by updating the field, false otherwise.
     * @param evaluator  A TypeEvaluator that will be called on each animation frame to
     *                   provide the necessary interpolation between the Object values to derive the animated
     *                   value.
     * @param values     A set of values that the animation will animate between over time.
     * @see UpdateFieldAnimatorRule
     */
    @SafeVarargs
    public final <T> AXAnimation fieldAnimatorSet(String fieldName, AXAnimatorUpdateListener<T> listener, boolean invalidate, TypeEvaluator<T> evaluator, T... values) {
        createRule(new UpdateFieldAnimatorRule<>(fieldName, listener, evaluator, invalidate, values));
        return this;
    }

    // *************** Not Animated Rules ***************

    /**
     * Add a rule to bring view to front.
     *
     * @see View#bringToFront()
     */
    public AXAnimation bringViewToFront() {
        createRule(new RuleBringToFront());
        return this;
    }

    /**
     * Add a rule to send view to back.
     */
    public AXAnimation sendViewToBack() {
        createRule(new RuleSendToBack());
        return this;
    }

    /**
     * Add a rule to bring view to front.
     *
     * @param viewID target view's id
     * @see View#bringToFront()
     */
    public AXAnimation bringViewToFront(@IdRes int viewID) {
        createRule(new RuleBringToFront(viewID));
        return this;
    }

    /**
     * Add a rule to send view to back.
     *
     * @param viewID target view's id
     */
    public AXAnimation sendViewToBack(@IdRes int viewID) {
        if (viewID == PARENT_ID)
            throw new IllegalArgumentException("viewID must be a real id, can't");
        createRule(new RuleSendToBack(viewID));
        return this;
    }

    /**
     * Add a rule to bring view to front.
     *
     * @param view target view
     * @see View#bringToFront()
     */
    public AXAnimation bringViewToFront(View view) {
        createRule(new RuleBringToFront(view));
        return this;
    }

    /**
     * Add a rule to send view to back.
     *
     * @param view target view
     */
    public AXAnimation sendViewToBack(View view) {
        createRule(new RuleSendToBack(view));
        return this;
    }

    // *************** Smart Rules ***************

    /**
     * RotateX 180°
     *
     * @see #rotationX(Float...)
     */
    public AXAnimation flipHorizontal() {
        return flipHorizontal(180);
    }

    /**
     * RotateY 180°
     *
     * @see #rotationY(Float...)
     */
    public AXAnimation flipVertical() {
        return flipVertical(180);
    }

    /**
     * RotateX
     *
     * @param finalRotation The final degrees of X rotation
     * @see #rotationX(Float...)
     */
    public AXAnimation flipHorizontal(float finalRotation) {
        return rotationX(0f, 15f, -15f, finalRotation);
    }

    /**
     * RotateY
     *
     * @param finalRotation The final degrees of Y rotation
     * @see #rotationY(Float...)
     */
    public AXAnimation flipVertical(float finalRotation) {
        return rotationY(0f, 15f, -15f, finalRotation);
    }

    /**
     * RotateX from 0° to 90° and changes visibility to {@link View#GONE}
     *
     * @see #rotationX(Float...)
     * @see #visibility(int)
     */
    public AXAnimation flipHorizontalToHide() {
        return flipHorizontalToHide(View.GONE);
    }

    /**
     * RotateY from 0° to 90° and changes visibility to {@link View#GONE}
     *
     * @see #rotationY(Float...)
     * @see #visibility(int)
     */
    public AXAnimation flipVerticalToHide() {
        return flipVerticalToHide(View.GONE);
    }

    /**
     * RotateX from 0° to 90° and changes visibility
     *
     * @param visibility One of {@link View#INVISIBLE} or {@link View#GONE}.
     * @see #rotationX(Float...)
     * @see #visibility(int)
     */
    public AXAnimation flipHorizontalToHide(int visibility) {
        if (visibility != View.GONE && visibility != View.INVISIBLE)
            throw new RuntimeException("Unexpected visibility!");

        long d = data.getDuration();
        long d2 = data.getDelay();
        flipHorizontal(90);
        delay(d + d2);
        duration(1);
        visibility(visibility);
        rotationX(0f);
        duration(d);
        delay(d2);
        return this;
    }

    /**
     * RotateY from 0° to 90° and changes visibility
     *
     * @param visibility One of {@link View#INVISIBLE} or {@link View#GONE}.
     * @see #rotationY(Float...)
     * @see #visibility(int)
     */
    public AXAnimation flipVerticalToHide(int visibility) {
        if (visibility != View.GONE && visibility != View.INVISIBLE)
            throw new RuntimeException("Unexpected visibility!");

        long d = data.getDuration();
        long d2 = data.getDelay();
        flipVertical(90);
        delay(d + d2);
        duration(1);
        visibility(visibility);
        rotationY(0f);
        duration(d);
        delay(d2);
        return this;
    }

    /**
     * RotateX from 90° to 0° and changes visibility to {@link View#VISIBLE}
     *
     * @see #rotationX(Float...)
     * @see #visibility(int)
     */
    public AXAnimation flipHorizontalToShow() {
        long d = data.getDuration();
        boolean f = data.isFirstValueFromView();
        firstValueFromView(false);
        rotationX(90f, -15f, 15f, 0f);
        duration(1);
        visibility(View.VISIBLE);
        duration(d);
        firstValueFromView(f);
        return this;
    }

    /**
     * RotateY from 90° to 0° and changes visibility to {@link View#VISIBLE}
     *
     * @see #rotationY(Float...)
     * @see #visibility(int)
     */
    public AXAnimation flipVerticalToShow() {
        long d = data.getDuration();
        boolean f = data.isFirstValueFromView();
        firstValueFromView(false);
        rotationY(90f, -15f, 15f, 0f);
        duration(1);
        visibility(View.VISIBLE);
        duration(d);
        firstValueFromView(f);
        return this;
    }

    /**
     * @see #alpha(Float...)
     */
    public AXAnimation flash() {
        return alpha(1f, 0f, 1f, 0f, 1f);
    }

    /**
     * @see #bounceOut()
     * @see #alpha(Float...)
     * @see #scaleX(Float...)
     * @see #scaleY(Float...)
     */
    public AXAnimation bounceIn() {
        alpha(0f, 1f, 1f, 1f);
        scaleX(0.3f, 1.05f, 0.9f, 1f);
        scaleY(0.3f, 1.05f, 0.9f, 1f);
        return this;
    }

    /**
     * @see #bounceIn()
     * @see #alpha(Float...)
     * @see #scaleX(Float...)
     * @see #scaleY(Float...)
     */
    public AXAnimation bounceOut() {
        scaleY(1f, 0.9f, 1.05f, 0.3f);
        scaleX(1f, 0.9f, 1.05f, 0.3f);
        alpha(1f, 1f, 1f, 0f);
        return this;
    }

    /**
     * @see #fadeOut()
     * @see #alpha(Float...)
     */
    public AXAnimation fadeIn() {
        return alpha(1f);
    }

    /**
     * @see #fadeIn()
     * @see #alpha(Float...)
     */
    public AXAnimation fadeOut() {
        return alpha(0f);
    }

    /**
     * @see #translationX(Float...)
     */
    public AXAnimation shake() {
        if (!measureUnitEnabled)
            return shake(2, dp(5));
        else
            return shake(2, 5);
    }

    /**
     * @param nbShake     Numbers of shake for CycleInterpolator
     * @param translation Shake translation
     * @see #cycleInterpolator(float)
     * @see #translationX(Float...)
     */
    public AXAnimation shake(float nbShake, float translation) {
        TimeInterpolator old = data.getInterpolator();
        cycleInterpolator(nbShake);
        float translationValue = getValue(false, false, translation);
        translationX(-translationValue);
        translationX(translationValue);
        interpolator(old);
        return this;
    }

    /**
     * @see #translationY(Float...)
     */
    public AXAnimation shakeY() {
        if (!measureUnitEnabled)
            return shakeY(2, dp(5));
        else
            return shakeY(2, 5);
    }

    /**
     * @param nbShake     Numbers of shake for CycleInterpolator
     * @param translation Shake translation
     * @see #cycleInterpolator(float)
     * @see #translationY(Float...)
     */
    public AXAnimation shakeY(float nbShake, float translation) {
        TimeInterpolator old = data.getInterpolator();
        cycleInterpolator(nbShake);
        float translationValue = getValue(false, false, translation);
        translationY(-translationValue);
        translationY(translationValue);
        interpolator(old);
        return this;
    }

    /**
     * @see #scale(Float...)
     */
    public AXAnimation press() {
        return press(0.95f);
    }

    /**
     * @param depth scale value
     * @see #scale(Float...)
     */
    public AXAnimation press(float depth) {
        long oldDuration = data.getDuration();
        int oldRepeat = data.getRepeatCount();
        int oldRepeatMode = data.getRepeatMode();
        duration(oldDuration / 2);
        repeatCount(1);
        repeatMode(REVERSE);
        scale(depth);
        repeatCount(oldRepeat);
        repeatMode(oldRepeatMode);
        duration(oldDuration);
        return this;
    }

    // *************** OtherAnimationRule ***************

    /**
     * Add a {@link Rule} to start another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animation the other animation to start
     */
    public AXAnimation startOtherAnimation(AXAnimation animation) {
        createRule(new OtherAnimationRule(animation));
        return this;
    }

    /**
     * Add a {@link Rule} to start another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animation the other animation to start
     * @param view      TargetView of animation
     */
    public AXAnimation startOtherAnimation(AXAnimation animation, View view) {
        createRule(new OtherAnimationRule(animation, view));
        return this;
    }

    /**
     * Add a {@link Rule} to start another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animation the other animation to start
     * @param viewID    ID of Animation's TargetView
     */
    public AXAnimation startOtherAnimation(AXAnimation animation, int viewID) {
        createRule(new OtherAnimationRule(animation, viewID));
        return this;
    }

    /**
     * Add a {@link Rule} to start another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animationName name of the other animation to start
     */
    public AXAnimation startOtherAnimation(String animationName) {
        return startOtherAnimation(AXAnimation.getAnimation(animationName));
    }

    /**
     * Add a {@link Rule} to start another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animationName name of the other animation to start
     * @param view          TargetView of animation
     */
    public AXAnimation startOtherAnimation(String animationName, View view) {
        return startOtherAnimation(AXAnimation.getAnimation(animationName), view);
    }

    /**
     * Add a {@link Rule} to start another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animationName name of the other animation to start
     * @param viewID        ID of Animation's TargetView
     */
    public AXAnimation startOtherAnimation(String animationName, int viewID) {
        return startOtherAnimation(AXAnimation.getAnimation(animationName), viewID);
    }

    /**
     * Add a {@link Rule} to reverse another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animation the other animation to reverse
     */
    public AXAnimation reverseOtherAnimation(AXAnimation animation) {
        createRule(new ReverseRule(new OtherAnimationRule(animation)));
        return this;
    }

    /**
     * Add a {@link Rule} to reverse another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animation the other animation to reverse
     * @param view      TargetView of animation
     */
    public AXAnimation reverseOtherAnimation(AXAnimation animation, View view) {
        createRule(new ReverseRule(new OtherAnimationRule(animation, view)));
        return this;
    }

    /**
     * Add a {@link Rule} to reverse another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animation the other animation to reverse
     * @param viewID    ID of Animation's TargetView
     */
    public AXAnimation reverseOtherAnimation(AXAnimation animation, int viewID) {
        createRule(new ReverseRule(new OtherAnimationRule(animation, viewID)));
        return this;
    }

    /**
     * Add a {@link Rule} to reverse another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animationName name of the other animation to reverse
     */
    public AXAnimation reverseOtherAnimation(String animationName) {
        return reverseOtherAnimation(AXAnimation.getAnimation(animationName));
    }

    /**
     * Add a {@link Rule} to reverse another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animationName name of the other animation to reverse
     * @param view          TargetView of animation
     */
    public AXAnimation reverseOtherAnimation(String animationName, View view) {
        return reverseOtherAnimation(AXAnimation.getAnimation(animationName), view);
    }

    /**
     * Add a {@link Rule} to reverse another AXAnimation during this animation.
     * - AnimatorValues only supports delay.
     *
     * @param animationName name of the other animation to reverse
     * @param viewID        ID of Animation's TargetView
     */
    public AXAnimation reverseOtherAnimation(String animationName, int viewID) {
        return reverseOtherAnimation(AXAnimation.getAnimation(animationName), viewID);
    }

    // *************** ReverseRule ***************

    /**
     * Specifies whether the reverse rule should use new animator values or keep using old ones.
     *
     * @param newValues True if ReverseRules should use their own animator values, false otherwise.
     *                  by default it's false
     */
    public AXAnimation applyAnimatorForReverseRules(boolean newValues) {
        this.applyNewAnimatorForReverseRules = newValues;
        return this;
    }

    /**
     * Specifies whether the reverse rule should store previous data or not
     * default: true
     *
     * @param keepOldData True if ReverseRules should use store old data and reverse them later,
     *                    false otherwise. by default it's true
     */
    public AXAnimation shouldReverseRulesKeepOldData(boolean keepOldData) {
        this.shouldReverseRulesKeepOldData = keepOldData;
        return this;
    }

    /**
     * Reverse previous rule and add it to the opened section.
     */
    public AXAnimation reversePreviousRule() {
        if (tmpRules.size() > 0)
            return reverseRule(tmpRules.size() - 1);
        else if (rules.size() > 0)
            return reverseRuleOnSection(-1, rules.size() - 1);
        else
            throw new RuntimeException("There is no rule!");
    }

    /**
     * Reverse a rule and add it to the opened section.
     *
     * @param index index of rule in opened section
     */
    public AXAnimation reverseRule(int index) {
        return reverseRule(tmpRules.get(index));
    }

    /**
     * Reverse a rule and add it to the opened section.
     *
     * @param rule rule to reverse
     */
    public AXAnimation reverseRule(Rule<?> rule) {
        createReverseRule(ReverseRule.reverseRule(rule, shouldReverseRulesKeepOldData));
        return this;
    }

    /**
     * Reverse a rule and add it to the opened section.
     *
     * @param ruleIndex   index of target rule to reverse
     * @param ruleSection index of rule's section
     */
    public AXAnimation reverseRuleOnSection(int ruleIndex, int ruleSection) {
        return reverseRuleOnSection(ruleIndex, ruleSection, false);
    }

    private AXAnimation reverseRuleOnSection(int ruleIndex, int ruleSection, boolean a) {
        if (ruleSection < 0)
            return reverseRuleOnSection(ruleIndex, 0, true);
        else if (ruleSection >= rules.size())
            return reverseRuleOnSection(ruleIndex, rules.size() - 1, false);

        RuleSection s = getRuleSection(ruleSection);
        if (s instanceof WaitRule)
            return reverseRuleOnSection(ruleIndex, a ? ruleSection + 1 : ruleSection - 1, a);

        return reverseRuleOnSection(ruleIndex, s);
    }

    /**
     * Reverse a rule and add it to the opened section.
     *
     * @param ruleIndex   index of target rule to reverse
     * @param ruleSection target rule's section
     */
    public AXAnimation reverseRuleOnSection(int ruleIndex, RuleSection ruleSection) {
        if (ruleIndex >= 0) {
            return reverseRuleOnSection(ruleSection.getRules()[ruleIndex], ruleSection);
        } else {
            return reverseRuleOnSection(ruleSection.getRules()[ruleSection.getRules().length + ruleIndex], ruleSection);
        }
    }

    /**
     * Reverse a rule and add it to the opened section.
     *
     * @param rule    target rule to reverse
     * @param section target rule's section
     */
    public AXAnimation reverseRuleOnSection(Rule<?> rule, RuleSection section) {
        createReverseRule(section, ReverseRule.reverseRule(rule, shouldReverseRulesKeepOldData));
        return this;
    }

    /**
     * Reverse all previous RuleSection's rules and import them into this opened section.
     * it won't close the opened section!
     */
    public AXAnimation reversePreviousRuleSection() {
        return reverseRuleSection(rules.size() - 1);
    }

    /**
     * Reverse all previous RuleSection's rules and import them into this opened section.
     * it won't close the opened section!
     *
     * @param section index of ruleSection to reverse
     */
    public AXAnimation reverseRuleSection(int section) {
        return reverseRuleSection(section, false);
    }

    private AXAnimation reverseRuleSection(int ruleSection, boolean a) {
        if (ruleSection < 0)
            return reverseRuleSection(0, true);
        else if (ruleSection >= rules.size())
            return reverseRuleSection(rules.size() - 1, false);

        RuleSection s = getRuleSection(ruleSection);
        if (s instanceof WaitRule)
            return reverseRuleSection(a ? ruleSection + 1 : ruleSection - 1, a);

        return reverseRuleSection(s);
    }

    /**
     * Reverse all previous RuleSection's rules and import them into this opened section.
     * it won't close the opened section!
     *
     * @param section ruleSection to reverse
     */
    public AXAnimation reverseRuleSection(RuleSection section) {
        if (section.getRules() == null)
            return this;

        for (int i = 0; i < section.getRules().length; i++)
            createReverseRule(section, ReverseRule.reverseRule(section.getRules()[i], shouldReverseRulesKeepOldData));

        return this;
    }

    // *************** LiveVar updater ***************

    /**
     * Adds a {@link LiveVarUpdater} to set of updaters.
     */
    public AXAnimation updateLiveVar(LiveVarUpdater liveVarUpdater) {
        liveVarUpdaters.add(liveVarUpdater);
        return this;
    }

    /**
     * @return Returns all liveVarUpdaters.
     */
    public List<LiveVarUpdater> getALlLiveVarUpdaters() {
        return liveVarUpdaters;
    }

    /**
     * Adds a {@link NotAnimatedRule} to update {@link LiveVar}
     */
    public AXAnimation updateLiveVar(final LiveVar<?> var, final Object value) {
        createRule(new NotAnimatedRule<Void>(null) {
            @Override
            public void apply(View targetView) {
                var.update(value);
            }

            @Override
            public String getRuleName() {
                return "UpdateLiveVar_NotAnimatedRule";
            }
        });
        return this;
    }

    /**
     * Adds a {@link NotAnimatedRule} to update {@link LiveVar}
     */
    public AXAnimation updateLiveVar(final LiveVar<?> var, final Object... values) {
        return updateLiveVar(var, (Object) values);
    }

    // *************** Get Ready For New Section ***************

    /**
     * Repeats previous rule section
     *
     * @param repeatCount the number of times the animation should be repeated
     * @param repeatMode  {@link #RESTART} or {@link #REVERSE}
     * @param delay       delay between each section
     */
    public AXAnimation repeatPreviousRuleSection(@IntRange(from = 1) int repeatCount, @RepeatMode int repeatMode, long delay) {
        return repeatRuleSection(repeatCount, repeatMode, delay, rules.size() - 1);
    }

    /**
     * Repeats a rule section
     *
     * @param repeatCount the number of times the animation should be repeated
     * @param repeatMode  {@link #RESTART} or {@link #REVERSE}
     * @param delay       delay between each section
     * @param section     index of ruleSection to repeat
     */
    public AXAnimation repeatRuleSection(@IntRange(from = 1) int repeatCount, @RepeatMode int repeatMode, long delay, int section) {
        return repeatRuleSection(repeatCount, repeatMode, delay, section, false);
    }

    private AXAnimation repeatRuleSection(int repeatCount, int repeatMode, long delay, int ruleSection, boolean a) {
        if (ruleSection < 0)
            return repeatRuleSection(repeatCount, repeatMode, delay, 0, true);
        else if (ruleSection >= rules.size())
            return repeatRuleSection(repeatCount, repeatMode, delay, rules.size() - 1, false);

        RuleSection s = getRuleSection(ruleSection);
        if (s instanceof WaitRule)
            return repeatRuleSection(repeatCount, repeatMode, delay, a ? ruleSection + 1 : ruleSection - 1, a);

        return repeatRuleSection(repeatCount, repeatMode, delay, s);
    }

    /**
     * Repeats a rule section
     *
     * @param repeatCount the number of times the animation should be repeated
     * @param repeatMode  {@link #RESTART} or {@link #REVERSE}
     * @param delay       delay between each section
     * @param section     ruleSection to repeat
     */
    public AXAnimation repeatRuleSection(@IntRange(from = 1) int repeatCount, @RepeatMode int repeatMode, long delay, RuleSection section) {
        for (int i = 0; i < repeatCount; i++) {
            if (repeatMode == RESTART || i % 2 != 0) {
                addRuleSection(section);
            } else {
                addRuleSection(new ReverseRuleSection(section, shouldReverseRulesKeepOldData));
            }

            if (delay > 0)
                addRuleSection(new WaitRule(delay));
            else if (delay < 0)
                addRuleSection(new ReverseWaitRule(delay));
        }
        return this;
    }

    /**
     * Close this section and open a new one.
     */
    public AXAnimation nextSection() {
        doneRule();
        return this;
    }

    /**
     * Close this section and open a new one with a reverse delay.
     */
    public AXAnimation nextSectionImmediate() {
        doneRule();
        addSection(new ReverseWaitRule(getRuleSectionTotalDuration(rules.get(rules.size() - 1))));
        delay(0);
        return this;
    }

    /**
     * Close this section and open a new one with delay
     *
     * @param delay Delays before starting next section
     */
    public AXAnimation nextSectionWithDelay(long delay) {
        doneRule();
        addSection(new WaitRule(delay));
        delay(0);
        return this;
    }

    /**
     * Close this section and open a new one with reverse delay
     *
     * @param delay will start while previous section is running
     */
    public AXAnimation nextSectionWithReverseDelay(long delay) {
        doneRule();
        addSection(new ReverseWaitRule(delay));
        delay(0);
        return this;
    }

    /**
     * Add a new delay rule section (before opened section).
     *
     * @param duration Delays before starting opened section
     */
    public AXAnimation waitBefore(long duration) {
        addSection(new WaitRule(duration));
        return this;
    }

    /**
     * Add a new delay rule section (before opened section).
     * Animator will start next section whenever {@link com.aghajari.axanimation.rules.WaitNotifyRule.Listener#isDone(View)} return true!
     */
    public AXAnimation waitNotifyBefore(WaitNotifyRule.Listener listener) {
        addSection(new WaitNotifyRule(listener));
        return this;
    }

    /**
     * Add a new delay rule section (before opened section).
     * Animator will start next section whenever {@link com.aghajari.axanimation.rules.WaitNotifyRule.Listener#isDone(View)} return true!
     *
     * @param delay Delays between each notify
     */
    public AXAnimation waitNotifyBefore(long delay, WaitNotifyRule.Listener listener) {
        addSection(new WaitNotifyRule(delay, listener));
        return this;
    }

    /**
     * Gets all rule sections of this animation except the opened section.
     *
     * @return all rule sections of this animation.
     */
    public List<RuleSection> getAllRuleSections() {
        return rules;
    }

    /**
     * Gets all rules on the opened section.
     *
     * @return all rules on current section
     */
    public List<Rule<?>> getAllRuleOnThisSections() {
        return tmpRules;
    }


    // *************** Get Ready For New Rule ***************

    /**
     * Denotes that the next rule should only be called on the given API level or higher.
     *
     * @param api The API level to require.
     */
    public AXAnimation requiresApi(int api) {
        this.nextRuleRequiresApi = api;
        return this;
    }

    private boolean shouldSkipNextRule(Rule<?> rule) {
        if (nextRuleRequiresApi == -1)
            return false;

        int api = nextRuleRequiresApi;
        nextRuleRequiresApi = -1;
        boolean s = Build.VERSION.SDK_INT < api;
        if (s)
            addToTmpRules(new SkippedRule(rule, "RequiresApi " + api));

        return s;
    }

    /**
     * Sets a RuleWrapper for more customizations.
     * <p>
     * {@link ReverseRule} Will reverse next rules.
     * {@link DebugRuleWrapper} Will debug next rules.
     *
     * @param wrapper A wrapper for next rules.
     */
    public AXAnimation wrap(Class<? extends RuleWrapper> wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    /**
     * Sets a RuleSectionWrapper for more customizations.
     * <p>
     * {@link ReverseRuleSection} Will reverse next sections.
     * {@link DebugRuleSectionWrapper} Will debug next sections.
     *
     * @param wrapper A wrapper for next rule sections.
     */
    public AXAnimation wrap(Class<? extends RuleSectionWrapper> wrapper, boolean wrapDelays) {
        this.wrapperSection = wrapper;
        this.wrapDelays = wrapDelays;
        return this;
    }

    /**
     * Add a rule to opened section.
     */
    private void createRule(Rule<?> rule) {
        if (rule.getAnimatorValues() == null)
            rule.setAnimatorValues(data.clone());

        if (shouldSkipNextRule(rule))
            return;

        addToTmpRules(rule);
    }

    /**
     * Add a reverse rule to opened section.
     */
    private void createReverseRule(Rule<?> rule) {
        if (applyNewAnimatorForReverseRules)
            rule.setAnimatorValues(data.clone());

        if (shouldSkipNextRule(rule))
            return;

        addToTmpRules(rule);
    }

    /**
     * Add a reverse rule to opened section.
     * animatorValues based on it's RuleSection.
     */
    private void createReverseRule(RuleSection section, Rule<?> rule) {
        if (applyNewAnimatorForReverseRules) {
            rule.setAnimatorValues(data.clone());
        } else {
            if (rule.getAnimatorValues() == null)
                rule.setAnimatorValues(section.getAnimatorValues());
        }

        if (shouldSkipNextRule(rule))
            return;

        addToTmpRules(rule);
    }

    private void addToTmpRules(Rule<?> rule) {
        if (wrapper != null) {
            try {
                tmpRules.add(wrapper.getConstructor(Rule.class).newInstance(rule));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tmpRules.add(rule);
        }
    }

    /**
     * Close opened section. (and open a new one)
     */
    private void doneRule() {
        if (tmpRules.isEmpty())
            return;

        Rule<?>[] rules = new Rule[tmpRules.size()];
        rules = tmpRules.toArray(rules);

        RuleSection section = new RuleSection(rules);
        section.setAnimatorValues(data.clone());

        addSection(section);
        tmpRules.clear();
    }

    RuleSection getRuleSection(int index) {
        RuleSection s = rules.get(index);
        if (s instanceof RuleSectionWrapper)
            return ((RuleSectionWrapper) s).getRuleSection();
        return s;
    }

    int getRealRuleSectionCount() {
        int m = -1;
        for (int i = 0; i < rules.size(); i++) {
            if (!(getRuleSection(i) instanceof WaitRule))
                m++;
        }
        return Math.max(m, 0);
    }

    private void addSection(RuleSection section) {
        if (section instanceof WaitRule && !wrapDelays) {
            this.rules.add(section);
            return;
        }

        if (!(section instanceof WaitRule)) {
            section.withStartAction(sectionStartListener);
            section.withEndAction(sectionEndListener);
            sectionEndListener = null;
            sectionStartListener = null;
        }

        if (wrapperSection != null) {
            try {
                rules.add(wrapperSection.getConstructor(RuleSection.class).newInstance(section));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.rules.add(section);
        }
    }

    // *************** PreRule ***************

    /**
     * Adds a {@link PreRule} to make a Placeholder of targetView.
     *
     * @param focusOnCopy True if AXAnimation must animate the placeholder, False otherwise.
     *                    Note that Placeholder can not have some animation rules such as
     *                    background, textColor and etc!
     */
    public AXAnimation copyOfView(boolean focusOnCopy) {
        return copyOfView(true, focusOnCopy);
    }

    /**
     * Adds a {@link PreRule} to make a Placeholder of targetView.
     *
     * @param removeCopyAtTheEnd True if Placeholder should be removed at the end, False otherwise.
     * @param focusOnCopy        True if AXAnimation must animate the placeholder, False otherwise.
     *                           Note that Placeholder can not have some animation rules such as
     *                           background, textColor and etc!
     */
    public AXAnimation copyOfView(boolean removeCopyAtTheEnd, boolean focusOnCopy) {
        return copyOfView(removeCopyAtTheEnd, focusOnCopy, null);
    }

    /**
     * Adds a {@link PreRule} to make a Placeholder of targetView.
     *
     * @param removeCopyAtTheEnd   True if Placeholder should be removed at the end, False otherwise.
     * @param focusOnCopy          True if AXAnimation must animate the placeholder, False otherwise.
     *                             Note that Placeholder can not have some animation rules such as
     *                             background, textColor and etc!
     * @param placeholderAnimation the animation that must be start for the placeholder.
     */
    public AXAnimation copyOfView(boolean removeCopyAtTheEnd, boolean focusOnCopy, @Nullable AXAnimation placeholderAnimation) {
        removePreRule(PreRuleCopyView.class);
        return addPreRule(new PreRuleCopyView(focusOnCopy, removeCopyAtTheEnd, placeholderAnimation));
    }

    /**
     * PreRule will prepare the targetView for animation.
     *
     * @param preRule the preRule to be added to the current set of preRules for this animation.
     * @see PreRule
     */
    public AXAnimation addPreRule(PreRule preRule) {
        preRules.add(preRule);
        return this;
    }

    /**
     * @param preRule the preRule to be removed from the current set of preRules for this animation.
     */
    public AXAnimation removePreRule(PreRule preRule) {
        preRules.remove(preRule);
        return this;
    }

    /**
     * @param cls Class of the PreRules to be removed from the current set of PreRules for this animation.
     */
    public AXAnimation removePreRule(Class<? extends PreRule> cls) {
        for (Iterator<PreRule> it = preRules.iterator(); it.hasNext(); ) {
            PreRule preRule = it.next();
            if (preRule.getClass().isAssignableFrom(cls))
                it.remove();
        }
        return this;
    }

    /**
     * Removes all preRules from the set of this animation.
     */
    public AXAnimation clearPreRules() {
        preRules.clear();
        return this;
    }

    // *************** Draw Rules ***************

    /**
     * Adds a rule to animate a {@link Paint}'s property.
     *
     * @param target       The Paint whose property is to be animated.
     * @param propertyName The name of the property being animated.
     * @param reset        True if the property should reset at the end.
     * @param values       A set of values that the animation will animate between over time.
     * @see PaintPropertyRule
     * @see Paint
     */
    @SafeVarargs
    public final <T> AXAnimation drawSetPaint(Paint target, String propertyName, boolean reset, T... values) {
        createRule(new PaintPropertyRule<>(target, propertyName, reset, values));
        return this;
    }

    /**
     * Adds a rule to animate a {@link Paint}'s property.
     *
     * @param target       The Paint whose property is to be animated.
     * @param propertyName The name of the property being animated.
     * @param reset        True if the property should reset at the end.
     * @param evaluator    A TypeEvaluator that will be called on each animation frame to
     *                     provide the necessary interpolation between the Object values to derive the animated
     *                     value.
     * @param values       A set of values that the animation will animate between over time.
     * @see PaintPropertyRule
     * @see Paint
     */
    @SafeVarargs
    public final <T> AXAnimation drawSetPaint(Paint target, String propertyName, boolean reset, TypeEvaluator<?> evaluator, T... values) {
        createRule(new PaintPropertyRule<>(target, propertyName, reset, evaluator, values));
        return this;
    }

    /**
     * Adds a rule to animate a {@link Paint}'s property. ({@link LiveVar})
     *
     * @param target       The Paint whose property is to be animated.
     * @param propertyName The name of the property being animated.
     * @param reset        True if the property should reset at the end.
     * @param values       A set of values that the animation will animate between over time.
     * @see PaintPropertyRule
     * @see Paint
     */
    public final <T> AXAnimation drawSetPaint(Paint target, String propertyName, boolean reset, LiveVar<T[]> values) {
        createRule(new PaintPropertyRule<>(target, propertyName, reset, values));
        return this;
    }

    /**
     * Adds a rule to animate a {@link Paint}'s property. ({@link LiveVar})
     *
     * @param target       The Paint whose property is to be animated.
     * @param propertyName The name of the property being animated.
     * @param reset        True if the property should reset at the end.
     * @param evaluator    A TypeEvaluator that will be called on each animation frame to
     *                     provide the necessary interpolation between the Object values to derive the animated
     *                     value.
     * @param values       A set of values that the animation will animate between over time.
     * @see PaintPropertyRule
     * @see Paint
     */
    public final <T> AXAnimation drawSetPaint(Paint target, String propertyName, boolean reset, TypeEvaluator<?> evaluator, LiveVar<T[]> values) {
        createRule(new PaintPropertyRule<>(target, propertyName, reset, evaluator, values));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to set {@link Canvas} transformation matrix.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be called before calling
     *                    the super.dispatchDraw(Canvas).
     * @param values      The matrix to replace the canvas matrix with. (AnimatorValues)
     * @see MatrixRule
     * @see Canvas#setMatrix(Matrix)
     */
    public AXAnimation drawSetMatrix(final String key, final boolean drawOnFront, Matrix... values) {
        createRule(new MatrixRule(key, drawOnFront, values));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a path.
     *
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param path        The path to be drawn
     * @see PathRule
     * @see Canvas#drawPath(Path, Paint)
     */
    public AXAnimation drawPath(final String key, final boolean drawOnFront,
                                Paint paint, Path path) {
        return drawPath(key, drawOnFront, Gravity.START, paint, path);
    }

    /**
     * Adds a {@link DrawRule} to draw a path.
     *
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param lineGravity {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param path        The path to be drawn
     * @see PathRule
     * @see Canvas#drawPath(Path, Paint)
     */
    public AXAnimation drawPath(final String key, final boolean drawOnFront, @LineGravity int lineGravity,
                                Paint paint, Path path) {
        createRule(new PathRule(paint, key, drawOnFront, lineGravity, path));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a line.
     *
     * @param key         A specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param lineGravity {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param paint       The paint that should use to draw
     * @param startX      The x-coordinate of the start point of the line
     * @param startY      The y-coordinate of the start point of the line
     * @param stopX       The x-coordinate of the stop point of the line
     * @param stopY       The x-coordinate of the stop point of the line
     * @see LineRule
     * @see Canvas#drawLine(float, float, float, float, Paint)
     */
    public AXAnimation drawLine(final String key, final boolean drawOnFront, @LineGravity int lineGravity,
                                Paint paint, float startX, float startY, float stopX, float stopY) {
        return drawLine(key, drawOnFront, lineGravity, paint,
                new PointF[]{
                        new PointF(startX, startY),
                        new PointF(stopX, stopY)
                });
    }

    /**
     * Adds a {@link DrawRule} to draw a line.
     *
     * @param key         A specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param lineGravity {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param paint       The paint that should use to draw
     * @param values      Array of <code>PointF[]</code>,
     *                    the PointF[] must contains two point for start and end of the line.
     * @see LineRule
     * @see Canvas#drawLine(float, float, float, float, Paint)
     * @see #drawLine(String, boolean, int, Paint, float, float, float, float)
     */
    public AXAnimation drawLine(final String key, final boolean drawOnFront, @LineGravity int lineGravity,
                                Paint paint, PointF[]... values) {
        createRule(new LineRule(paint, key, drawOnFront, lineGravity, getValue(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a line. ({@link LiveSizePoint})
     *
     * @param key         A specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param lineGravity {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param paint       The paint that should use to draw
     * @param startX      The x-coordinate of the start point of the line
     * @param startY      The y-coordinate of the start point of the line
     * @param stopX       The x-coordinate of the stop point of the line
     * @param stopY       The x-coordinate of the stop point of the line
     * @see LiveLineRule
     * @see Canvas#drawLine(float, float, float, float, Paint)
     */
    public AXAnimation drawLine(final String key, final boolean drawOnFront, @LineGravity int lineGravity,
                                Paint paint, LiveSize startX, LiveSize startY, LiveSize stopX, LiveSize stopY) {
        return drawLine(key, drawOnFront, lineGravity, paint,
                new LiveSizePoint[]{
                        new LiveSizePoint(startX, startY),
                        new LiveSizePoint(stopX, stopY)
                });
    }

    /**
     * Adds a {@link DrawRule} to draw a line. ({@link LiveSizePoint})
     *
     * @param key         A specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param lineGravity {@link Gravity#START}, {@link Gravity#END} or {@link Gravity#CENTER}
     * @param paint       The paint that should use to draw
     * @param values      Array of <code>LiveSizePoint[]</code>,
     *                    the LiveSizePoint[] must contains two point for start and end of the line.
     * @see LiveLineRule
     * @see Canvas#drawLine(float, float, float, float, Paint)
     * @see #drawLine(String, boolean, int, Paint, LiveSize, LiveSize, LiveSize, LiveSize)
     */
    public AXAnimation drawLine(final String key, final boolean drawOnFront, @LineGravity int lineGravity,
                                Paint paint, LiveSizePoint[]... values) {
        createRule(new LiveLineRule(paint, key, drawOnFront, lineGravity, getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an arc (part of circle).
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param cx          The x-coordinate of the center of the circle to be drawn
     * @param cy          The y-coordinate of the center of the circle to be drawn
     * @param radius      The radius of the circle to be drawn
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngles Sweep angle (in degrees) measured clockwise (Animator values)
     * @see ArcRule
     * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
     */
    public AXAnimation drawArc(String key, boolean drawOnFront, Paint paint,
                               float cx, float cy, float radius, boolean useCenter,
                               float startAngle, float... sweepAngles) {
        createRule(new ArcRule(paint, key, drawOnFront, getValue(true, cx),
                getValue(true, cy), getValue(false, false, radius),
                useCenter, startAngle, sweepAngles));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an arc (part of circle). ({@link LiveSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param cx          The x-coordinate of the center of the circle to be drawn
     * @param cy          The y-coordinate of the center of the circle to be drawn
     * @param radius      The radius of the circle to be drawn
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngles Sweep angle (in degrees) measured clockwise (Animator values)
     * @see LiveArcRule
     * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
     */
    public AXAnimation drawArc(String key, boolean drawOnFront, Paint paint,
                               LiveSize cx, LiveSize cy, float radius, boolean useCenter,
                               float startAngle, float... sweepAngles) {
        createRule(new LiveArcRule(paint, key, drawOnFront, getValue(true, cx),
                getValue(true, cy), getValue(false, false, radius),
                useCenter, startAngle, sweepAngles));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an arc (part of oval).
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param oval        The bounds of oval used to define the shape and size of the arc
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngles Sweep angle (in degrees) measured clockwise (Animator values)
     * @see ArcRule
     * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
     */
    public AXAnimation drawArc(String key, boolean drawOnFront, Paint paint,
                               RectF oval, boolean useCenter,
                               float startAngle, float... sweepAngles) {
        createRule(new ArcRule(paint, key, drawOnFront, getValue(oval),
                useCenter, startAngle, sweepAngles));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an arc (part of oval). ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param oval        The bounds of oval used to define the shape and size of the arc
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @param sweepAngles Sweep angle (in degrees) measured clockwise (Animator values)
     * @see LiveArcRule
     * @see Canvas#drawArc(RectF, float, float, boolean, Paint)
     */
    public AXAnimation drawArc(String key, boolean drawOnFront, Paint paint,
                               LayoutSize oval, boolean useCenter,
                               float startAngle, float... sweepAngles) {
        createRule(new LiveArcRule(paint, key, drawOnFront, getValue(oval),
                useCenter, startAngle, sweepAngles));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a circle.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param cx          The x-coordinate of the center of the circle to be drawn
     * @param cy          The y-coordinate of the center of the circle to be drawn
     * @param radius      The radius of the circle to be drawn
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @see #drawArc(String, boolean, Paint, float, float, float, boolean, float, float...)
     * @see Canvas#drawCircle(float, float, float, Paint)
     */
    public AXAnimation drawCircle(String key, boolean drawOnFront, Paint paint,
                                  float cx, float cy, float radius, boolean useCenter,
                                  float startAngle) {
        createRule(new ArcRule(paint, key, drawOnFront, getValue(true, cx),
                getValue(true, cy), getValue(false, false, radius),
                useCenter, startAngle, 360));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a circle. ({@link LiveSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param cx          The x-coordinate of the center of the circle to be drawn
     * @param cy          The y-coordinate of the center of the circle to be drawn
     * @param radius      The radius of the circle to be drawn
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @see #drawArc(String, boolean, Paint, LiveSize, LiveSize, float, boolean, float, float...)
     * @see Canvas#drawCircle(float, float, float, Paint)
     */
    public AXAnimation drawCircle(String key, boolean drawOnFront, Paint paint,
                                  LiveSize cx, LiveSize cy, float radius, boolean useCenter,
                                  float startAngle) {
        createRule(new LiveArcRule(paint, key, drawOnFront, getValue(true, cx),
                getValue(true, cy), getValue(false, false, radius),
                useCenter, startAngle, 360));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an oval.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param oval        The bounds of oval used to define the shape and size of the arc
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @see #drawArc(String, boolean, Paint, RectF, boolean, float, float...)
     * @see Canvas#drawOval(RectF, Paint)
     */
    public AXAnimation drawOval(String key, boolean drawOnFront, Paint paint,
                                RectF oval, boolean useCenter,
                                float startAngle) {
        createRule(new ArcRule(paint, key, drawOnFront, getValue(oval),
                useCenter, startAngle, 360));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an oval. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param oval        The bounds of oval used to define the shape and size of the arc
     * @param useCenter   If true, include the center of the oval in the arc, and close it if it is
     *                    being stroked. This will draw a wedge
     * @param startAngle  Starting angle (in degrees) where the arc begins
     * @see #drawArc(String, boolean, Paint, LayoutSize, boolean, float, float...)
     * @see Canvas#drawOval(RectF, Paint)
     */
    public AXAnimation drawOval(String key, boolean drawOnFront, Paint paint,
                                LayoutSize oval, boolean useCenter,
                                float startAngle) {
        createRule(new LiveArcRule(paint, key, drawOnFront, getValue(oval),
                useCenter, startAngle, 360));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an oval rect.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rectangle bounds of the oval to be drawn. (Animator values)
     * @see OvalRectRule
     * @see Canvas#drawOval(RectF, Paint)
     */
    public AXAnimation drawOvalRect(String key, boolean drawOnFront, Paint paint, int gravity, RectF... values) {
        createRule(new OvalRectRule(paint, key, drawOnFront, gravity, getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an oval rect.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param left        The left side of the oval to be drawn
     * @param top         The top side of the oval to be drawn
     * @param right       The right side of the oval to be drawn
     * @param bottom      The bottom side of the oval to be drawn
     * @see OvalRectRule
     * @see Canvas#drawOval(float, float, float, float, Paint)
     */
    public AXAnimation drawOvalRect(String key, boolean drawOnFront, Paint paint, int gravity,
                                    float left, float top, float right, float bottom) {
        return drawOvalRect(key, drawOnFront, paint, gravity, new RectF(left, top, right, bottom));
    }

    /**
     * Adds a {@link DrawRule} to draw an oval rect. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rectangle bounds of the oval to be drawn. (Animator values)
     * @see LiveOvalRectRule
     * @see Canvas#drawOval(RectF, Paint)
     */
    public AXAnimation drawOvalRect(String key, boolean drawOnFront, Paint paint, int gravity, LayoutSize... values) {
        createRule(new LiveOvalRectRule(paint, key, drawOnFront, gravity, getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw an oval rect. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param left        The left side of the oval to be drawn
     * @param top         The top side of the oval to be drawn
     * @param right       The right side of the oval to be drawn
     * @param bottom      The bottom side of the oval to be drawn
     * @see LiveOvalRectRule
     * @see Canvas#drawOval(float, float, float, float, Paint)
     */
    public AXAnimation drawOvalRect(String key, boolean drawOnFront, Paint paint, int gravity,
                                    LiveSize left, LiveSize top, LiveSize right, LiveSize bottom) {
        return drawOvalRect(key, drawOnFront, paint, gravity, new LayoutSize(left, top, right, bottom));
    }

    /**
     * Adds a {@link DrawRule} to draw a rect.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rect to be drawn. (Animator values)
     * @see RectRule
     * @see Canvas#drawRect(RectF, Paint)
     */
    public AXAnimation drawRect(String key, boolean drawOnFront, Paint paint, int gravity, RectF... values) {
        createRule(new RectRule(paint, key, drawOnFront, gravity, getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a rect.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param left        The left side of the rectangle to be drawn
     * @param top         The top side of the rectangle to be drawn
     * @param right       The right side of the rectangle to be drawn
     * @param bottom      The bottom side of the rectangle to be drawn
     * @see RectRule
     * @see Canvas#drawRect(float, float, float, float, Paint)
     */
    public AXAnimation drawRect(String key, boolean drawOnFront, Paint paint, int gravity,
                                float left, float top, float right, float bottom) {
        return drawRect(key, drawOnFront, paint, gravity, new RectF(left, top, right, bottom));
    }

    /**
     * Adds a {@link DrawRule} to draw a rect. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param values      The rect to be drawn. (Animator values)
     * @see LiveRectRule
     * @see Canvas#drawRect(RectF, Paint)
     */
    public AXAnimation drawRect(String key, boolean drawOnFront, Paint paint, int gravity, LayoutSize... values) {
        createRule(new LiveRectRule(paint, key, drawOnFront, gravity, getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a rect. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param left        The left side of the rectangle to be drawn
     * @param top         The top side of the rectangle to be drawn
     * @param right       The right side of the rectangle to be drawn
     * @param bottom      The bottom side of the rectangle to be drawn
     * @see LiveRectRule
     * @see Canvas#drawRect(float, float, float, float, Paint)
     */
    public AXAnimation drawRect(String key, boolean drawOnFront, Paint paint, int gravity,
                                LiveSize left, LiveSize top, LiveSize right, LiveSize bottom) {
        return drawRect(key, drawOnFront, paint, gravity, new LayoutSize(left, top, right, bottom));
    }

    /**
     * Adds a {@link DrawRule} to draw a round rect.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param rx          The x-radius of the oval used to round the corners
     * @param ry          The y-radius of the oval used to round the corners
     * @param values      The rectangular bounds of the roundRect to be drawn
     * @see RoundRectRule
     * @see Canvas#drawRoundRect(RectF, float, float, Paint)
     */
    public AXAnimation drawRoundRect(String key, boolean drawOnFront, Paint paint, int gravity, float rx, float ry, RectF... values) {
        createRule(new RoundRectRule(paint, key, drawOnFront, gravity, getValue(rx), getValue(ry), getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a round rect.
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param rx          The x-radius of the oval used to round the corners
     * @param ry          The y-radius of the oval used to round the corners
     * @param left        The left side of the rectangle to be drawn
     * @param top         The top side of the rectangle to be drawn
     * @param right       The right side of the rectangle to be drawn
     * @param bottom      The bottom side of the rectangle to be drawn
     * @see RoundRectRule
     * @see Canvas#drawRect(float, float, float, float, Paint)
     */
    public AXAnimation drawRoundRect(String key, boolean drawOnFront, Paint paint, int gravity, float rx, float ry,
                                     float left, float top, float right, float bottom) {
        return drawRoundRect(key, drawOnFront, paint, gravity, rx, ry, new RectF(left, top, right, bottom));
    }

    /**
     * Adds a {@link DrawRule} to draw a round rect. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param rx          The x-radius of the oval used to round the corners
     * @param ry          The y-radius of the oval used to round the corners
     * @param values      The rectangular bounds of the roundRect to be drawn
     * @see LiveRoundRectRule
     * @see Canvas#drawRoundRect(RectF, float, float, Paint)
     */
    public AXAnimation drawRoundRect(String key, boolean drawOnFront, Paint paint, int gravity, float rx, float ry, LayoutSize... values) {
        createRule(new LiveRoundRectRule(paint, key, drawOnFront, gravity, getValue(rx), getValue(ry), getValues(values)));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a round rect. ({@link LayoutSize})
     *
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param paint       the paint that should use to draw
     * @param gravity     Gravity of the oval's corner that animation should start.
     * @param rx          The x-radius of the oval used to round the corners
     * @param ry          The y-radius of the oval used to round the corners
     * @param left        The left side of the rectangle to be drawn
     * @param top         The top side of the rectangle to be drawn
     * @param right       The right side of the rectangle to be drawn
     * @param bottom      The bottom side of the rectangle to be drawn
     * @see LiveRoundRectRule
     * @see Canvas#drawRect(float, float, float, float, Paint)
     */
    public AXAnimation drawRoundRect(String key, boolean drawOnFront, Paint paint, int gravity, float rx, float ry,
                                     LiveSize left, LiveSize top, LiveSize right, LiveSize bottom) {
        return drawRoundRect(key, drawOnFront, paint, gravity, rx, ry, new LayoutSize(left, top, right, bottom));
    }


    /**
     * Adds a {@link DrawRule} to draw a text.
     *
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param typing      True if should type it during animation, False otherwise.
     * @param gravity     Gravity of the origin corner.
     * @param x           The x-coordinate of origin for where to draw the text
     * @param y           The y-coordinate of origin for where to draw the text
     * @param text        The text to be drawn
     * @see TextRule
     * @see Canvas#drawText(CharSequence, int, int, float, float, Paint)
     */
    public AXAnimation drawText(final String key, boolean drawOnFront, boolean typing, Paint paint, int gravity,
                                float x, float y, CharSequence text) {
        createRule(new TextRule(paint, key, drawOnFront, typing, gravity, getValue(true, false, x), getValue(true, false, y), text));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a text. ({@link LiveSize})
     *
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param typing      True if should type it during animation, False otherwise.
     * @param gravity     Gravity of the origin corner.
     * @param x           The x-coordinate of origin for where to draw the text
     * @param y           The y-coordinate of origin for where to draw the text
     * @param text        The text to be drawn
     * @see TextRule
     * @see Canvas#drawText(CharSequence, int, int, float, float, Paint)
     */
    public AXAnimation drawText(final String key, boolean drawOnFront, boolean typing, Paint paint, int gravity,
                                LiveSize x, LiveSize y, CharSequence text) {
        createRule(new TextRule(paint, key, drawOnFront, typing, gravity, getValue(true, x), getValue(true, y), text));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a text. ({@link LiveVar})
     *
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param typing      True if should type it during animation, False otherwise.
     * @param gravity     Gravity of the origin corner.
     * @param x           The x-coordinate of origin for where to draw the text
     * @param y           The y-coordinate of origin for where to draw the text
     * @param text        The text to be drawn
     * @see TextRule
     * @see Canvas#drawText(CharSequence, int, int, float, float, Paint)
     */
    public AXAnimation drawText(final String key, boolean drawOnFront, boolean typing, Paint paint, int gravity,
                                float x, float y, LiveVar<CharSequence> text) {
        createRule(new TextRule(paint, key, drawOnFront, typing, gravity, getValue(true, false, x), getValue(true, false, y), text));
        return this;
    }

    /**
     * Adds a {@link DrawRule} to draw a text. ({@link LiveSize}, {@link LiveVar})
     *
     * @param paint       the paint that should use to draw
     * @param key         a specific key for the rule
     * @param drawOnFront True if the rule should be drawn before calling
     *                    the super.dispatchDraw(Canvas).
     * @param typing      True if should type it during animation, False otherwise.
     * @param gravity     Gravity of the origin corner.
     * @param x           The x-coordinate of origin for where to draw the text
     * @param y           The y-coordinate of origin for where to draw the text
     * @param text        The text to be drawn
     * @see TextRule
     * @see Canvas#drawText(CharSequence, int, int, float, float, Paint)
     */
    public AXAnimation drawText(final String key, boolean drawOnFront, boolean typing, Paint paint, int gravity,
                                LiveSize x, LiveSize y, LiveVar<CharSequence> text) {
        createRule(new TextRule(paint, key, drawOnFront, typing, gravity, getValue(true, x), getValue(true, y), text));
        return this;
    }


    /**
     * Removes a {@link DrawRule} without animation.
     * You can use {@link ReverseRule} to reverse a {@link DrawRule}'s animation
     * and remove it when it's gone.
     *
     * @param key The rule's specific key
     */
    public AXAnimation removeDrawRule(final String key) {
        createRule(new RemoveDrawRule(key));
        return this;
    }

    // *************** AXAnimator methods ***************

    /**
     * Start AXAnimation
     * Will start right after it gets the view's LayoutSize from it's AnimatedLayout parent.
     *
     * @param view target view
     */
    public void start(@NonNull View view) {
        start(view, null, false, false);
    }

    /**
     * Plays the AXAnimator in reverse.
     * it will start from the end and play backwards.
     *
     * @param view target view
     * @see AXAnimation#start(View)
     */
    public void reverse(@NonNull View view) {
        start(view, null, true, false);
    }

    public void end(@NonNull View view) {
        start(view, null, false, true);
    }

    public void start(@NonNull View view, @Nullable LayoutSize originalLayout, boolean reverseMode, boolean endMode) {
        doneRule();
        animator.targetView = view;
        this.reverseMode = reverseMode;
        this.endMode = endMode;

        boolean hasLayoutRule = AXAnimator.hasLayoutRule(this);

        if (view.getParent() == null && hasLayoutRule)
            throw new NullPointerException("View's parent can not be null!");

        if (view.getParent() != null && !(view.getParent() instanceof AnimatedLayout)) {
            if (hasLayoutRule)
                throw new ClassCastException("View's parent must be an AnimatedLayout to work with Layout rules!");
        }

        LayoutSize parentSize = null;

        if (view.getParent() != null && view.getParent() instanceof AnimatedLayout) {
            AnimatedLayout layout = (AnimatedLayout) view.getParent();

            if (originalLayout == null) {
                if (originalLayoutParams == null) {
                    if (view.getLayoutParams() instanceof AnimatedLayoutParams) {
                        start(view, new LayoutSize((AnimatedLayoutParams) view.getLayoutParams()), reverseMode, endMode);
                    } else {
                        layout.getLayoutSize(view, sizeReadyListener);
                    }
                } else {
                    layout.getLayoutSize(view, originalLayoutParams, sizeReadyListener);
                }
                return;
            } else {
                if (layout instanceof InspectLayout) {
                    ((InspectLayout) layout).getReadyForInspect(AXAnimator.hasInspect(this));
                }

                parentSize = layout.getLayoutSize();
            }
        }

        if (preRules.size() > 0) {
            Pair<View, LayoutSize> pair = Pair.create(view, originalLayout);
            for (PreRule preRule : preRules)
                pair = preRule.apply(this, pair);

            animator.start(pair.first, parentSize, pair.second, this, reverseMode, endMode);
        } else {
            animator.start(view, parentSize, originalLayout, this, reverseMode, endMode);
        }
    }

    public ViewGroup.LayoutParams getOriginalLayoutParams() {
        if (originalLayoutParams != null) {
            return originalLayoutParams;
        } else {
            if (getTargetView() != null)
                return getTargetView().getLayoutParams();
        }
        return null;
    }

    public void pause() {
        animator.pause();
    }

    public void resume() {
        animator.resume();
    }

    public void cancel() {
        animator.cancel();
    }

    public void end() {
        animator.end();
    }

    public boolean isPaused() {
        return animator.isPaused();
    }

    public boolean isRunning() {
        return animator.isRunning();
    }

    /**
     * Gets the length of the animation. (+ delay)
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getTotalDuration() {
        if (repeatCount == INFINITE)
            return INFINITE;

        return animator.getTotalDuration(this) * (repeatCount + 1);
    }

    /**
     * Gets the length of the RuleSection. (+ delay)
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getRuleSectionTotalDuration(int index) {
        return animator.getTotalDuration(getRuleSection(index));
    }

    /**
     * Gets the length of the RuleSection. (+ delay)
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getRuleSectionTotalDuration(RuleSection section) {
        return animator.getTotalDuration(section);
    }

    /**
     * Gets the current position of the animation in time, which is equal to the current
     * time minus the time that the animation started. An animation that is not yet started will
     * return a value of zero.
     *
     * @return The current position in time of the animation.
     */
    public long getCurrentPlayTime() {
        return animator.getCurrentPlayTime();
    }

    /**
     * Sets the position of the animation to the specified point in time. This time should
     * be between 0 and the total duration of the animation, including any repetition. If
     * the animation has not yet been started, then it will not advance forward after it is
     * set to this time; it will simply set the time to this value and perform any appropriate
     * actions based on that time. If the animation is already running, then setCurrentPlayTime()
     * will set the current playing time to this value and continue playing from that point.
     * <p>
     * Note: Some rules don't support currentPlayTime!
     *
     * @param playTime The time, in milliseconds, to which the animation is advanced or rewound.
     */
    public void setCurrentPlayTime(long playTime) {
        animator.animation = this;
        animator.setCurrentPlayTime(playTime);
    }

    /**
     * Returns the current animation fraction, which is the elapsed/interpolated fraction used in
     * the most recent frame update on the animation.
     *
     * @return Elapsed/interpolated fraction of the animation.
     */
    public float getAnimatedFraction() {
        return Math.max(Math.min(1f, animator.getAnimatedFraction(this)), 0);
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the life of an
     * animation, such as start, pause, and end.
     *
     * @param listener the listener to be added to the current set of listeners for this animation.
     * @see AXAnimatorListener
     * @see AXAnimatorListenerAdapter
     */
    public AXAnimation addAnimatorListener(@NonNull AXAnimatorListener listener) {
        animator.listeners.add(listener);
        return this;
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public AXAnimation removeAnimatorListener(@NonNull AXAnimatorListener listener) {
        animator.listeners.remove(listener);
        return this;
    }

    /**
     * Gets the set of {@link AXAnimatorListener} objects that are currently
     * listening for events on this <code>Animator</code> object.
     *
     * @return ArrayList<AXAnimatorListener> The set of listeners.
     */
    public ArrayList<AXAnimatorListener> getAnimatorListeners() {
        return animator.listeners;
    }

    /**
     * Removes all listeners from the set listening to this animation.
     */
    public AXAnimation clearAnimatorListeners() {
        animator.listeners.clear();
        return this;
    }

    /**
     * You can get targetView later on {@link AXAnimatorListener}
     *
     * @return target view
     */
    public View getTargetView() {
        return animator.targetView;
    }

    /**
     * You can get targetSize later on {@link AXAnimatorListener}
     *
     * @return target size
     */
    public LayoutSize getTargetSize() {
        return animator.targetSize;
    }

    /**
     * You can get parentSize later on {@link AXAnimatorListener}
     *
     * @return parent size
     */
    public LayoutSize getParentSize() {
        return animator.layoutSizes[0];
    }

    /**
     * You can get originalSize later on {@link AXAnimatorListener}
     *
     * @return original size
     */
    public LayoutSize getOriginalSize() {
        return animator.layoutSizes[1];
    }

    /**
     * @return True if the animation has some rules which depends on layout.
     */
    public boolean hasLayoutRule() {
        return AXAnimator.hasLayoutRule(this);
    }

    /**
     * @return index of running animation section.
     */
    public int getCurrentSectionIndex() {
        return animator.indexes[0];
    }

    /**
     * @return index of running animation rule on the section.
     */
    public int getCurrentRuleIndex() {
        return animator.indexes[1];
    }

    /**
     * Sets a listener for listening to the start of section.
     *
     * @see RuleSection#onStart(AXAnimation)
     */
    public AXAnimation withSectionStartAction(@Nullable final AXAnimatorStartListener startListener) {
        sectionStartListener = startListener;
        return this;
    }

    /**
     * Sets a listener for listening to the end of section.
     *
     * @see RuleSection#onEnd(AXAnimation)
     */
    public AXAnimation withSectionEndAction(@Nullable final AXAnimatorEndListener endListener) {
        sectionEndListener = endListener;
        return this;
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the start life of an
     * animation
     * Removes other start actions, If you want to add a new one without removing others you can use
     * {@link #addStartAction(AXAnimatorStartListener)}
     *
     * @param startListener the listener to be added to the current set of listeners for this animation.
     *                      pass null to only remove other start actions.
     * @see #addAnimatorListener(AXAnimatorListener)
     */
    public AXAnimation withStartAction(@Nullable final AXAnimatorStartListener startListener) {
        removeAllStartListeners();
        if (startListener == null)
            return this;

        return addAnimatorListener(new ListenerAdapter<AXAnimatorStartListener>(startListener) {
            @Override
            public void onAnimationStart(AXAnimation animation) {
                startListener.onAnimationStart(animation);
            }
        });
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the end life of an
     * animation.
     * Removes other end actions, If you want to add a new one without removing others you can use
     * {@link #addEndAction(AXAnimatorEndListener)}
     *
     * @param endListener the listener to be added to the current set of listeners for this animation.
     *                    pass null to only remove other end actions.
     * @see #addAnimatorListener(AXAnimatorListener)
     */
    public AXAnimation withEndAction(@Nullable final AXAnimatorEndListener endListener) {
        removeAllEndListeners();
        if (endListener == null)
            return this;

        return addAnimatorListener(new ListenerAdapter<AXAnimatorEndListener>(endListener) {
            @Override
            public void onAnimationEnd(AXAnimation animation) {
                endListener.onAnimationEnd(animation);
            }
        });
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the start life of an
     * animation
     *
     * @param startListener the listener to be added to the current set of listeners for this animation.
     * @see #addAnimatorListener(AXAnimatorListener)
     */
    public AXAnimation addStartAction(@NonNull final AXAnimatorStartListener startListener) {
        removeListener(startListener);
        return addAnimatorListener(new ListenerAdapter<AXAnimatorStartListener>(startListener) {
            @Override
            public void onAnimationStart(AXAnimation animation) {
                otherListener.onAnimationStart(animation);
            }
        });
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the end life of an
     * animation
     *
     * @param endListener the listener to be added to the current set of listeners for this animation.
     * @see #addAnimatorListener(AXAnimatorListener)
     */
    public AXAnimation addEndAction(@NonNull final AXAnimatorEndListener endListener) {
        removeListener(endListener);
        return addAnimatorListener(new ListenerAdapter<AXAnimatorEndListener>(endListener) {
            @Override
            public void onAnimationEnd(AXAnimation animation) {
                otherListener.onAnimationEnd(animation);
            }
        });
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public AXAnimation removeStartAction(@NonNull AXAnimatorStartListener listener) {
        removeListener(listener);
        return this;
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public AXAnimation removeEndAction(@NonNull AXAnimatorEndListener listener) {
        removeListener(listener);
        return this;
    }

    private void removeListener(Object listener) {
        AXAnimatorListener toRemove = null;
        for (AXAnimatorListener l : getAnimatorListeners()) {
            if (l instanceof ListenerAdapter) {
                if (((ListenerAdapter<?>) l).otherListener == listener) {
                    toRemove = l;
                    break;
                }
            }
        }
        if (toRemove != null)
            removeAnimatorListener(toRemove);
    }

    private void removeAllStartListeners() {
        ArrayList<AXAnimatorListener> toRemove = new ArrayList<>();
        for (AXAnimatorListener l : getAnimatorListeners()) {
            if (l instanceof ListenerAdapter) {
                if (((ListenerAdapter<?>) l).otherListener instanceof AXAnimatorStartListener) {
                    toRemove.add(l);
                }
            }
        }
        for (AXAnimatorListener lr : toRemove)
            removeAnimatorListener(lr);
    }

    private void removeAllEndListeners() {
        ArrayList<AXAnimatorListener> toRemove = new ArrayList<>();
        for (AXAnimatorListener l : getAnimatorListeners()) {
            if (l instanceof ListenerAdapter) {
                if (((ListenerAdapter<?>) l).otherListener instanceof AXAnimatorEndListener) {
                    toRemove.add(l);
                }
            }
        }
        for (AXAnimatorListener lr : toRemove)
            removeAnimatorListener(lr);
    }

    private static class ListenerAdapter<L> extends AXAnimatorListenerAdapter {
        protected L otherListener;

        public ListenerAdapter(L otherListener) {
            this.otherListener = otherListener;
        }
    }

    // *************** Reset & Import ***************

    /**
     * Sets the duration, delay, interpolator and other animator properties to default values.
     */
    public AXAnimation resetAnimatorValues() {
        data.reset();
        return this;
    }

    /**
     * Remove all data of this animation
     */
    public void resetAnimation() {
        resetAnimatorValues();
        rules.clear();
        tmpRules.clear();
        originalLayoutParams = null;
        targetLayoutParams = null;
        applyNewAnimatorForReverseRules = false;
        shouldReverseRulesKeepOldData = true;
        lockX().lockY();
    }

    /**
     * Import another animation
     */
    public AXAnimation importAnimation(AXAnimation animation) {
        return importAnimation(animation, false);
    }

    /**
     * Import another animation
     */
    public AXAnimation importAnimation(AXAnimation animation, boolean clone) {
        if (clone) {
            for (RuleSection section : animation.rules)
                rules.add((RuleSection) section.clone());
            for (Rule<?> rule : animation.tmpRules)
                tmpRules.add((Rule<?>) rule.clone());
        } else {
            rules.addAll(animation.rules);
            tmpRules.addAll(animation.tmpRules);
        }
        liveVarUpdaters.addAll(animation.liveVarUpdaters);
        preRules.addAll(animation.preRules);
        repeatCount = animation.repeatCount;
        repeatMode = animation.repeatMode;
        applyNewAnimatorForReverseRules = animation.applyNewAnimatorForReverseRules;
        shouldReverseRulesKeepOldData = animation.shouldReverseRulesKeepOldData;
        widthLocked = animation.widthLocked;
        heightLocked = animation.heightLocked;
        targetLayoutParams = animation.targetLayoutParams;
        originalLayoutParams = animation.originalLayoutParams;
        measureUnitEnabled = animation.measureUnitEnabled;
        density = animation.density;
        nextRuleRequiresApi = animation.nextRuleRequiresApi;
        data.importAnimatorData(animation.data);
        animator.listeners.addAll(animation.animator.listeners);
        if (animation.wrapper != null)
            wrapper = animation.wrapper;
        if (animation.wrapperSection != null) {
            wrapperSection = animation.wrapperSection;
            wrapDelays = animation.wrapDelays;
        }
        return this;
    }

    /**
     * Remove all data of this animation and import another one
     */
    public AXAnimation setAnimation(AXAnimation animation) {
        resetAnimation();
        importAnimation(animation);
        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NonNull
    @Override
    public AXAnimation clone() {
        return create().importAnimation(this, true);
    }

    // *************** AXSimpleAnimator ***************

    /**
     * Creates a {@link AXSimpleAnimator} for the opened section's rules.
     * If the section had more than 1 rules, will return a {@link AXSimpleAnimatorSet}!
     *
     * @param target the target view of animator.
     * @return AXSimpleAnimator for the opened section's rules
     */
    public AXSimpleAnimator createSimpleAnimator(View target) {
        if (tmpRules.size() == 0) {
            throw new RuntimeException("There is no rule in this section!");
        }

        if (tmpRules.size() == 1) {
            return AXSimpleAnimator.create(target, originalLayoutParams, tmpRules.get(0));
        } else {
            return new AXSimpleAnimatorSet(tmpRules, target, originalLayoutParams);
        }
    }

    // *************** AXAnimationSaver ***************

    /**
     * Save animation,
     * Later you can load this animation by {@link AXAnimation#getAnimation(String)}.
     * Also this one can be useful to load animations on {@link AXAnimationSet}
     *
     * @param name animation's name
     */
    public void save(String name) {
        AXAnimationSaver.save(this, name);
    }

    /**
     * @param name animation's name
     * @return saved animation.
     */
    public static AXAnimation getAnimation(String name) {
        return AXAnimationSaver.get(name);
    }

    /**
     * @return all saved animation.
     */
    public static Map<String, AXAnimation> getAllSavedAnimations() {
        return AXAnimationSaver.animations;
    }

    /**
     * @return all running & paused animations of the view
     */
    public static List<AXAnimation> getAnimationsOfView(View view) {
        return AXAnimationSaver.get(view);
    }

    /**
     * Cancels all running & paused animations of the view
     */
    public static void clear(View view) {
        List<AXAnimation> list = getAnimationsOfView(view);
        if (list != null) {
            for (AXAnimation a : list)
                a.cancel();
        }
        AXAnimationSaver.clear(view);
    }

    // *************** Measure units ***************

    /**
     * Sets a custom measure density
     * <p>
     * All values of (int, float, Point, Rect, LayoutSize)
     * will be updated with this density
     *
     * @see AXAnimation#dp()
     * @see AXAnimation#px()
     */
    public AXAnimation measureUnit(float density) {
        measureUnitEnabled = true;
        this.density = density;
        if (density == 1f)
            return px();
        return this;
    }

    /**
     * Sets a custom measure density based on
     * the physical density of the screen
     *
     * @see AXAnimation#measureUnit(float)
     * @see AXAnimation#px()
     */
    public AXAnimation dp() {
        return measureUnit(Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Sets a custom measure density based on
     * the physical density of the screen
     *
     * @see AXAnimation#measureUnit(float)
     * @see AXAnimation#px()
     */
    public AXAnimation dp(Context context) {
        if (context != null)
            return measureUnit(context.getResources().getDisplayMetrics().density);
        else
            return dp();
    }

    /**
     * Disable custom measure density
     * px based on actual pixels on the screen.
     *
     * @see AXAnimation#measureUnit(float)
     * @see AXAnimation#dp()
     */
    public AXAnimation px() {
        measureUnitEnabled = false;
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    private float dp(float value) {
        return Resources.getSystem().getDisplayMetrics().density * value;
    }

    private float measure(final float v) {
        return measure(true, false, v);
    }

    private float measure(final boolean c, final boolean supportLP, final float v) {
        if (c & SizeUtils.isCustomSize((int) v))
            return v;

        if (supportLP && (v == MATCH_PARENT || v == WRAP_CONTENT))
            return v;

        return (int) (v * density);
    }

    private int measure(final int v) {
        return measure(false, v);
    }

    private int measure(final boolean supportLP, final int v) {
        return measure(true, supportLP, v);
    }

    private int measure(final boolean c, final boolean supportLP, final int v) {
        if (c & SizeUtils.isCustomSize(v))
            return v;

        if (supportLP && (v == MATCH_PARENT || v == WRAP_CONTENT))
            return v;

        return (int) (v * density);
    }

    private float[] getValues(float... values) {
        if (!measureUnitEnabled)
            return values;

        float[] pxValues = new float[values.length];
        for (int i = 0; i < values.length; ++i) {
            pxValues[i] = measure(values[i]);
        }
        return pxValues;
    }

    private Float[] getValues(Float... values) {
        if (!measureUnitEnabled)
            return values;

        Float[] pxValues = new Float[values.length];
        for (int i = 0; i < values.length; ++i) {
            pxValues[i] = measure(values[i]);
        }
        return pxValues;
    }

    private int[] getValues(int... values) {
        return getValues(false, values);
    }

    private int[] getValues(boolean supportLP, int... values) {
        if (!measureUnitEnabled)
            return values;

        int[] pxValues = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            pxValues[i] = measure(supportLP, values[i]);
        }
        return pxValues;
    }

    private LiveSize[] getValues(LiveSize... values) {
        if (!measureUnitEnabled)
            return values;

        for (LiveSize s : values)
            s.measure(true, density);

        return values;
    }

    private Point[] getValues(Point... values) {
        if (!measureUnitEnabled)
            return values;

        Point[] pxValues = new Point[values.length];
        for (int i = 0; i < values.length; ++i) {
            pxValues[i] = new Point(measure(false, false, values[i].x), measure(false, false, values[i].y));
        }
        return pxValues;
    }

    private Rect[] getValues(Rect... values) {
        if (!measureUnitEnabled)
            return values;

        for (Rect r : values) {
            r.left = measure(true, false, r.left);
            r.top = measure(true, false, r.top);
            r.right = measure(true, true, r.right);
            r.bottom = measure(true, true, r.bottom);
        }
        return values;
    }

    private RectF[] getValues(RectF... values) {
        if (!measureUnitEnabled)
            return values;

        for (RectF r : values) {
            r.left = measure(true, false, r.left);
            r.top = measure(true, false, r.top);
            r.right = measure(true, true, r.right);
            r.bottom = measure(true, true, r.bottom);
        }
        return values;
    }

    private RectF getValue(RectF r) {
        if (!measureUnitEnabled)
            return r;

        r.left = measure(true, false, r.left);
        r.top = measure(true, false, r.top);
        r.right = measure(true, true, r.right);
        r.bottom = measure(true, true, r.bottom);
        return r;
    }

    private LayoutSize[] getValues(LayoutSize... values) {
        if (!measureUnitEnabled)
            return values;

        for (LayoutSize r : values) {
            getValues(r);
        }
        return values;
    }

    private LayoutSize getValue(LayoutSize r) {
        if (!measureUnitEnabled)
            return r;

        r.left = measure(true, false, r.left);
        r.top = measure(true, false, r.top);
        r.right = measure(true, r.right);
        r.bottom = measure(true, r.bottom);

        if (r.liveLeft != null) getValue(false, r.liveLeft);
        if (r.liveRight != null) getValue(true, r.liveRight);
        if (r.liveTop != null) getValue(false, r.liveTop);
        if (r.liveBottom != null) getValue(true, r.liveBottom);
        return r;
    }

    private LiveSize getValue(boolean supportsLP, LiveSize value) {
        if (!measureUnitEnabled)
            return value;
        value.measure(supportsLP, density);

        return value;
    }

    private int getValue(int value) {
        return getValue(false, value);
    }

    private int getValue(boolean supportLP, int value) {
        return getValue(true, supportLP, value);
    }

    private int getValue(boolean c, boolean supportLP, int value) {
        if (!measureUnitEnabled)
            return value;

        return measure(c, supportLP, value);
    }

    private float getValue(float value) {
        return getValue(false, value);
    }

    private float getValue(boolean supportLP, float value) {
        return getValue(true, supportLP, value);
    }

    private float getValue(boolean c, boolean supportLP, float value) {
        if (!measureUnitEnabled)
            return value;

        return measure(c, supportLP, value);
    }

    private Point getValue(Point value) {
        if (!measureUnitEnabled)
            return value;

        value.x = measure(false, false, value.x);
        value.y = measure(false, false, value.y);
        return value;
    }

    private PointF[][] getValue(PointF[][] value) {
        if (!measureUnitEnabled)
            return value;

        for (PointF[] p : value) {
            for (PointF p2 : p) {
                getValue(p2);
            }
        }
        return value;
    }

    private LiveSizePoint[][] getValues(LiveSizePoint[]... values) {
        if (!measureUnitEnabled)
            return values;

        for (LiveSizePoint[] liveSizePoints : values) {
            for (LiveSizePoint p : liveSizePoints) {
                getValue(true, p.x);
                getValue(true, p.y);
            }
        }
        return values;
    }

    private PointF getValue(PointF value) {
        if (!measureUnitEnabled)
            return value;

        value.x = measure(true, true, value.x);
        value.y = measure(true, true, value.y);
        return value;
    }

}
