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

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.draw.rules.MatrixRule;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to handle {@link DrawableLayout} rules.
 *
 * @author AmirHossein Aghajari
 */
public class DrawHandler {

    private transient final Map<String, OnDraw> onDraws = new HashMap<>();

    public void add(String key, OnDraw onDraw) {
        onDraws.put(key, onDraw);
    }

    public void remove(String key) {
        onDraws.remove(key);
    }

    public void clear() {
        onDraws.clear();
    }

    public OnDraw get(String key) {
        return onDraws.get(key);
    }

    public Map<String, OnDraw> getOnDraws() {
        return onDraws;
    }


    /**
     * Call this on {@link View#dispatchDraw(Canvas)}
     * Once before calling super.dispatchDraw(Canvas) (front = False)
     * And once after calling super.dispatchDraw(Canvas) (front = True)
     */
    @SuppressWarnings("JavadocReference")
    public void draw(DrawableLayout drawableLayout, Canvas canvas, boolean front) {
        applyMatrix(drawableLayout, canvas, front);

        for (OnDraw onDraw : onDraws.values()) {
            if (!(onDraw instanceof MatrixRule) && onDraw.isDrawingOnFront() == front) {
                onDraw.onDraw(drawableLayout, canvas);
            }
        }
    }

    private void applyMatrix(DrawableLayout drawableLayout, Canvas canvas, boolean front) {
        for (OnDraw onDraw : onDraws.values()) {
            if (onDraw instanceof MatrixRule && onDraw.isDrawingOnFront() == front) {
                onDraw.onDraw(drawableLayout, canvas);
            }
        }
    }

    static boolean canDraw(View view, final String key, boolean checkParent) {
        if (view == null)
            return false;

        return (view instanceof DrawableLayout && ((DrawableLayout) view).canDraw(key)) ||
                (checkParent && canDraw((View) view.getParent(), key, true));
    }

    @NonNull
    static View getDrawableView(View view, final String key) {
        if (view == null)
            throw new RuntimeException("Couldn't find a DrawableLayout!");

        if (canDraw(view, key, false)) {
            return view;
        } else {
            return getDrawableView((View) view.getParent(), key);
        }
    }
}
