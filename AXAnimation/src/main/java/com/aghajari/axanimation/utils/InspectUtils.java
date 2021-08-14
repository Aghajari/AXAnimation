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

import android.graphics.Point;
import android.view.View;

import com.aghajari.axanimation.inspect.InspectLayout;
import com.aghajari.axanimation.inspect.InspectView;
import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * A helper class for using {@link InspectView} on a {@link InspectLayout}
 *
 * @author AmirHossein Aghajari
 */
public class InspectUtils {

    private InspectUtils() {
    }

    public static void inspect(View targetView, View view, LayoutSize size, int gravity, boolean t) {
        if (targetView.getParent() instanceof InspectLayout) {
            InspectView inspectView = ((InspectLayout) targetView.getParent()).getInspectView();
            if (inspectView != null) {
                View v = view == null ? (View) targetView.getParent() : view;
                inspectView.inspect(v, size, gravity, t);
            }
        }
    }

    public static void inspect(View targetView, View view, LayoutSize start, LayoutSize end, int gravityStart, int gravityEnd, Point delta) {
        if (targetView.getParent() instanceof InspectLayout) {
            InspectView inspectView = ((InspectLayout) targetView.getParent()).getInspectView();
            if (inspectView != null) {
                View v = view == null ? (View) targetView.getParent() : view;
                inspectView.inspect(v, start, end, gravityStart, gravityEnd, delta);
            }
        }
    }

    public static void inspect(View targetView, View view, LayoutSize start, int gravityStart, final Point end, final boolean reverse, final boolean horizontal) {
        if (targetView.getParent() instanceof InspectLayout) {
            InspectView inspectView = ((InspectLayout) targetView.getParent()).getInspectView();
            if (inspectView != null) {
                View v = view == null ? (View) targetView.getParent() : view;
                inspectView.inspect(v, start, gravityStart, end, reverse, horizontal);
            }
        }
    }

    public static void clearInspect(View targetView) {
        if (targetView.getParent() instanceof InspectLayout) {
            InspectView inspectView = ((InspectLayout) targetView.getParent()).getInspectView();
            if (inspectView != null) {
                inspectView.clearInspect();
            }
        }
    }

    public static void removeInspectView(View view) {
        if (view instanceof InspectLayout) {
            ((InspectLayout) view).getReadyForInspect(false);
        } else if (view.getParent() instanceof InspectLayout) {
            ((InspectLayout) view.getParent()).getReadyForInspect(false);
        }
    }
}
