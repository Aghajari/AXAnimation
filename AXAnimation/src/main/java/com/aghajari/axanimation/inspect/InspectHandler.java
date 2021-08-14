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
package com.aghajari.axanimation.inspect;

import android.view.ViewGroup;

import com.aghajari.axanimation.layouts.AnimatedLayoutParams;

/**
 * @hide
 */
public class InspectHandler {

    InspectView inspectView;

    public void getReadyForInspect(ViewGroup parent, boolean enabled) {
        if (!enabled && inspectView != null) {
            inspectView.getElements().clear();
            parent.removeView(inspectView);
        } else if (enabled && inspectView == null) {
            inspectView = new InspectView(parent.getContext());
            parent.addView(inspectView);

            AnimatedLayoutParams lp = new AnimatedLayoutParams(-1, -1);
            lp.skipMeasure = true;
            inspectView.setLayoutParams(lp);
        }

        if (inspectView != null) {
            inspectView.getElements().clear();
            inspectView.invalidate();
        }
    }

    public InspectView getInspectView() {
        return inspectView;
    }
}
