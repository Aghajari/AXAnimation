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
package com.aghajari.axanimation.layouts;

import android.view.ViewGroup;

import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A custom LayoutParams which contains view's layout while {@link com.aghajari.axanimation.AXAnimation} is running.
 * Only a {@link AnimatedLayout} can use this LayoutParams.
 *
 * @author AmirHossein Aghajari
 */
public class AnimatedLayoutParams extends ViewGroup.MarginLayoutParams {
    public ViewGroup.LayoutParams original;
    public LayoutSize originalLayout;
    public int left, top, right, bottom;

    /**
     * skipMeasure will tell the view's parent How should the parent treat the child
     */
    public boolean skipMeasure;

    public AnimatedLayoutParams(ViewGroup.LayoutParams lp, LayoutSize ls) {
        super(lp);
        original = lp;
        this.left = ls.left;
        this.top = ls.top;
        this.right = ls.right;
        this.bottom = ls.bottom;
    }

    public AnimatedLayoutParams(int width, int height) {
        super(width, height);
        left = top = 0;
        right = width;
        bottom = height;
    }

    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }

    public ViewGroup.LayoutParams getRealOriginal() {
        return getRealOriginal(this);
    }

    private static ViewGroup.LayoutParams getRealOriginal(AnimatedLayoutParams lp) {
        if (lp.original instanceof AnimatedLayoutParams) {
            return getRealOriginal((AnimatedLayoutParams) lp.original);
        } else {
            return lp.original;
        }
    }
}