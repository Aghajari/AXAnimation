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

import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import com.aghajari.axanimation.listener.AXAnimatorSetListener;
import com.aghajari.axanimation.listener.AXAnimatorSetListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Apply {@link AXAnimation} to multi views together
 *
 * @author AmirHossein Aghajari
 */
public class AXAnimationSet implements BaseAnimation {
    final List<ArrayList<Pair<View, BaseAnimation>>> list = new ArrayList<>();
    final ArrayList<Pair<View, BaseAnimation>> tmp = new ArrayList<>();
    final AXAnimatorSet animatorSet = new AXAnimatorSet();

    private AXAnimationSet(BaseAnimation animation, View... views) {
        add(animation, views);
    }

    private void add(BaseAnimation animation, View... views) {
        if (views.length == 0) {
            tmp.add(Pair.create((View) null, animation));
        } else if (views.length == 1) {
            tmp.add(Pair.create(views[0], animation));
        } else if (animation instanceof AXAnimation) {
            for (View v : views) {
                try {
                    tmp.add(Pair.create(v, (BaseAnimation) ((AXAnimation) animation).clone()));
                } catch (Exception e) {
                    tmp.add(Pair.create(v, animation));
                }
            }
        } else {
            for (View v : views)
                tmp.add(Pair.create(v, animation));
        }
    }

    public static AXAnimationSet delay(long duration) {
        return new AXAnimationSet(new Delay(duration));
    }

    public static AXAnimationSet animate(AXAnimation animation, View... views) {
        return new AXAnimationSet(animation, views);
    }

    public static AXAnimationSet animate(String animationName, View... views) {
        return new AXAnimationSet(AXAnimationSaver.get(animationName), views);
    }

    private void closeEntry() {
        if (tmp.size() == 0)
            return;
        list.add(new ArrayList<>(tmp));
        tmp.clear();
    }

    public AXAnimationSet andDelay(long duration) {
        tmp.add(Pair.create((View) null, (BaseAnimation) new Delay(duration)));
        return this;
    }

    public AXAnimationSet thenDelay(long duration) {
        closeEntry();
        return andDelay(duration);
    }

    public AXAnimationSet andAnimate(AXAnimation animation, View... views) {
        add(animation, views);
        return this;
    }

    public AXAnimationSet thenAnimate(AXAnimation animation, View... views) {
        closeEntry();
        return andAnimate(animation, views);
    }

    public AXAnimationSet andAnimate(String animationName, View... views) {
        add((BaseAnimation) AXAnimationSaver.get(animationName), views);
        return this;
    }

    public AXAnimationSet thenAnimate(String animationName, View... views) {
        closeEntry();
        return andAnimate(animationName, views);
    }

    /**
     * Gets the length of the animation. (+ delay)
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getTotalDuration() {
        return animatorSet.getTotalDuration(this);
    }

    public void pause() {
        animatorSet.pause();
    }

    public void resume() {
        animatorSet.resume();
    }

    public void cancel() {
        animatorSet.cancel();
    }

    public boolean isPaused() {
        return animatorSet.isPaused();
    }

    public boolean isRunning() {
        return animatorSet.isRunning();
    }

    public void start() {
        closeEntry();
        animatorSet.start(this, false);
    }

    /**
     * Plays the AXAnimatorSet in reverse.
     * it will start from the end and play backwards.
     *
     * @see AXAnimationSet#start()
     */
    public void reverse() {
        closeEntry();
        animatorSet.start(this, true);
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the life of an
     * animation, such as start, pause, and end.
     *
     * @param listener the listener to be added to the current set of listeners for this animation.
     * @see AXAnimatorSetListener
     * @see AXAnimatorSetListenerAdapter
     */
    public AXAnimationSet addAnimatorListener(@Nullable AXAnimatorSetListener listener) {
        animatorSet.listeners.add(listener);
        return this;
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public AXAnimationSet removeAnimatorListener(@Nullable AXAnimatorSetListener listener) {
        animatorSet.listeners.remove(listener);
        return this;
    }

    /**
     * Gets the set of {@link AXAnimatorSetListener} objects that are currently
     * listening for events on this <code>Animator</code> object.
     *
     * @return ArrayList<AXAnimatorSetListener> The set of listeners.
     */
    public ArrayList<AXAnimatorSetListener> getAnimatorListeners() {
        return animatorSet.listeners;
    }

    /**
     * Removes all listeners from the set listening to this animation.
     */
    public AXAnimationSet clearAnimatorListeners() {
        animatorSet.listeners.clear();
        return this;
    }

}
