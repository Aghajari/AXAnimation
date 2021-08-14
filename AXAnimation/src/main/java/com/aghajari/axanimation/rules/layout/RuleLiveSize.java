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
package com.aghajari.axanimation.rules.layout;

import android.util.Pair;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aghajari.axanimation.layouts.AnimatedLayout;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.livevar.LiveSize;
import com.aghajari.axanimation.layouts.OnLayoutSizeReadyListener;
import com.aghajari.axanimation.livevar.LiveSizeDebugger;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;
import com.aghajari.axanimation.utils.InspectUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A custom {@link Rule} to change view's position by {@link LiveSize}.
 *
 * @author AmirHossein Aghajari
 * @see LiveSize
 */
public abstract class RuleLiveSize<T> extends RuleWithTmpData<LiveSize[], T> implements LiveSizeDebugger {

    protected final LiveSizeHandler handler = new LiveSizeHandler();

    public RuleLiveSize(LiveSize... data) {
        super(data);
    }

    @Override
    public long shouldWait() {
        return handler.shouldWait();
    }

    @Override
    public void getReady(@NonNull View view) {
        super.getReady(view);
        handler.getReady(view, Arrays.asList(data));
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

    @Override
    public void debug(@NonNull View view,
                      @Nullable LayoutSize target,
                      @Nullable LayoutSize original,
                      @Nullable LayoutSize parentSize) {
        if (animatorValues != null && animatorValues.isInspectEnabled()) {
            handler.debug(view, target, original, parentSize, Gravity.NO_GRAVITY);
        }
        handler.tmpSize = null;
    }

    public static class LiveSizeHandler {
        private final HashMap<Pair<LiveSize.Pair<Integer, Float>, LiveSize>, View> map = new HashMap<>();
        private final ArrayList<View> views = new ArrayList<>();
        private WeakReference<Collection<LiveSize>> tmpSize = null;

        public long shouldWait() {
            return map.isEmpty() ? -1 : 0;
        }

        public void getReady(@NonNull View view, Collection<LiveSize> data) {
            tmpSize = new WeakReference<>(data);

            for (LiveSize s : data) {
                for (LiveSize.Pair<Integer, Float> pair : s.getRelatedViews().keySet()) {
                    View relatedView = pair.view;
                    if (relatedView == null && pair.viewID != -1) {
                        relatedView = ((View) view.getParent()).findViewById(pair.viewID);
                    }
                    if (relatedView != null) {
                        map.put(Pair.create(pair, s), relatedView);
                        if (!views.contains(relatedView))
                            views.add(relatedView);
                    }
                }
            }

            for (int i = 0; i < views.size(); i++) {
                AnimatedLayout layout = (AnimatedLayout) view.getParent();
                layout.getLayoutSize(views.get(i), new OnLayoutSizeReadyListener() {
                    @Override
                    public void onReady(View view, LayoutSize size) {
                        for (Iterator<Map.Entry<Pair<LiveSize.Pair<Integer, Float>, LiveSize>, View>> it = map.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry<Pair<LiveSize.Pair<Integer, Float>, LiveSize>, View> entry = it.next();
                            if (entry.getValue() == view) {
                                entry.getKey().second.setRelatedLayout(entry.getKey().first, size);
                                it.remove();
                            }
                        }
                    }
                });
            }
            views.clear();
        }

        public void debug(@NonNull View view,
                          @Nullable LayoutSize target,
                          @Nullable LayoutSize original,
                          @Nullable LayoutSize parentSize,
                          int gravity) {
            if (tmpSize != null) {
                Collection<LiveSize> sizes = tmpSize.get();
                if (sizes != null) {
                    for (LiveSize s : sizes) {
                        for (LiveSize.Pair<Integer, Float> pair : s.getRelatedViews().keySet()) {
                            View relatedView = pair.view;
                            if (relatedView == null && pair.viewID != -1) {
                                relatedView = ((View) view.getParent()).findViewById(pair.viewID);
                            }
                            if (relatedView != null) {
                                LayoutSize size = s.getRelatedViews().get(pair);
                                if (size != null) {
                                    InspectUtils.inspect(view, relatedView, size, pair.second.intValue(), false);
                                    if (gravity != Gravity.NO_GRAVITY && gravity == pair.second.intValue())
                                        InspectUtils.inspect(view, relatedView, size, pair.second.intValue(), true);
                                }
                            }
                        }
                    }
                }
            }
            InspectUtils.inspect(view, view, target, Gravity.FILL, true);
            tmpSize = null;
        }
    }
}
