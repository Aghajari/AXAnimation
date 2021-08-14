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

import android.view.View;
import android.view.ViewGroup;

import com.aghajari.axanimation.livevar.LayoutSize;

/**
 * @hide
 */
public interface AnimatedLayout {

    /**
     * Gets {@link LayoutSize} for this view with it's own LayoutParams.
     * Whenever the layout is ready,
     * Call {@link OnLayoutSizeReadyListener#onReady(View, LayoutSize)} on the listener.
     */
    void getLayoutSize(View view, OnLayoutSizeReadyListener listener);

    /**
     * Gets {@link LayoutSize} for this view with a custom LayoutParams.
     * Whenever the layout is ready,
     * Call {@link OnLayoutSizeReadyListener#onReady(View, LayoutSize)} on the listener.
     */
    void getLayoutSize(View view, ViewGroup.LayoutParams layoutParams, OnLayoutSizeReadyListener listener);

    /**
     * @return {@link LayoutSize} of this layout as ParentSize
     */
    LayoutSize getLayoutSize();

}
