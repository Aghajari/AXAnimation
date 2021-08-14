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
package com.aghajari.axanimation.prerule;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.draw.CanvasView;
import com.aghajari.axanimation.layouts.AnimatedLayoutParams;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.listener.AXAnimatorListener;
import com.aghajari.axanimation.listener.AXAnimatorListenerAdapter;

/**
 * A {@link PreRule} to make a Placeholder of targetView.
 *
 * @author AmirHossein Aghajari
 */
public class PreRuleCopyView implements PreRule {

    protected final boolean focusOnCopyView;
    protected final boolean removeCopyViewAtTheEnd;
    protected final AXAnimation placeholderAnimation;

    public PreRuleCopyView(boolean focusOnCopyView, boolean removeCopyViewAtTheEnd, AXAnimation placeholderAnimation) {
        this.focusOnCopyView = focusOnCopyView;
        this.removeCopyViewAtTheEnd = removeCopyViewAtTheEnd;
        this.placeholderAnimation = placeholderAnimation;
    }

    @Override
    public Pair<View, LayoutSize> apply(AXAnimation animation, Pair<View, LayoutSize> org) {
        View view = org.first;

        final ViewGroup parent = (ViewGroup) view.getParent();
        int index = parent.indexOfChild(view);
        ViewGroup.LayoutParams lp = getOriginalLayoutParams(animation);

        final View copyView = createPlaceholder(view);
        parent.addView(copyView, getChildIndex(index));

        ViewGroup.LayoutParams targetLayoutParams = getTargetLayoutParams(org, lp);
        if (targetLayoutParams != null)
            copyView.setLayoutParams(targetLayoutParams);

        if (removeCopyViewAtTheEnd) {
            addEndAction(animation, new AXAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(AXAnimation animation) {
                    super.onAnimationEnd(animation);
                    if (copyView.getParent() != null)
                        parent.removeView(copyView);

                    animation.removeAnimatorListener(this);
                }
            });
        }

        if (focusOnCopyView) {
            startPlaceholderAnimation(view);
            return Pair.create(copyView, org.second);
        }
        startPlaceholderAnimation(copyView);
        return org;
    }

    @Override
    public boolean isLayoutSizeNecessary() {
        return true;
    }

    // Make it possible to create a subclass of this PreRule!

    /**
     * Starts an animation for the placeholder (if exists)
     *
     * @param view copyView if {@link #focusOnCopyView} is false,
     *             targetView if {@link #focusOnCopyView} is true.
     */
    protected void startPlaceholderAnimation(View view) {
        if (placeholderAnimation != null)
            placeholderAnimation.start(view);
    }

    /**
     * @param action A listener to remove copyView at the end
     */
    protected void addEndAction(AXAnimation animation, AXAnimatorListener action) {
        if (removeCopyViewAtTheEnd)
            animation.addAnimatorListener(action);
    }

    /**
     * @return the layoutParams of copyView
     */
    protected ViewGroup.LayoutParams getTargetLayoutParams(Pair<View, LayoutSize> org, ViewGroup.LayoutParams original) {
        if (org.second != null && !org.second.isEmpty()) {
            AnimatedLayoutParams lp = new AnimatedLayoutParams(original, org.second);
            lp.skipMeasure = true;
            return lp;
        } else {
            return original;
        }
    }

    /**
     * @return the first layoutParams of copyView
     */
    protected ViewGroup.LayoutParams getOriginalLayoutParams(AXAnimation animation) {
        return animation.getOriginalLayoutParams();
    }

    /**
     * @return the position at which to add the copyView
     */
    protected int getChildIndex(int realIndex) {
        return focusOnCopyView ? realIndex + 1 : realIndex;
    }

    /**
     * @return a placeholder (copyView) for the targetView
     */
    protected View createPlaceholder(View target) {
        return new PlaceholderView(target);
    }

    /**
     * PlaceholderView creates a bitmap from targetView and draw it for next times.
     */
    private static class PlaceholderView extends CanvasView {

        final View target;
        Bitmap bitmap;

        private PlaceholderView(View target) {
            super(target.getContext());
            this.target = target;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            innerMeasure();
        }

        private void innerMeasure() {
            int w = target.getMeasuredWidth();
            int h = target.getMeasuredHeight();

            if (w <= 0 && h <= 0) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        innerMeasure();
                    }
                });
            } else {
                setMeasuredDimension(w, h);
                requestLayout();
            }
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            if (bitmap == null) {
                target.draw(canvas);
                int w = target.getMeasuredWidth();
                int h = target.getMeasuredHeight();

                if (w > 0 && h > 0) {
                    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas tmpCanvas = new Canvas(bitmap);
                    target.draw(tmpCanvas);
                    invalidate();
                }
            } else {
                canvas.drawBitmap(bitmap, 0, 0, null);
            }
        }

    }

}
