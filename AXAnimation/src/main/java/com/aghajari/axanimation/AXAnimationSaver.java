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
package com.aghajari.axanimation;

import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * A helper class to save {@link AXAnimation}s.
 *
 * @author AmirHossein Aghajari
 */
class AXAnimationSaver {

    static HashMap<String, AXAnimation> animations;

    public static void save(AXAnimation animation, String name) {
        if (animations == null)
            animations = new HashMap<>();
        animations.put(name, animation);
    }

    public static AXAnimation get(String name) {
        if (animations == null || !animations.containsKey(name))
            throw new RuntimeException("Animation (" + name + ") doesn't exist!");
        return animations.get(name);
    }

    static WeakHashMap<View, ArrayList<AXAnimation>> runningAnimations;

    public static void run(View view, AXAnimation animation) {
        if (runningAnimations == null)
            runningAnimations = new WeakHashMap<>();
        if (!runningAnimations.containsKey(view)) {
            runningAnimations.put(view, new ArrayList<>(Collections.singleton(animation)));
        } else {
            ArrayList<AXAnimation> a = runningAnimations.get(view);
            if (a != null && !a.contains(animation))
                a.add(animation);
        }
    }

    public static ArrayList<AXAnimation> get(View view) {
        if (runningAnimations == null)
            return null;
        return runningAnimations.get(view);
    }

    public static void clear(View view, AXAnimation animation) {
        if (runningAnimations == null || !runningAnimations.containsKey(view))
            return;
        ArrayList<AXAnimation> a = runningAnimations.get(view);
        if (a != null)
            a.remove(animation);
    }

    public static void clear(View view) {
        if (runningAnimations == null)
            return;
        runningAnimations.remove(view);
    }

}
