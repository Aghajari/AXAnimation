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
package com.aghajari.axanimation.listener;

import com.aghajari.axanimation.AXAnimationSet;

/**
 * This adapter class provides empty implementations of the methods from {@link AXAnimatorSetListener}.
 * Any custom listener that cares only about a subset of the methods of this listener can
 * simply subclass this adapter class instead of implementing the interface directly.
 *
 * @author AmirHossein Aghajari
 */
public abstract class AXAnimatorSetListenerAdapter implements AXAnimatorSetListener {

    @Override
    public void onAnimationStart(AXAnimationSet animation) {

    }

    @Override
    public void onAnimationCancel(AXAnimationSet animation) {

    }

    @Override
    public void onAnimationPause(AXAnimationSet animation) {

    }

    @Override
    public void onAnimationResume(AXAnimationSet animation) {

    }

    @Override
    public void onAnimationEnd(AXAnimationSet animation) {

    }
}