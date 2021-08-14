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

import android.os.Handler;
import android.util.Pair;
import android.view.View;

import com.aghajari.axanimation.listener.AXAnimatorListenerAdapter;
import com.aghajari.axanimation.listener.AXAnimatorSetListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to start animation of {@link AXAnimationSet}
 *
 * @author AmirHossein Aghajari
 */
class AXAnimatorSet {
    final List<AXAnimation> animations = new ArrayList<>();
    final ArrayList<AXAnimatorSetListener> listeners = new ArrayList<>();
    boolean paused, running;
    boolean reverse = false;
    AXAnimationSet set;

    public void pause() {
        running = false;
        paused = true;
        for (AXAnimation animation : animations) {
            animation.pause();
        }

        for (AXAnimatorSetListener listener : listeners)
            listener.onAnimationPause(set);
    }

    public void resume() {
        running = true;
        paused = false;
        for (AXAnimation animation : animations) {
            animation.resume();
        }

        for (AXAnimatorSetListener listener : listeners)
            listener.onAnimationResume(set);
    }

    public void cancel() {
        running = false;
        paused = false;
        for (AXAnimation animation : animations) {
            animation.cancel();
        }
        animations.clear();

        for (AXAnimatorSetListener listener : listeners)
            listener.onAnimationCancel(set);
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isRunning() {
        return running;
    }

    public void start(AXAnimationSet animation, boolean reverse) {
        this.set = animation;
        running = true;
        animations.clear();
        this.reverse = reverse;

        for (AXAnimatorSetListener listener : listeners)
            listener.onAnimationStart(set);

        start(0);
    }

    public void start(final int index) {
        if (set.list.size() <= index) {
            done();
            return;
        }

        ArrayList<Pair<View, BaseAnimation>> anims = set.list.get(reverse ? set.list.size() - index - 1 : index);
        lastAnim = null;
        lastDuration = 0;
        startPair(anims, 0, index);
    }

    AXAnimation lastAnim;
    long lastDuration;

    public void startPair(final ArrayList<Pair<View, BaseAnimation>> anims, final int index, final int mainIndex) {
        if (anims.size() <= index) {
            if (lastAnim != null) {
                AXAnimatorListenerAdapter listenerAdapter = new AXAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(AXAnimation animation) {
                        super.onAnimationEnd(animation);
                        animation.removeAnimatorListener(this);
                        start(mainIndex + 1);
                    }
                };
                lastAnim.addAnimatorListener(listenerAdapter);
            } else {
                start(mainIndex + 1);
            }
            return;
        }

        Pair<View, BaseAnimation> anim = anims.get(reverse ? anims.size() - index - 1 : index);
        if (anim.second instanceof Delay) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPair(anims, index + 1, mainIndex);
                }
            }, ((Delay) anim.second).duration);
            return;
        } else if (anim.first != null && anim.second != null) {
            long d = ((AXAnimation) anim.second).getTotalDuration();
            if (lastAnim == null || d >= lastDuration) {
                lastAnim = (AXAnimation) anim.second;
                lastDuration = d;
            }
            AXAnimatorListenerAdapter listenerAdapter = new AXAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(AXAnimation animation) {
                    super.onAnimationEnd(animation);
                    animations.remove(animation);
                }
            };
            AXAnimation animation = ((AXAnimation) anim.second);
            animation.addAnimatorListener(listenerAdapter);
            animations.add(animation);
            animation.start(anim.first, null, reverse, false);
        }
        startPair(anims, index + 1, mainIndex);
    }

    private void done() {
        running = false;
        paused = false;

        for (AXAnimatorSetListener listener : listeners)
            listener.onAnimationEnd(set);
    }

    public long getTotalDuration(AXAnimationSet set) {
        long d = 0;
        for (ArrayList<Pair<View, BaseAnimation>> anims : set.list) {
            long max = 0;
            for (Pair<View, BaseAnimation> pair : anims) {
                if (pair.second instanceof Delay)
                    d += ((Delay) pair.second).duration;
                else
                    max = Math.max(max, ((AXAnimation) pair.second).getTotalDuration());
            }
            d += max;
        }
        return d;
    }

}
