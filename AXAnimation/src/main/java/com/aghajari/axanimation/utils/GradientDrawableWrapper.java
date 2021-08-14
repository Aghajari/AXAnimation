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
package com.aghajari.axanimation.utils;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.annotation.Nullable;

/**
 * Helper class to get and set a GradientDrawable properties. (+ private fields)
 * Works on all android versions.
 * Tested on platforms: 21, 23, 28 and 30
 *
 * @author AmirHossein Aghajari
 * @see android.graphics.drawable.GradientDrawable
 * @see com.aghajari.axanimation.evaluator.DrawableEvaluator
 */
public class GradientDrawableWrapper {

    private GradientDrawableWrapper() {
    }

    @Nullable
    public static float[] getCornerRadii(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                return drawable.getCornerRadii();
            } catch (Exception ignore) {
                // Platform-30, throws NullPointerException If you haven't set it before.
                // "Attempt to invoke virtual method 'java.lang.Object float[].clone()' on a null object reference"
                return null;
            }
        } else {
            Object o = ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mRadiusArray");
            if (o != null)
                return ((float[]) o).clone();
        }
        return null;
    }

    public static float getCornerRadius(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getCornerRadius();
        } else {
            return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mRadius", 0f);
        }
    }

    public static float getGradientCenterX(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getGradientCenterX();
        } else {
            return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mCenterX", 0.5f);
        }
    }

    public static float getGradientCenterY(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getGradientCenterY();
        } else {
            return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mCenterY", 0.5f);
        }
    }

    public static float getGradientRadius(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getGradientRadius();
        } else {
            return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mGradientRadius", 0.5f);
        }
    }

    public static int[] getColors(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getColors();
        } else {
            Object o = ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mGradientColors");
            if (o != null)
                return ((int[]) o).clone();
        }
        return null;
    }

    @Nullable
    public static ColorStateList getColor(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getColor();
        } else {
            try {
                Object o = ReflectionUtils.getPrivateFieldValueWithThrows(drawable.getConstantState(), "mSolidColors");
                if (o != null)
                    return (ColorStateList) o;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Object c = ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mSolidColor");
                if (c != null)
                    return ColorStateList.valueOf((int) c);
            }
        }
        return null;
    }

    @Nullable
    public static ColorStateList getStrokeColor(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            try {
                Object o = ReflectionUtils.getPrivateFieldValueWithThrows(drawable.getConstantState(), "mStrokeColors");
                if (o != null)
                    return (ColorStateList) o;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Object c = ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mStrokeColor");
                if (c != null)
                    return ColorStateList.valueOf((int) c);
            }
        } else {
            Object c = ReflectionUtils.getPrivateFieldValue(drawable, "mStrokePaint");
            if (c != null)
                return ColorStateList.valueOf(((Paint) c).getColor());
        }
        return null;
    }

    public static int getStrokeWidth(GradientDrawable drawable) {
        int v = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mStrokeWidth", 0);
        } else {
            Object c = ReflectionUtils.getPrivateFieldValue(drawable, "mStrokePaint");
            if (c != null)
                v = (int) ((Paint) c).getStrokeWidth();
        }
        // animator should not animate stroke from -1 to 0
        if (v == -1)
            v = 0;
        return v;
    }

    public static float getStrokeDashWidth(GradientDrawable drawable) {
        return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mStrokeDashWidth", 0f);
    }

    public static float getStrokeDashGap(GradientDrawable drawable) {
        return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mStrokeDashGap", 0f);
    }

    public static boolean getUseLevel(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getUseLevel();
        } else {
            return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mUseLevel", false);
        }
    }

    @Nullable
    public static Paint getFillPaint(GradientDrawable drawable) {
        return ReflectionUtils.getPrivateFieldValue(drawable, "mFillPaint", null);
    }

    public static void ensureValidRect(GradientDrawable drawable, Canvas tmpCanvas) {
        // throws NoSuchMethodException on Platform30, SO STOP USING THIS METHOD
        //ReflectionUtils.invokePrivateMethod(drawable, "ensureValidRect");
        // Drawable will call this method on draw, so let's fake it.
        drawable.draw(tmpCanvas);
    }

    public static void setColor(GradientDrawable drawable, ColorStateList color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setColor(color);
        } else {
            drawable.setColor(color.getDefaultColor());
        }
    }

    public static void setStroke(GradientDrawable drawable, int width, ColorStateList colorStateList, float dashWidth, float dashGap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setStroke(width, colorStateList, dashWidth, dashGap);
        } else {
            drawable.setStroke(width, colorStateList.getDefaultColor(), dashWidth, dashGap);
        }
    }

    public static RectF getRect(GradientDrawable drawable) {
        try {
            Object o = ReflectionUtils.getPrivateFieldValueWithThrows(drawable, "mRect");
            if (o != null)
                return (RectF) o;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                float inset = getStrokeWidth(drawable) * 0.5f;
                RectF rect = new RectF(drawable.getBounds());
                rect.left += inset;
                rect.top += inset;
                rect.right -= inset;
                rect.bottom -= inset;
                return rect;
            } catch (Exception ignore) {
            }
        }
        return new RectF();
    }

    public static int getGradientType(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getGradientType();
        } else {
            return ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mGradient", GradientDrawable.LINEAR_GRADIENT);
        }
    }

    public static void setOrientationImmediate(GradientDrawable drawable, GradientDrawable.Orientation orientation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            try {
                ReflectionUtils.setPrivateFieldValueWithThrows(drawable.getConstantState(), "mOrientation", orientation);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                drawable.setOrientation(orientation);
            }
        } else {
            drawable.setOrientation(orientation);
        }
    }

    @Nullable
    public static float[] getPositions(GradientDrawable drawable) {
        Object o = ReflectionUtils.getPrivateFieldValue(drawable.getConstantState(), "mPositions");
        if (o != null)
            return ((float[]) o).clone();
        return null;
    }
}
