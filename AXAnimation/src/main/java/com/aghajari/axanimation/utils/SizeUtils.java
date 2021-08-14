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

import android.view.Gravity;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A helper class for finding real sizes from releated values
 *
 * @author AmirHossein Aghajari
 * @see com.aghajari.axanimation.livevar.LiveSize
 */
public class SizeUtils {

    private static final int SPECIFIED = 0x0001;
    private static final int BEFORE = 0x0002;
    private static final int AFTER = 0x0004;
    private static final int SHIFT = 8;
    public static final int PARENT = -(BEFORE | SPECIFIED) << SHIFT;
    public static final int TARGET = -(AFTER | SPECIFIED) << SHIFT;
    public static final int ORIGINAL = -(SPECIFIED) << SHIFT;
    public static final int MASK = (-SPECIFIED | -BEFORE | -AFTER) << SHIFT;

    private SizeUtils() {
    }

    public static float calculate(float value, int viewWidth, int viewHeight,
                                  LayoutSize parent, LayoutSize target, LayoutSize original,
                                  int gravity) {
        if (!isCustomSize((int) value))
            return value;

        if (value == AXAnimation.PARENT_WIDTH)
            return parent.getWidth();
        else if (value == AXAnimation.PARENT_HEIGHT)
            return parent.getHeight();
        else if (value == AXAnimation.MATCH_PARENT)
            return Gravity.isHorizontal(gravity) ? parent.getWidth() : parent.getHeight();
        else if (value == AXAnimation.WRAP_CONTENT)
            return Gravity.isHorizontal(gravity) ? viewWidth : viewHeight;
        else if (value == AXAnimation.CONTENT_WIDTH)
            return viewWidth;
        else if (value == AXAnimation.CONTENT_HEIGHT)
            return viewHeight;
        else if (value == AXAnimation.ORIGINAL)
            return original.get((int) value, gravity);
        else if (value == AXAnimation.TARGET)
            return target.get((int) value, gravity);
        else if (value == AXAnimation.PARENT)
            return parent.get((int) value, gravity);
        else if (((int) value & MASK) == PARENT)
            return parent.get((int) value, ((int) value &
                    (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK)));
        else if (((int) value & MASK) == ORIGINAL)
            return original.get((int) value, ((int) value &
                    (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK)));
        else if (((int) value & MASK) == TARGET)
            return target.get((int) value, ((int) value &
                    (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK)));

        return value;
    }

    public static boolean isCustomSize(int value) {
        if (value == AXAnimation.CONTENT_WIDTH
                || value == AXAnimation.CONTENT_HEIGHT
                || value == AXAnimation.PARENT_WIDTH
                || value == AXAnimation.PARENT_HEIGHT
                || value == PARENT
                || value == ORIGINAL
                || value == TARGET)
            return true;

        int m = value & MASK;
        if (m == PARENT || m == ORIGINAL || m == TARGET) {
            int g = value & (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK);
            return Gravity.isHorizontal(g) || Gravity.isVertical(g);
        }
        return false;
    }

    public static int calculate(int value, int viewWidth, int viewHeight,
                                LayoutSize parent, LayoutSize target, LayoutSize original,
                                int gravity) {
        return (int) calculate((float) value, viewWidth, viewHeight, parent, target, original, gravity);
    }
}
