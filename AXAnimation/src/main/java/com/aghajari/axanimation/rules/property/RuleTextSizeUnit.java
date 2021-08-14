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
package com.aghajari.axanimation.rules.property;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.aghajari.axanimation.livevar.LiveVar;
import com.aghajari.axanimation.rules.PropertyValueRule;
import com.aghajari.axanimation.rules.Rule;

/**
 * A {@link Rule} to change TextView's textSize
 *
 * @author AmirHossein Aghajari
 * @see TextView#setTextSize(int, float)
 */
public class RuleTextSizeUnit extends PropertyValueRule<Float> {
    private final int unit;

    public RuleTextSizeUnit(int unit, Float... data) {
        super(null, "textSize", data);
        this.unit = unit;
    }

    public RuleTextSizeUnit(int unit, LiveVar<Float[]> data) {
        super(null, "textSize", data);
        this.unit = unit;
    }

    @Override
    public Object getStartValue(View view) {
        Context c = view.getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        return reverseDimension(unit, ((TextView) view).getTextSize(), r.getDisplayMetrics());
    }

    private static float reverseDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_PX:
                return value;
            case TypedValue.COMPLEX_UNIT_DIP:
                return value / metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return value / metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return value / (metrics.xdpi * (1.0f / 72));
            case TypedValue.COMPLEX_UNIT_IN:
                return value / metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return value / (metrics.xdpi * (1.0f / 25.4f));
        }
        return value;
    }

    @Override
    public void onAnimationUpdate(View view, ValueAnimator animator, Float value) {
        super.onAnimationUpdate(view, animator, value);
        ((TextView) view).setTextSize(unit, value);
    }

}
