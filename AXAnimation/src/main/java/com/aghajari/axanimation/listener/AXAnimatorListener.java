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

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.rules.RuleSection;

/**
 * <p>An animation listener receives notifications from an animation.
 * Notifications indicate animation related events, such as the end of the animation.</p>
 *
 * @author AmirHossein Aghajari
 */
public interface AXAnimatorListener {
    void onAnimationStart(AXAnimation animation);

    void onAnimationCancel(AXAnimation animation);

    void onAnimationPause(AXAnimation animation);

    void onAnimationResume(AXAnimation animation);

    void onAnimationEnd(AXAnimation animation);

    void onAnimationRepeat(AXAnimation animation);

    void onRuleSectionChanged(AXAnimation animation, RuleSection section);
}
