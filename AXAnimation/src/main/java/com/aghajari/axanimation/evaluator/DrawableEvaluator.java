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
package com.aghajari.axanimation.evaluator;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.TypeEvaluator;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.utils.GradientDrawableWrapper;


/**
 * This evaluator can be used to perform type interpolation between <code>Drawable</code> values.
 *
 * @author AmirHossein Aghajari
 */
public class DrawableEvaluator implements TypeEvaluator<Drawable> {

    // Other evaluators
    private final ArgbEvaluator argbEvaluator = ArgbEvaluator.getInstance();
    private final FloatEvaluator floatEvaluator = new FloatEvaluator();
    private final IntEvaluator intEvaluator = new IntEvaluator();
    private final FloatArrayEvaluator floatArrayEvaluator = new FloatArrayEvaluator();
    private final ColorStateListEvaluator colorStateListEvaluator = new ColorStateListEvaluator();
    private final ColorStateListEvaluator strokeColorStateListEvaluator = new ColorStateListEvaluator();
    private final FadeDrawableEvaluator fadeDrawableEvaluator = new FadeDrawableEvaluator();
    private ShaderEvaluator shaderEvaluator = null;

    // Start & End & tmp drawables
    private Drawable[] drawables;

    /**
     * @see GradientDrawableWrapper#ensureValidRect(GradientDrawable, Canvas)
     */
    private final Canvas tmpCanvas = new Canvas();

    // Saved values to animate
    private Object[] savedValues;
    private static final int START_COLOR_CD = 0;
    private static final int END_COLOR_CD = 1;
    private static final int START_COLORS_GD = 2;
    private static final int END_COLORS_GD = 3;
    private static final int START_COLOR_STATE_LIST_GD = 4;
    private static final int END_COLOR_STATE_LIST_GD = 5;
    private static final int HAS_RADII_GD = 6;
    private static final int START_RADII_GD = 7;
    private static final int END_RADII_GD = 8;
    private static final int HAS_RADIUS_GD = 9;
    private static final int START_RADIUS_GD = 10;
    private static final int END_RADIUS_GD = 11;
    private static final int HAS_GRADIENT_RADIUS_GD = 12;
    private static final int START_GRADIENT_RADIUS_GD = 13;
    private static final int END_GRADIENT_RADIUS_GD = 14;
    private static final int HAS_GRADIENT_CENTER_GD = 15;
    private static final int START_GRADIENT_CENTER_X_GD = 16;
    private static final int END_GRADIENT_CENTER_X_GD = 17;
    private static final int START_GRADIENT_CENTER_Y_GD = 18;
    private static final int END_GRADIENT_CENTER_Y_GD = 19;
    private static final int HAS_STROKE = 20;
    private static final int START_STROKE_COLOR = 21;
    private static final int END_STROKE_COLOR = 22;
    private static final int START_STROKE_WIDTH = 23;
    private static final int END_STROKE_WIDTH = 24;
    private static final int START_STROKE_DASH_WIDTH = 25;
    private static final int END_STROKE_DASH_WIDTH = 26;
    private static final int START_STROKE_DASH_GAP = 27;
    private static final int END_STROKE_DASH_GAP = 28;
    private static final int HAS_ALPHA = 29;
    private static final int START_ALPHA = 30;
    private static final int END_ALPHA = 31;
    private static final int VALUES_LENGTH = 32;

    // Animations id
    // Useful for subclasses to stop animating a property.
    private static final int ID_COLOR_CD = 0;
    private static final int ID_COLORS_GD = 1;
    private static final int ID_COLOR_STATE_LIST_GD = 2;
    private static final int ID_RADII_GD = 3;
    private static final int ID_RADIUS_GD = 4;
    private static final int ID_GRADIENT_RADIUS_GD = 5;
    private static final int ID_GRADIENT_CENTER_GD = 6;
    private static final int ID_GRADIENT_CENTER_X_GD = 7;
    private static final int ID_GRADIENT_CENTER_Y_GD = 8;
    private static final int ID_STROKE_GD = 9;
    private static final int ID_STROKE_COLOR_GD = 10;
    private static final int ID_STROKE_WIDTH_GD = 11;
    private static final int ID_STROKE_DASH_WIDTH_GD = 12;
    private static final int ID_STROKE_DASH_GAP_GD = 13;
    private static final int ID_ALPHA_GD = 14;
    private static final int ID_SHADER_GD = 15;
    private static final int ID_FADE = 16;

    private final Rect bounds = new Rect();

    public DrawableEvaluator() {
    }

    @NonNull
    @Override
    public Drawable evaluate(float fraction, @NonNull Drawable startValue, @NonNull Drawable endValue) {
        /*if (fraction >= 1)
            return endValue;
        if (fraction == 0)
            return startValue;*/

        boolean shouldCopy;
        if (drawables == null || savedValues == null) {
            drawables = new Drawable[3];
            shouldCopy = true;
        } else {
            shouldCopy = drawables[0] != startValue || drawables[1] != endValue;
        }

        Drawable newDrawable;
        if (shouldCopy) {
            savedValues = new Object[VALUES_LENGTH];
            drawables[0] = startValue;
            drawables[1] = endValue;

            // Clone the drawable which has more properties to animate.
            // If i don't do that i can't animate GradientDrawable to ColorDrawable
            Drawable toClone = endValue;
            if (!(toClone instanceof GradientDrawable) && startValue instanceof GradientDrawable)
                toClone = startValue;

            newDrawable = toClone.getConstantState().newDrawable().mutate();

            if (newDrawable.getBounds().isEmpty()) {
                if (bounds.isEmpty()) {
                    if (!startValue.getBounds().isEmpty())
                        bounds.set(startValue.getBounds());
                    else
                        bounds.set(endValue.getBounds());
                }
                newDrawable.setBounds(bounds);
            }

            newDrawable.draw(tmpCanvas);

            drawables[2] = newDrawable;
        } else {
            newDrawable = drawables[2];
        }


        // Save start & end values only once
        // So we don't need calculate them every time.
        // I'm using reflection so i should stay away from it
        // as long as we can to make it faster!
        if (shouldCopy) {
            shaderEvaluator = null;

            if (!evaluatorSupports(startValue, endValue)) {
                // Aghajari 's DrawableEvaluator only can animate ColorDrawable & GradientDrawable
                // Can we animate other types?
                // Deals with other types like FadeDrawableEvaluator
                fadeDrawableEvaluator.init(startValue, endValue);

            } else if (newDrawable instanceof ColorDrawable) {
                // ColorDrawable only needs to animate it's color and nothing else!

                ColorDrawable cd = (ColorDrawable) newDrawable;
                savedValues[START_COLOR_CD] = getColor(startValue, cd.getColor());
                savedValues[END_COLOR_CD] = getColor(endValue, cd.getColor());
                savedValues[HAS_ALPHA] = false;

            } else if (newDrawable instanceof GradientDrawable) {
                // The most difficult type to deal with is here :D
                // let's start with Alpha
                int startAlpha = startValue.getAlpha();
                int endAlpha = endValue.getAlpha();
                savedValues[HAS_ALPHA] = startAlpha != endAlpha;
                savedValues[START_ALPHA] = startAlpha;
                savedValues[END_ALPHA] = endAlpha;

                GradientDrawable gd = (GradientDrawable) newDrawable;
                ColorStateList startCSL = getColorStateList(startValue);
                ColorStateList endCSL = getColorStateList(endValue);

                // I can't animate the color by ColorStateListEvaluator
                // one (or both) of drawables don't have ColorStateList
                if (startCSL == null || endCSL == null) {
                    savedValues[START_COLOR_STATE_LIST_GD] = (ColorStateList) null;
                    savedValues[END_COLOR_STATE_LIST_GD] = (ColorStateList) null;

                    // If both of drawables have multi colors
                    // And have different gradient types
                    // Or different orientations (for linear)
                    // Or different gradient radius (for radial)
                    // Or one (or both) of them are using (UseLevel) (for sweep),
                    // I should animate the Shader of mFillPaint
                    // (The only way to do that is using reflection)
                    // I've made another Evaluator for Shader (ShaderEvaluator)
                    if (startValue instanceof GradientDrawable && endValue instanceof GradientDrawable) {
                        int[] realStartColors = GradientDrawableWrapper.getColors((GradientDrawable) startValue);
                        int[] realEndColors = GradientDrawableWrapper.getColors((GradientDrawable) endValue);
                        if (realStartColors != null && realEndColors != null) {
                            if (shouldUseShaderEvaluator((GradientDrawable) startValue, (GradientDrawable) endValue, tmpCanvas))
                                shaderEvaluator = new ShaderEvaluator((GradientDrawable) startValue,
                                        (GradientDrawable) endValue,
                                        realStartColors, realEndColors);
                        }
                    }

                    // So I couldn't animate the color by ColorStateListEvaluator
                    // Let's animate all possible colors
                    int color = getColor(gd, Color.TRANSPARENT);
                    int[] startColors = getColors(startValue, color);
                    int[] endColors = getColors(endValue, color);

                    // length of startColors & endColors must be same
                    int length = Math.max(startColors.length, endColors.length);

                    // length 1 means we only have main color, let's work with it.
                    if (length == 1) {
                        if (startColors.length != 1)
                            startColors = new int[]{color};
                        if (endColors.length != 1)
                            endColors = new int[]{color};

                    } else {

                        if (startColors.length != length) {
                            int[] tmpColors = new int[length];
                            int lastColor = color;
                            for (int i = 0; i < tmpColors.length; i++) {
                                if (startColors.length > i) {
                                    lastColor = startColors[i];
                                    tmpColors[i] = startColors[i];
                                } else {
                                    tmpColors[i] = lastColor;
                                }
                            }
                            startColors = tmpColors;
                        }

                        if (endColors.length != length) {
                            int[] tmpColors = new int[length];
                            int lastColor = color;
                            for (int i = 0; i < tmpColors.length; i++) {
                                if (endColors.length > i) {
                                    lastColor = endColors[i];
                                    tmpColors[i] = endColors[i];
                                } else {
                                    tmpColors[i] = lastColor;
                                }
                            }
                            endColors = tmpColors;
                        }

                    }

                    savedValues[START_COLORS_GD] = startColors;
                    savedValues[END_COLORS_GD] = endColors;
                } else {
                    // I'm going to animate this one by ColorStateListEvaluator
                    savedValues[START_COLOR_STATE_LIST_GD] = startCSL;
                    savedValues[END_COLOR_STATE_LIST_GD] = endCSL;
                    colorStateListEvaluator.csl = null;
                }

                // Next step, cornerRadii
                // I only need to animate cornerRadii or cornerRadius
                // I won't animate both together
                // If only one of the drawables had cornerRadii,
                // I'll convert other one's cornerRadius to cornerRadii.
                Object startRadii = getCornerRadii(startValue);
                Object endRadii = getCornerRadii(endValue);
                Object startRadius = getCornerRadius(startValue);
                Object endRadius = getCornerRadius(endValue);

                boolean radiusDone = false;

                if (startRadii != null || endRadii != null) {
                    float[] sRadii;
                    float[] eRadii;
                    float tmpStartRadii = startRadius == null ? 0 : (float) startRadius;
                    float tmpEndRadii = endRadius == null ? 0 : (float) endRadius;

                    if (startRadii != null)
                        sRadii = (float[]) startRadii;
                    else
                        sRadii = new float[0];

                    if (endRadii != null)
                        eRadii = (float[]) endRadii;
                    else
                        eRadii = new float[0];

                    int lengthRadii = Math.max(sRadii.length, eRadii.length);

                    if (lengthRadii > 0) {
                        if (sRadii.length != lengthRadii) {
                            float[] tmpRadii = new float[lengthRadii];
                            for (int i = 0; i < tmpRadii.length; i++) {
                                if (sRadii.length > i)
                                    tmpRadii[i] = sRadii[i];
                                else
                                    tmpRadii[i] = tmpStartRadii;

                            }
                            sRadii = tmpRadii;
                        }

                        if (eRadii.length != lengthRadii) {
                            float[] tmpRadii = new float[lengthRadii];
                            for (int i = 0; i < tmpRadii.length; i++) {
                                if (eRadii.length > i)
                                    tmpRadii[i] = eRadii[i];
                                else
                                    tmpRadii[i] = tmpEndRadii;

                            }
                            eRadii = tmpRadii;
                        }

                        radiusDone = true;
                        savedValues[START_RADII_GD] = sRadii;
                        savedValues[END_RADII_GD] = eRadii;
                    }
                }

                savedValues[HAS_RADII_GD] = radiusDone;
                savedValues[HAS_RADIUS_GD] = false;

                // Couldn't animate cornerRadii
                // Let's check cornerRadius
                if (!radiusDone) {

                    if (startRadius != null || endRadius != null) {
                        float sRadius = 0;
                        float eRadius = 0;

                        if (startRadius != null)
                            sRadius = (float) startRadius;

                        if (endRadius != null)
                            eRadius = (float) endRadius;

                        if (sRadius != eRadius) {
                            savedValues[HAS_RADIUS_GD] = true;
                            savedValues[START_RADIUS_GD] = sRadius;
                            savedValues[END_RADIUS_GD] = eRadius;
                        }
                    }
                }

                // Next step, GradientRadius
                // Need it for Radial Gradient
                Object startGradientRadius = getGradientRadius(startValue);
                Object endGradientRadius = getGradientRadius(endValue);
                savedValues[HAS_GRADIENT_RADIUS_GD] = false;

                if (startGradientRadius != null || endGradientRadius != null) {
                    float sGRadius = 0;
                    float eGRadius = 0;

                    if (startGradientRadius != null)
                        sGRadius = (float) startGradientRadius;

                    if (endGradientRadius != null)
                        eGRadius = (float) endGradientRadius;

                    if (sGRadius != eGRadius) {
                        savedValues[HAS_GRADIENT_RADIUS_GD] = true;
                        savedValues[START_GRADIENT_RADIUS_GD] = sGRadius;
                        savedValues[END_GRADIENT_RADIUS_GD] = eGRadius;
                    }
                }

                // If startCenterX equals endCenterX
                // and startCenterY equals endCenterY,
                // I don't need to update it at all.
                // Note: there is only one method to set GradientCenter
                //  and needs both X, Y together.
                boolean shouldChangeCenter = true;

                Object startGradientCenterX = getGradientCenterX(startValue);
                Object endGradientCenterX = getGradientCenterX(endValue);
                boolean sameX;
                if (startGradientCenterX != null || endGradientCenterX != null) {
                    float sGradientCenterX = 0.5f;
                    float eGradientCenterX = 0.5f;

                    if (startGradientCenterX != null)
                        sGradientCenterX = (float) startGradientCenterX;

                    if (endGradientCenterX != null)
                        eGradientCenterX = (float) endGradientCenterX;

                    sameX = sGradientCenterX == eGradientCenterX;
                    savedValues[START_GRADIENT_CENTER_X_GD] = sGradientCenterX;
                    savedValues[END_GRADIENT_CENTER_X_GD] = eGradientCenterX;
                } else {
                    sameX = true;
                    shouldChangeCenter = false;
                }

                Object startGradientCenterY = getGradientCenterY(startValue);
                Object endGradientCenterY = getGradientCenterY(endValue);
                boolean sameY;
                if (startGradientCenterY != null || endGradientCenterY != null) {
                    float sGradientCenterY = 0.5f;
                    float eGradientCenterY = 0.5f;

                    if (startGradientCenterY != null)
                        sGradientCenterY = (float) startGradientCenterY;

                    if (endGradientCenterY != null)
                        eGradientCenterY = (float) endGradientCenterY;

                    sameY = sGradientCenterY == eGradientCenterY;
                    savedValues[START_GRADIENT_CENTER_Y_GD] = sGradientCenterY;
                    savedValues[END_GRADIENT_CENTER_Y_GD] = eGradientCenterY;
                } else {
                    shouldChangeCenter = false;
                    sameY = true;
                }

                if (sameX && sameY)
                    shouldChangeCenter = false;

                savedValues[HAS_GRADIENT_CENTER_GD] = shouldChangeCenter;

                // Next step, stroke properties
                // Note: there is only one method to set stroke
                //  and needs all stroke properties together.
                ColorStateList startStrokeColor = getStrokeColor(startValue);
                ColorStateList endStrokeColor = getStrokeColor(endValue);
                int startStrokeWidth = getStrokeWidth(startValue);
                int endStrokeWidth = getStrokeWidth(endValue);
                float startStrokeDashWidth = getStrokeDashWidth(startValue);
                float endStrokeDashWidth = getStrokeDashWidth(endValue);
                float startStrokeDashGap = getStrokeDashGap(startValue);
                float endStrokeDashGap = getStrokeDashGap(endValue);

                // I don't need to animate stroke if both of drawables have same properties.
                if (ColorStateListEvaluator.equals(startStrokeColor, endStrokeColor)
                        && startStrokeWidth == endStrokeWidth
                        && startStrokeDashWidth == endStrokeDashWidth
                        && startStrokeDashGap == endStrokeDashGap) {
                    savedValues[HAS_STROKE] = false;
                } else {
                    savedValues[HAS_STROKE] = true;
                    if (startStrokeColor == null)
                        startStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT);
                    if (endStrokeColor == null)
                        endStrokeColor = ColorStateList.valueOf(Color.TRANSPARENT);
                    strokeColorStateListEvaluator.csl = null;

                    savedValues[START_STROKE_COLOR] = startStrokeColor;
                    savedValues[END_STROKE_COLOR] = endStrokeColor;
                    savedValues[START_STROKE_WIDTH] = startStrokeWidth;
                    savedValues[END_STROKE_WIDTH] = endStrokeWidth;
                    savedValues[START_STROKE_DASH_WIDTH] = startStrokeDashWidth;
                    savedValues[END_STROKE_DASH_WIDTH] = endStrokeDashWidth;
                    savedValues[START_STROKE_DASH_GAP] = startStrokeDashGap;
                    savedValues[END_STROKE_DASH_GAP] = endStrokeDashGap;
                }
            }
        }

        // Animate saved values

        // As i said before,
        // Deals with other types like FadeDrawableEvaluator
        if (!evaluatorSupports(startValue, endValue)) {
            return evaluate(ID_FADE, fraction, startValue, endValue);

        } else if (newDrawable instanceof ColorDrawable) {
            // As i said before,
            // ColorDrawable only needs to animate it's color and nothing else!
            ColorDrawable cd = (ColorDrawable) newDrawable;

            if (canEvaluate(ID_COLOR_CD, fraction)) {
                int startColor = (int) savedValues[START_COLOR_CD];
                int endColor = (int) savedValues[END_COLOR_CD];
                if (startColor != endColor) {
                    cd.setColor(evaluateColor(ID_COLOR_CD, fraction, startColor, endColor));
                }
            }

        } else if (newDrawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) newDrawable;

            ColorStateList startCSL = (ColorStateList) savedValues[START_COLOR_STATE_LIST_GD];
            ColorStateList endCSL = (ColorStateList) savedValues[END_COLOR_STATE_LIST_GD];

            if (startCSL == null || endCSL == null) {
                // Animate all possible colors
                if (canEvaluate(ID_COLORS_GD, fraction)) {
                    int[] startColors = (int[]) savedValues[START_COLORS_GD];
                    int[] endColors = (int[]) savedValues[END_COLORS_GD];
                    int length = Math.max(startColors.length, endColors.length);

                    if (length == 1) {
                        gd.setColor(evaluateColor(ID_COLORS_GD, fraction, startColors[0], endColors[0]));
                    } else {
                        int[] targetColors = new int[length];
                        for (int i = 0; i < targetColors.length; i++) {
                            targetColors[i] = evaluateColor(ID_COLORS_GD, fraction, startColors[i], endColors[i]);
                        }
                        gd.setColors(targetColors);
                    }
                }
            } else if (canEvaluate(ID_COLOR_STATE_LIST_GD, fraction)) {
                // Animate ColorStateList if none of them is null.
                GradientDrawableWrapper.setColor(gd, evaluate(ID_COLOR_STATE_LIST_GD, fraction, colorStateListEvaluator, startCSL, endCSL));
            }

            if ((boolean) savedValues[HAS_RADII_GD] && canEvaluate(ID_RADII_GD, fraction)) {
                // Animate cornerRadii
                float[] sRadii = (float[]) savedValues[START_RADII_GD];
                float[] eRadii = (float[]) savedValues[END_RADII_GD];
                gd.setCornerRadii(evaluate(ID_RADII_GD, fraction, sRadii, eRadii));
            }

            if ((boolean) savedValues[HAS_RADIUS_GD] && canEvaluate(ID_RADIUS_GD, fraction)) {
                // Animate cornerRadius
                float sRadius = (float) savedValues[START_RADIUS_GD];
                float eRadius = (float) savedValues[END_RADIUS_GD];
                if (sRadius != eRadius)
                    gd.setCornerRadius(evaluate(ID_RADIUS_GD, fraction, sRadius, eRadius));
            }

            if ((boolean) savedValues[HAS_GRADIENT_RADIUS_GD] && canEvaluate(ID_GRADIENT_RADIUS_GD, fraction)) {
                // Animate gradient radius
                float sGRadius = (float) savedValues[START_GRADIENT_RADIUS_GD];
                float eGRadius = (float) savedValues[END_GRADIENT_RADIUS_GD];
                if (sGRadius != eGRadius)
                    gd.setGradientRadius(evaluate(ID_GRADIENT_RADIUS_GD, fraction, sGRadius, eGRadius));
            }

            if ((boolean) savedValues[HAS_GRADIENT_CENTER_GD] && canEvaluate(ID_GRADIENT_CENTER_GD, fraction)) {
                // Animate gradient center
                float centerX, centerY;

                float sGradientCenterX = (float) savedValues[START_GRADIENT_CENTER_X_GD];
                float eGradientCenterX = (float) savedValues[END_GRADIENT_CENTER_X_GD];
                if (sGradientCenterX == eGradientCenterX)
                    centerX = sGradientCenterX;
                else
                    centerX = evaluate(ID_GRADIENT_CENTER_X_GD, fraction, sGradientCenterX, eGradientCenterX);

                float sGradientCenterY = (float) savedValues[START_GRADIENT_CENTER_Y_GD];
                float eGradientCenterY = (float) savedValues[END_GRADIENT_CENTER_Y_GD];
                if (sGradientCenterY == eGradientCenterY)
                    centerY = sGradientCenterY;
                else
                    centerY = evaluate(ID_GRADIENT_CENTER_Y_GD, fraction, sGradientCenterY, eGradientCenterY);

                gd.setGradientCenter(centerX, centerY);
            }

            if ((boolean) savedValues[HAS_STROKE] && canEvaluate(ID_STROKE_GD, fraction)) {
                // Animate stroke properties

                ColorStateList startStrokeColor = (ColorStateList) savedValues[START_STROKE_COLOR];
                ColorStateList endStrokeColor = (ColorStateList) savedValues[END_STROKE_COLOR];
                int startStrokeWidth = (int) savedValues[START_STROKE_WIDTH];
                int endStrokeWidth = (int) savedValues[END_STROKE_WIDTH];
                float startStrokeDashWidth = (float) savedValues[START_STROKE_DASH_WIDTH];
                float endStrokeDashWidth = (float) savedValues[END_STROKE_DASH_WIDTH];
                float startStrokeDashGap = (float) savedValues[START_STROKE_DASH_GAP];
                float endStrokeDashGap = (float) savedValues[END_STROKE_DASH_GAP];

                if (startStrokeWidth != 0 || endStrokeWidth != 0) {
                    ColorStateList targetStrokeColor;
                    int targetStrokeWidth;
                    float targetStrokeDashWidth;
                    float targetStrokeDashGap;

                    if (startStrokeColor != endStrokeColor)
                        targetStrokeColor = evaluate(ID_STROKE_COLOR_GD, fraction, strokeColorStateListEvaluator, startStrokeColor, endStrokeColor);
                    else
                        targetStrokeColor = endStrokeColor;

                    if (startStrokeWidth != endStrokeWidth)
                        targetStrokeWidth = evaluate(ID_STROKE_WIDTH_GD, fraction, startStrokeWidth, endStrokeWidth);
                    else
                        targetStrokeWidth = endStrokeWidth;

                    if (startStrokeDashWidth != endStrokeDashWidth)
                        targetStrokeDashWidth = evaluate(ID_STROKE_DASH_WIDTH_GD, fraction, startStrokeDashWidth, endStrokeDashWidth);
                    else
                        targetStrokeDashWidth = endStrokeDashWidth;

                    if (startStrokeDashGap != endStrokeDashGap)
                        targetStrokeDashGap = evaluate(ID_STROKE_DASH_GAP_GD, fraction, startStrokeDashGap, endStrokeDashGap);
                    else
                        targetStrokeDashGap = endStrokeDashGap;

                    GradientDrawableWrapper.setStroke(gd, targetStrokeWidth, targetStrokeColor, targetStrokeDashWidth, targetStrokeDashGap);
                }
            }

            if ((boolean) savedValues[HAS_ALPHA] && canEvaluate(ID_ALPHA_GD, fraction)) {
                // Animate alpha
                newDrawable.setAlpha(evaluate(ID_ALPHA_GD, fraction, (int) savedValues[START_ALPHA], (int) savedValues[END_ALPHA]));
            }

            if (shaderEvaluator != null && canEvaluate(ID_SHADER_GD, fraction)) {
                // Apply old settings
                // So the GradientDrawable won't change the new Shader.
                // ensureValidRect & setShader must be the final step
                GradientDrawableWrapper.ensureValidRect(gd, tmpCanvas);

                // Animate shader by using reflection
                Paint fillPaint = GradientDrawableWrapper.getFillPaint(gd);
                if (fillPaint != null)
                    fillPaint.setShader(evaluateShader(fraction, gd, fillPaint));
            }
        }

        return newDrawable;
    }

    // Evaluate values

    protected Shader evaluateShader(float fraction, GradientDrawable gd, Paint fillPaint) {
        if (canEvaluate(ID_SHADER_GD, fraction))
            return shaderEvaluator.evaluate(fraction, GradientDrawableWrapper.getColors(gd), gd);
        else
            return fillPaint.getShader();
    }

    protected float evaluate(int id, float fraction, float startValue, float endValue) {
        if (canEvaluate(id, fraction))
            return floatEvaluator.evaluate(fraction, startValue, endValue);
        else
            return endValue;
    }

    protected float[] evaluate(int id, float fraction, float[] startValue, float[] endValue) {
        if (canEvaluate(id, fraction))
            return floatArrayEvaluator.evaluate(fraction, startValue, endValue);
        else
            return endValue;
    }

    protected int evaluate(int id, float fraction, int startValue, int endValue) {
        if (canEvaluate(id, fraction))
            return intEvaluator.evaluate(fraction, startValue, endValue);
        else
            return endValue;
    }

    protected int evaluateColor(int id, float fraction, int startValue, int endValue) {
        if (canEvaluate(id, fraction))
            return (int) argbEvaluator.evaluate(fraction, startValue, endValue);
        else
            return endValue;
    }

    protected ColorStateList evaluate(int id, float fraction, ColorStateListEvaluator evaluator, ColorStateList startValue, ColorStateList endValue) {
        if (canEvaluate(id, fraction))
            return evaluator.evaluate(fraction, startValue, endValue);
        else
            return endValue;
    }

    protected Drawable evaluate(int id, float fraction, Drawable startValue, Drawable endValue) {
        if (canEvaluate(id, fraction))
            return fadeDrawableEvaluator.evaluate(fraction, startValue, endValue);
        else
            return endValue;
    }

    /**
     * Specifies whether the DrawableEvaluator can animate the value.
     * Useful for subclasses
     *
     * @param id       ID of Animation
     * @param fraction The fraction from the starting to the ending values
     * @return True if DrawableEvaluator can animate this value
     */
    protected boolean canEvaluate(int id, float fraction) {
        return true;
    }


    // Helper methods to get target values from drawable
    // I should use GradientDrawableWrapper to support old Androids (api < 24)
    // Also some properties are private, so i've used reflection too!

    private static Object getGradientCenterX(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return null;
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getGradientCenterX(gd);
        }
        return null;
    }

    private static Object getGradientCenterY(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return null;
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getGradientCenterY(gd);
        }
        return null;
    }

    private static Object getGradientRadius(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return null;
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getGradientRadius(gd);
        }
        return null;
    }

    private static Object getCornerRadius(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return null;
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getCornerRadius(gd);
        }
        return null;
    }

    private static Object getCornerRadii(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return null;
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getCornerRadii(gd);
        }
        return null;
    }

    // Keep color methods public
    // Some rules may need this one such as BackgroundColor.

    public static int getColor(Drawable drawable, int defaultColor) {
        return (int) getColor(drawable, (Object) defaultColor);
    }

    public static Object getColor(Drawable drawable, Object defaultColor) {
        if (drawable instanceof ColorDrawable) {
            return ((ColorDrawable) drawable).getColor();
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            ColorStateList color = GradientDrawableWrapper.getColor(gd);
            if (color != null) {
                return color.getDefaultColor();
            } else {
                int[] c = GradientDrawableWrapper.getColors(gd);
                if (c != null && c.length > 0) {
                    return c[0];
                }
            }
        }
        return defaultColor;
    }

    private static int[] getColors(Drawable drawable, int defaultColor) {
        if (drawable instanceof ColorDrawable) {
            return new int[]{((ColorDrawable) drawable).getColor()};
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            int[] colors = GradientDrawableWrapper.getColors(gd);
            if (colors != null) {
                return colors;
            }
            ColorStateList color = GradientDrawableWrapper.getColor(gd);
            if (color != null) {
                return new int[]{color.getDefaultColor()};
            }
        }
        return new int[]{defaultColor};
    }

    private static ColorStateList getColorStateList(Drawable drawable) {
        if (drawable instanceof ColorDrawable) {
            return ColorStateList.valueOf(((ColorDrawable) drawable).getColor());
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getColor(gd);
        }
        return null;
    }

    private static ColorStateList getStrokeColor(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getStrokeColor(gd);
        }
        return null;
    }

    private static int getStrokeWidth(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getStrokeWidth(gd);
        }
        return 0;
    }

    private static float getStrokeDashWidth(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getStrokeDashWidth(gd);
        }
        return 0;
    }

    private static float getStrokeDashGap(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) drawable;
            return GradientDrawableWrapper.getStrokeDashGap(gd);
        }
        return 0;
    }

    /**
     * @return True if drawables have different gradient types (or positions).
     */
    private static boolean shouldUseShaderEvaluator(GradientDrawable gd1, GradientDrawable gd2, Canvas tmpCanvas) {
        GradientDrawableWrapper.ensureValidRect(gd1, tmpCanvas);
        GradientDrawableWrapper.ensureValidRect(gd2, tmpCanvas);

        Paint startFillPaint = GradientDrawableWrapper.getFillPaint(gd1);
        Paint endFillPaint = GradientDrawableWrapper.getFillPaint(gd2);
        if (startFillPaint == null || endFillPaint == null)
            return false;

        int type1 = GradientDrawableWrapper.getGradientType(gd1);
        int type2 = GradientDrawableWrapper.getGradientType(gd2);
        if (type1 != type2)
            return true;

        if (type1 == GradientDrawable.LINEAR_GRADIENT) {
            return gd1.getOrientation() != gd2.getOrientation() ||
                    GradientDrawableWrapper.getPositions(gd1) != GradientDrawableWrapper.getPositions(gd2);
        } else if (type1 == GradientDrawable.RADIAL_GRADIENT) {
            return GradientDrawableWrapper.getGradientRadius(gd1)
                    != GradientDrawableWrapper.getGradientRadius(gd2);
        } else if (type1 == GradientDrawable.SWEEP_GRADIENT) {
            return GradientDrawableWrapper.getUseLevel(gd1)
                    || GradientDrawableWrapper.getUseLevel(gd2);
        }
        return false;
    }

    /**
     * @return True if d1 & d2 both are ColorDrawable or GradientDrawable
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean evaluatorSupports(Drawable d1, Drawable d2) {
        return (d1 instanceof ColorDrawable || d1 instanceof GradientDrawable)
                && (d2 instanceof ColorDrawable || d2 instanceof GradientDrawable);
    }

    private class ShaderEvaluator {
        private final RectFEvaluator rectEvaluator = new RectFEvaluator();
        final RectF start;
        final RectF end;
        float[] startPositions;
        float[] endPositions;
        private final int endType;

        private final int[] startColors, endColors;
        private boolean startOrientation = false;

        private final GradientDrawable.Orientation[] orientations;

        public ShaderEvaluator(GradientDrawable startDrawable, GradientDrawable endDrawable, int[] startColors, int[] endColors) {
            orientations = new GradientDrawable.Orientation[]
                    {startDrawable.getOrientation(), endDrawable.getOrientation()};
            GradientDrawableWrapper.ensureValidRect(startDrawable, tmpCanvas);
            GradientDrawableWrapper.ensureValidRect(endDrawable, tmpCanvas);

            // Only startDrawable has bounds,
            // So for both drawables i should use this one.
            final RectF r = GradientDrawableWrapper.getRect(startDrawable);

            int startType = GradientDrawableWrapper.getGradientType(startDrawable);
            endType = GradientDrawableWrapper.getGradientType(endDrawable);

            start = createRect(startDrawable, r, startType);
            end = createRect(endDrawable, r, endType);

            Object sp = GradientDrawableWrapper.getPositions(startDrawable);
            if (sp != null)
                startPositions = (float[]) sp;
            else
                startPositions = new float[0];

            Object ep = GradientDrawableWrapper.getPositions(endDrawable);
            if (ep != null)
                endPositions = (float[]) ep;
            else
                endPositions = new float[0];

            int pLength = Math.max(startPositions.length, endPositions.length);
            if (pLength == 0) {
                startPositions = null;
                endPositions = null;
            } else {
                if (startPositions.length != pLength) {
                    float[] tmpPositions = new float[pLength];
                    for (int i = 0; i < tmpPositions.length; i++) {
                        if (startPositions.length > i) {
                            tmpPositions[i] = startPositions[i];
                        } else {
                            tmpPositions[i] = 0;
                        }
                    }
                    this.startPositions = tmpPositions;
                }

                if (endPositions.length != pLength) {
                    float[] tmpPositions = new float[pLength];
                    for (int i = 0; i < tmpPositions.length; i++) {
                        if (endPositions.length > i) {
                            tmpPositions[i] = endPositions[i];
                        } else {
                            tmpPositions[i] = 0;
                        }
                    }
                    this.endPositions = tmpPositions;
                }
            }

            int length = Math.max(startColors.length, endColors.length);
            int lastColor;

            if (startColors.length != length) {
                int[] tmpColors = new int[length];
                lastColor = Color.TRANSPARENT;
                for (int i = 0; i < tmpColors.length; i++) {
                    if (startColors.length > i) {
                        lastColor = startColors[i];
                        tmpColors[i] = startColors[i];
                    } else {
                        tmpColors[i] = lastColor;
                    }
                }
                this.startColors = tmpColors;
            } else {
                this.startColors = endColors;
            }

            if (endColors.length != length) {
                int[] tmpColors = new int[length];
                lastColor = Color.TRANSPARENT;
                for (int i = 0; i < tmpColors.length; i++) {
                    if (endColors.length > i) {
                        lastColor = endColors[i];
                        tmpColors[i] = endColors[i];
                    } else {
                        tmpColors[i] = lastColor;
                    }
                }
                this.endColors = tmpColors;
            } else {
                this.endColors = endColors;
            }
        }

        public Shader evaluate(float fraction, int[] colors, GradientDrawable drawable) {

            int[] targetColors = colors;
            if (targetColors == null) {
                targetColors = new int[startColors.length];
                for (int i = 0; i < targetColors.length; i++) {
                    targetColors[i] = (Integer) argbEvaluator.evaluate(fraction, startColors[i], endColors[i]);
                }
            }

            if (endType == GradientDrawable.LINEAR_GRADIENT) {
                // Prevents possible lags.
                if (fraction < 0.5f) {
                    if (!startOrientation) {
                        GradientDrawableWrapper.setOrientationImmediate(drawable, orientations[0]);
                        startOrientation = true;
                    }
                } else if (fraction >= 0.9) {
                    if (startOrientation) {
                        GradientDrawableWrapper.setOrientationImmediate(drawable, orientations[1]);
                        startOrientation = false;
                    }
                }

                RectF targetRect = rectEvaluator.evaluate(fraction, start, end);

                float[] targetPosition = endPositions;
                if (startPositions != null && endPositions != null)
                    targetPosition = floatArrayEvaluator.evaluate(fraction, startPositions, endPositions);

                return new LinearGradient(targetRect.left, targetRect.top, targetRect.right, targetRect.bottom,
                        targetColors, targetPosition, Shader.TileMode.CLAMP);

            } else if (endType == GradientDrawable.RADIAL_GRADIENT) {
                float radius = GradientDrawableWrapper.getGradientRadius(drawable);
                if (radius <= 0) {
                    // We can't have a shader with non-positive radius, so
                    // let's have a very, very small radius.
                    radius = 0.001f;
                }

                RectF end = new RectF(this.end);
                end.left -= radius;
                end.top -= radius;
                end.right += radius;
                end.bottom += radius;

                RectF targetRect = rectEvaluator.evaluate(fraction, start, end);
                return new RadialGradient(targetRect.centerX(), targetRect.centerY(), radius,
                        targetColors, null, Shader.TileMode.CLAMP);
            } else {
                RectF targetRect = rectEvaluator.evaluate(fraction, start, end);

                int[] tempColors = targetColors;
                float[] tempPositions = null;

                if (GradientDrawableWrapper.getUseLevel(drawable)) {
                    int length = targetColors.length;
                    tempColors = new int[length + 1];
                    System.arraycopy(targetColors, 0, tempColors, 0, length);
                    tempColors[length] = targetColors[length - 1];

                    final float position_fraction = 1.0f / (length - 1);
                    tempPositions = new float[length + 1];

                    final float level = drawable.getLevel() / 10000.0f;
                    for (int i = 0; i < length; i++) {
                        tempPositions[i] = i * position_fraction * level;
                    }
                    tempPositions[length] = 1.0f;
                }

                return new SweepGradient(targetRect.centerX(), targetRect.centerY(), tempColors, tempPositions);
            }
        }


        private RectF createRect(GradientDrawable drawable, RectF r, int type) {
            float x0 = 0, x1 = 0, y0 = 0, y1 = 0;

            switch (type) {
                case GradientDrawable.LINEAR_GRADIENT:
                    final float level = GradientDrawableWrapper.getUseLevel(drawable) ? drawable.getLevel() / 10000.0f : 1.0f;
                    switch (drawable.getOrientation()) {
                        case TOP_BOTTOM:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = x0;
                            y1 = level * r.bottom;
                            break;
                        case TR_BL:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = level * r.bottom;
                            break;
                        case RIGHT_LEFT:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = y0;
                            break;
                        case BR_TL:
                            x0 = r.right;
                            y0 = r.bottom;
                            x1 = level * r.left;
                            y1 = level * r.top;
                            break;
                        case BOTTOM_TOP:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = x0;
                            y1 = level * r.top;
                            break;
                        case BL_TR:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = level * r.right;
                            y1 = level * r.top;
                            break;
                        case LEFT_RIGHT:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = y0;
                            break;
                        default:/* TL_BR */
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = level * r.bottom;
                            break;
                    }
                    break;
                case GradientDrawable.RADIAL_GRADIENT:
                case GradientDrawable.SWEEP_GRADIENT:
                    x0 = r.left + (r.right - r.left) * GradientDrawableWrapper.getGradientCenterX(drawable);
                    y0 = r.top + (r.bottom - r.top) * GradientDrawableWrapper.getGradientCenterY(drawable);
                    x1 = x0;
                    y1 = y0;

                    break;
            }
            return new RectF(x0, y0, x1, y1);
        }
    }
}