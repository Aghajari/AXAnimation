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
package com.aghajari.axanimation.rules;

import android.view.View;

/**
 * Will stop Animator a while before starting the next section.
 * Whenever {@link #isDone(View)} returned true next section will start.
 *
 * @author AmirHossein Aghajari
 */
public class WaitNotifyRule extends WaitRule {

    protected final Listener listener;

    public WaitNotifyRule(Listener listener) {
        super(0);
        this.listener = listener;
    }

    public WaitNotifyRule(long duration, Listener listener) {
        super(duration);
        this.listener = listener;
    }

    public interface Listener {
        boolean isDone(View view);
    }

    public boolean isDone(View view) {
        if (listener == null)
            return true;
        return listener.isDone(view);
    }
}
