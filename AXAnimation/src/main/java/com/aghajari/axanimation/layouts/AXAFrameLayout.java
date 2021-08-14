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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.aghajari.axanimation.draw.DrawHandler;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.inspect.InspectHandler;
import com.aghajari.axanimation.inspect.InspectLayout;
import com.aghajari.axanimation.inspect.InspectView;
import com.aghajari.axanimation.livevar.LayoutSize;

import java.util.ArrayList;

/**
 * AXAnimatedFrameLayout
 *
 * @author AmirHossein Aghajari
 * @see FrameLayout
 */
public class AXAFrameLayout extends FrameLayout implements AnimatedLayout, InspectLayout, DrawableLayout {

    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;
    private final LayoutSize layoutSize = new LayoutSize();

    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);

    private final InspectHandler inspectHandler = new InspectHandler();
    private final DrawHandler drawHandler = new DrawHandler();

    public AXAFrameLayout(@NonNull Context context) {
        super(context);
    }

    public AXAFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AXAFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AXAFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        drawHandler.draw(this, canvas, false);
        super.dispatchDraw(canvas);
        drawHandler.draw(this, canvas, true);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (shouldSkipMeasure(child)) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                continue;
            }

            if (getMeasureAllChildren() || child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

                if (lp instanceof AnimatedLayoutParams) {
                    maxWidth = Math.max(maxWidth, ((AnimatedLayoutParams) lp).right);
                    maxHeight = Math.max(maxHeight, ((AnimatedLayoutParams) lp).bottom);
                } else {
                    maxWidth = Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                    maxHeight = Math.max(maxHeight,
                            child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                }

                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren && !(lp instanceof AnimatedLayoutParams)) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }
        // Account for padding too
        maxWidth += getPaddingLeftWithForeground_INTERNAL() + getPaddingRightWithForeground_INTERNAL();
        maxHeight += getPaddingTopWithForeground_INTERNAL() + getPaddingBottomWithForeground_INTERNAL();
        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - getPaddingLeftWithForeground_INTERNAL() - getPaddingRightWithForeground_INTERNAL()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeftWithForeground_INTERNAL() + getPaddingRightWithForeground_INTERNAL() +
                                    lp.leftMargin + lp.rightMargin,
                            lp.width);
                }
                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - getPaddingTopWithForeground_INTERNAL() - getPaddingBottomWithForeground_INTERNAL()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTopWithForeground_INTERNAL() + getPaddingBottomWithForeground_INTERNAL() +
                                    lp.topMargin + lp.bottomMargin,
                            lp.height);
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false /* no force left gravity */);
    }

    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();
        final int parentLeft = getPaddingLeftWithForeground_INTERNAL();
        final int parentRight = right - left - getPaddingRightWithForeground_INTERNAL();
        final int parentTop = getPaddingTopWithForeground_INTERNAL();
        final int parentBottom = bottom - top - getPaddingBottomWithForeground_INTERNAL();
        layoutSize.set(parentLeft, parentTop, parentRight, parentBottom);

        final ArrayList<View> skipped = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (shouldSkipMeasure(child)) {
                skipped.add(child);
                continue;
            }

            if (child.getVisibility() != GONE) {
                layoutChild(child, parentLeft, parentRight, parentTop, parentBottom, forceLeftGravity, null, child.getLayoutParams());
            }
        }

        for (View skippedChild : skipped) {
            if (skippedChild == null)
                continue;

            if (skippedChild.getLayoutParams() instanceof AnimatedLayoutParams) {
                AnimatedLayoutParams lp = (AnimatedLayoutParams) skippedChild.getLayoutParams();
                int w = lp.getWidth();
                if (lp.right == -1)
                    w = layoutSize.right - lp.left;
                int h = lp.getHeight();
                if (lp.bottom == -1)
                    h = layoutSize.bottom - lp.top;
                skippedChild.layout(lp.left, lp.top, w + lp.left, h + lp.top);
                skippedChild.invalidate();
            }
        }
    }

    void layoutChild(View child, int parentLeft, int parentRight, int parentTop, int parentBottom, boolean forceLeftGravity, OnLayoutSizeReadyListener listener, ViewGroup.LayoutParams layoutParams) {
        int childLeft = 0;
        int childTop = 0;
        int childRight = 0;
        int childBottom = 0;

        if (layoutParams instanceof AnimatedLayoutParams) {
            final AnimatedLayoutParams alp = (AnimatedLayoutParams) layoutParams;
            childLeft = alp.left;
            childTop = alp.top;
            childRight = alp.right;
            childBottom = alp.bottom;

        } else if (layoutParams instanceof LayoutParams) {
            final LayoutParams lp = (LayoutParams) layoutParams;
            int width, height;
            if (lp.width >= 0) {
                width = lp.width;
            } else {
                width = child.getMeasuredWidth();
            }
            if (lp.height >= 0) {
                height = lp.height;
            } else {
                height = child.getMeasuredHeight();
            }
            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = DEFAULT_CHILD_GRAVITY;
            }
            final int layoutDirection = getLayoutDirection();
            final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    if (!forceLeftGravity) {
                        childLeft = parentRight - width - lp.rightMargin;
                        break;
                    }
                case Gravity.LEFT:
                default:
                    childLeft = parentLeft + lp.leftMargin;
            }
            switch (verticalGravity) {
                case Gravity.CENTER_VERTICAL:
                    childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = parentBottom - height - lp.bottomMargin;
                    break;
                case Gravity.TOP:
                default:
                    childTop = parentTop + lp.topMargin;
            }

            childRight = childLeft + width;
            childBottom = childTop + height;
        }

        if (listener != null) {
            listener.onReady(child, new LayoutSize(childLeft, childTop, childRight, childBottom));
        } else {
            child.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    int getPaddingLeftWithForeground_INTERNAL() {
        return getPaddingLeft();
    }

    int getPaddingRightWithForeground_INTERNAL() {
        return getPaddingRight();
    }

    private int getPaddingTopWithForeground_INTERNAL() {
        return getPaddingTop();
    }

    private int getPaddingBottomWithForeground_INTERNAL() {
        return getPaddingBottom();
    }

    // *************** AnimatedLayout ***************

    protected boolean shouldSkipMeasure(View child) {
        if (child == null)
            return true;

        if (child.getLayoutParams() instanceof AnimatedLayoutParams)
            return ((AnimatedLayoutParams) child.getLayoutParams()).skipMeasure;

        return false;
    }

    @Override
    public void getLayoutSize(final View view, final OnLayoutSizeReadyListener listener) {
        getLayoutSize(view, view.getLayoutParams(), listener);
    }

    @Override
    public void getLayoutSize(final View view, final ViewGroup.LayoutParams layoutParams, final OnLayoutSizeReadyListener listener) {
        if (layoutSize.isEmpty()) {
            post(new Runnable() {
                @Override
                public void run() {
                    getLayoutSize(view, layoutParams, listener);
                }
            });
        } else {
            layoutChild(view, layoutSize.left, layoutSize.right, layoutSize.top, layoutSize.bottom, false, listener, layoutParams);
        }
    }

    @Override
    public LayoutSize getLayoutSize() {
        return layoutSize;
    }

    @Override
    public void getReadyForInspect(boolean enabled) {
        inspectHandler.getReadyForInspect(this, enabled);
    }

    @Override
    @Nullable
    public InspectView getInspectView() {
        return inspectHandler.getInspectView();
    }

    @Override
    public DrawHandler getDrawHandler() {
        return drawHandler;
    }

    @Override
    public boolean canDraw(String key) {
        return true;
    }
}
