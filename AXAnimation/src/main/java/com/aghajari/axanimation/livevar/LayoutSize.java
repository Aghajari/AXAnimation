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

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.layouts.AnimatedLayoutParams;
import com.aghajari.axanimation.utils.SizeUtils;

import java.util.Map;

/**
 * A helper class to store Layout position.
 *
 * @author AmirHossein Aghajari
 */
public class LayoutSize implements Cloneable, LiveSizeDebugger {

    public int left, top, right, bottom;

    @Nullable
    public LiveSize liveLeft, liveTop, liveRight, liveBottom;

    public LayoutSize() {
    }

    public LayoutSize(LayoutSize size) {
        if (size != null)
            set(size);
    }

    public LayoutSize(AnimatedLayoutParams lp) {
        if (lp != null)
            set(lp.left, lp.top, lp.right, lp.bottom);
    }

    public LayoutSize(int left, int top, int right, int bottom) {
        set(left, top, right, bottom);
    }

    public LayoutSize(LiveSize left, LiveSize top, LiveSize right, LiveSize bottom) {
        set(left, top, right, bottom);
    }

    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }

    public boolean isEmpty() {
        return left == 0 && top == 0 && right == 0 && bottom == 0;
    }

    public int getCenterX() {
        return left + (getWidth() / 2);
    }

    public int getCenterY() {
        return top + (getHeight() / 2);
    }

    public Rect getRect() {
        return new Rect(left, top, right, bottom);
    }

    public RectF getRectF() {
        return new RectF(left, top, right, bottom);
    }

    public void set(int l, int t, int r, int b) {
        this.left = l;
        this.top = t;
        this.right = r;
        this.bottom = b;
        liveLeft = liveTop = liveRight = liveBottom = null;
    }

    public void set(LiveSize l, LiveSize t, LiveSize r, LiveSize b) {
        liveLeft = l;
        liveTop = t;
        liveRight = r;
        liveBottom = b;

        if (liveLeft == null)
            liveLeft = LiveSize.create(AXAnimation.ORIGINAL | Gravity.LEFT);
        if (liveRight == null)
            liveRight = LiveSize.create(AXAnimation.ORIGINAL | Gravity.RIGHT);
        if (liveTop == null)
            liveTop = LiveSize.create(AXAnimation.ORIGINAL | Gravity.TOP);
        if (liveBottom == null)
            liveBottom = LiveSize.create(AXAnimation.ORIGINAL | Gravity.BOTTOM);
    }

    public void set(Rect rect) {
        liveLeft = liveTop = liveRight = liveBottom = null;

        if (rect == null) {
            left = top = right = bottom = 0;
        } else {
            left = rect.left;
            top = rect.top;
            right = rect.right;
            bottom = rect.bottom;
        }
    }

    public void set(LayoutSize layoutSize) {
        if (layoutSize == null) {
            left = top = right = bottom = 0;
            liveLeft = liveTop = liveRight = liveBottom = null;
        } else {
            left = layoutSize.left;
            top = layoutSize.top;
            right = layoutSize.right;
            bottom = layoutSize.bottom;

            liveLeft = layoutSize.liveLeft;
            liveTop = layoutSize.liveTop;
            liveRight = layoutSize.liveRight;
            liveBottom = layoutSize.liveBottom;
        }
    }

    public void setOnlyTarget(LayoutSize layoutSize) {
        if (layoutSize == null) {
            left = top = right = bottom = 0;
        } else {
            left = layoutSize.left;
            top = layoutSize.top;
            right = layoutSize.right;
            bottom = layoutSize.bottom;
        }
    }

    public void setHorizontal(Rect rect) {
        liveRight = liveLeft = null;

        if (rect == null) {
            left = right = 0;
        } else {
            left = rect.left;
            right = rect.right;
        }
    }

    public void setHorizontal(LayoutSize layoutSize) {
        if (layoutSize == null) {
            left = right = 0;
        } else {
            left = layoutSize.left;
            right = layoutSize.right;
        }
    }

    public void setVertical(Rect rect) {
        liveBottom = liveTop = null;

        if (rect == null) {
            top = bottom = 0;
        } else {
            top = rect.top;
            bottom = rect.bottom;
        }
    }

    public void setVertical(LayoutSize layoutSize) {
        if (layoutSize == null) {
            top = bottom = 0;
        } else {
            top = layoutSize.top;
            bottom = layoutSize.bottom;
        }
    }

    public boolean equals(AnimatedLayoutParams lp) {
        return equals(lp.left, lp.top, lp.right, lp.bottom);
    }

    public boolean equals(int l, int t, int r, int b) {
        return this.left == l && this.top == t && this.right == r && this.bottom == b;
    }

    public int get(int value, int gravity) {
        if (Gravity.isHorizontal(gravity)) {
            int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            switch (hg) {
                case Gravity.LEFT:
                    return left;
                case Gravity.RIGHT:
                    return right;
                case Gravity.CENTER_HORIZONTAL:
                    return getCenterX();
                case Gravity.FILL_HORIZONTAL:
                    return getWidth();
            }
        } else {
            int vg = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (vg) {
                case Gravity.TOP:
                    return top;
                case Gravity.BOTTOM:
                    return bottom;
                case Gravity.CENTER_VERTICAL:
                    return getCenterY();
                case Gravity.FILL_VERTICAL:
                    return getHeight();
            }
        }
        return value;
    }

    /**
     * Get ready before creating animator
     * Call this on {@link com.aghajari.axanimation.rules.Rule#onCreateAnimator(View, LayoutSize, LayoutSize, LayoutSize)}
     * before creating animator values.
     *
     * @hide
     */
    public void prepare(int viewWidth, int viewHeight,
                        LayoutSize parent, LayoutSize target, LayoutSize original) {
        if (liveLeft == null) {
            left = SizeUtils.calculate(left, viewWidth, viewHeight, parent, target, original, Gravity.LEFT);
        } else {
            left = (int) liveLeft.calculate(viewWidth, viewHeight, parent, target, original, Gravity.LEFT);
        }
        if (liveTop == null) {
            top = SizeUtils.calculate(top, viewWidth, viewHeight, parent, target, original, Gravity.TOP);
        } else {
            top = (int) liveTop.calculate(viewWidth, viewHeight, parent, target, original, Gravity.TOP);
        }
        if (liveRight == null) {
            right = SizeUtils.calculate(right, viewWidth, viewHeight, parent, target, original, Gravity.RIGHT);
        } else {
            right = (int) liveRight.calculate(viewWidth, viewHeight, parent, target, original, Gravity.RIGHT);
        }
        if (liveBottom == null) {
            bottom = SizeUtils.calculate(bottom, viewWidth, viewHeight, parent, target, original, Gravity.BOTTOM);
        } else {
            bottom = (int) liveBottom.calculate(viewWidth, viewHeight, parent, target, original, Gravity.BOTTOM);
        }
    }

    public Point getPoint(int gravity) {
        Point p = new Point();
        final int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        switch (hg) {
            case Gravity.RIGHT:
                p.x = right;
                break;
            case Gravity.LEFT:
                p.x = left;
                break;
            case Gravity.FILL_HORIZONTAL:
                p.x = getWidth();
                break;
            default:
                p.x = getCenterX();
                break;
        }

        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        switch (verticalGravity) {
            case Gravity.TOP:
                p.y = top;
                break;
            case Gravity.BOTTOM:
                p.y = bottom;
                break;
            case Gravity.FILL_VERTICAL:
                p.y = getHeight();
                break;
            default:
                p.y = getCenterY();
                break;
        }

        return p;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "LayoutSize(" + left + ", " +
                top + ", " + right +
                ", " + bottom + ")";
    }

    @NonNull
    @Override
    public LayoutSize clone() {
        try {
            return (LayoutSize) super.clone();
        } catch (CloneNotSupportedException e) {
            return new LayoutSize(this);
        }
    }

    /**
     * @hide
     */
    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(this, view);
    }

}
