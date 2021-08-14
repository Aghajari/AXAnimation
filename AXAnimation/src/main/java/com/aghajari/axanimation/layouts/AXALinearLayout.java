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
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.aghajari.axanimation.draw.DrawHandler;
import com.aghajari.axanimation.draw.DrawableLayout;
import com.aghajari.axanimation.inspect.InspectHandler;
import com.aghajari.axanimation.inspect.InspectLayout;
import com.aghajari.axanimation.inspect.InspectView;
import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * AXAnimatedLinearLayout
 *
 * @author AmirHossein Aghajari
 * @see LinearLayout
 */
public class AXALinearLayout extends LinearLayout implements AnimatedLayout, InspectLayout, DrawableLayout {

    // internal fields
    private int mTotalLength;
    private int mDividerWidth;
    private int mDividerHeight;

    private int[] mMaxAscent;
    private int[] mMaxDescent;

    private static final int VERTICAL_GRAVITY_COUNT = 4;
    private static final int INDEX_CENTER_VERTICAL = 0;
    private static final int INDEX_TOP = 1;
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_FILL = 3;

    private boolean sRemeasureWeightedChildren = true;
    private boolean mAllowInconsistentMeasurement;
    private boolean sUseZeroUnspecifiedMeasureSpec;

    // AnimatedLayout fields
    private final WeakHashMap<View, LayoutSize> viewLayoutSizes = new WeakHashMap<>();
    private final LayoutSize layoutSize = new LayoutSize();
    private final LayoutSize tmpLayoutSize = new LayoutSize();

    private final InspectHandler inspectHandler = new InspectHandler();
    private final DrawHandler drawHandler = new DrawHandler();

    public AXALinearLayout(Context context) {
        this(context, null);
    }

    public AXALinearLayout(Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AXALinearLayout(Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AXALinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        final int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;

        sRemeasureWeightedChildren = targetSdkVersion >= Build.VERSION_CODES.P;
        mAllowInconsistentMeasurement = targetSdkVersion <= Build.VERSION_CODES.M;
        sUseZeroUnspecifiedMeasureSpec = targetSdkVersion < Build.VERSION_CODES.M;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDividerDrawable() == null) {
            return;
        }
        if (getOrientation() == VERTICAL) {
            drawDividersVertical_INTERNAL(canvas);
        } else {
            drawDividersHorizontal_INTERNAL(canvas);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        drawHandler.draw(this, canvas, false);
        super.dispatchDraw(canvas);
        drawHandler.draw(this, canvas, true);
        canvas.restore();
    }

    void drawDividersVertical_INTERNAL(Canvas canvas) {
        final int count = getVirtualChildCount_INTERNAL();
        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child))
                continue;

            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt_INTERNAL(i)) {
                    ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                    final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                    final int top = child.getTop() - lp.topMargin - mDividerHeight;
                    drawHorizontalDivider_INTERNAL(canvas, top);
                }
            }
        }
        if (hasDividerBeforeChildAt_INTERNAL(count)) {
            final View child = getLastNonGoneChild_INTERNAL();
            int bottom;
            if (child == null) {
                bottom = getHeight() - getPaddingBottom() - mDividerHeight;
            } else {
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                bottom = child.getBottom() + lp.bottomMargin;
            }
            drawHorizontalDivider_INTERNAL(canvas, bottom);
        }
    }

    /**
     * Finds the last child that is not gone. The last child will be used as the reference for
     * where the end divider should be drawn.
     */
    private View getLastNonGoneChild_INTERNAL() {
        for (int i = getVirtualChildCount_INTERNAL() - 1; i >= 0; i--) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child))
                continue;

            if (child != null && child.getVisibility() != GONE) {
                return child;
            }
        }
        return null;
    }

    void drawDividersHorizontal_INTERNAL(Canvas canvas) {
        final int count = getVirtualChildCount_INTERNAL();
        final boolean isLayoutRtl = isLayoutRtl_INTERNAL();
        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child))
                continue;

            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt_INTERNAL(i)) {
                    ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                    final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                    final int position;
                    if (isLayoutRtl) {
                        position = child.getRight() + lp.rightMargin;
                    } else {
                        position = child.getLeft() - lp.leftMargin - mDividerWidth;
                    }
                    drawVerticalDivider_INTERNAL(canvas, position);
                }
            }
        }
        if (hasDividerBeforeChildAt_INTERNAL(count)) {
            final View child = getLastNonGoneChild_INTERNAL();
            int position;
            if (child == null) {
                if (isLayoutRtl) {
                    position = getPaddingLeft();
                } else {
                    position = getWidth() - getPaddingRight() - mDividerWidth;
                }
            } else {
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                if (isLayoutRtl) {
                    position = child.getLeft() - lp.leftMargin - mDividerWidth;
                } else {
                    position = child.getRight() + lp.rightMargin;
                }
            }
            drawVerticalDivider_INTERNAL(canvas, position);
        }
    }

    private boolean isLayoutRtl_INTERNAL() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    void drawHorizontalDivider_INTERNAL(Canvas canvas, int top) {
        getDividerDrawable().setBounds(getPaddingLeft() + getDividerPadding(), top,
                getWidth() - getPaddingRight() - getDividerPadding(), top + mDividerHeight);
        getDividerDrawable().draw(canvas);
    }

    void drawVerticalDivider_INTERNAL(Canvas canvas, int left) {
        getDividerDrawable().setBounds(left, getPaddingTop() + getDividerPadding(),
                left + mDividerWidth, getHeight() - getPaddingBottom() - getDividerPadding());
        getDividerDrawable().draw(canvas);
    }

    @Override
    public void setDividerDrawable(Drawable divider) {
        if (divider != null) {
            mDividerWidth = divider.getIntrinsicWidth();
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerWidth = 0;
            mDividerHeight = 0;
        }
        super.setDividerDrawable(divider);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getOrientation() == VERTICAL) {
            measureVertical_INTERNAL(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal_INTERNAL(widthMeasureSpec, heightMeasureSpec);
        }
    }

    void measureVertical_INTERNAL(int widthMeasureSpec, int heightMeasureSpec) {
        mTotalLength = 0;
        int maxWidth = 0;
        int childState = 0;
        int alternativeMaxWidth = 0;
        int weightedMaxWidth = 0;
        boolean allFillParent = true;
        float totalWeight = 0;
        final int count = getVirtualChildCount_INTERNAL();
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchWidth = false;
        boolean skippedMeasure = false;
        final int baselineChildIndex = getBaselineAlignedChildIndex();
        final boolean useLargestChild = isMeasureWithLargestChildEnabled();
        int largestChildHeight = Integer.MIN_VALUE;
        int consumedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        // See how tall everyone is. Also remember max width.
        for (int i = 0; i < count; ++i) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child)) {
                measureChildBeforeLayout_INTERNAL(child, i, widthMeasureSpec, 0,
                        heightMeasureSpec, 0);
                continue;
            }

            if (child == null) {
                mTotalLength += measureNullChild_INTERNAL(i);
                continue;
            }
            if (child.getVisibility() == View.GONE) {
                i += getChildrenSkipCount_INTERNAL(child, i);
                continue;
            }
            nonSkippedChildCount++;
            if (hasDividerBeforeChildAt_INTERNAL(i)) {
                mTotalLength += mDividerHeight;
            }
            ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

            final LayoutParams lp = (LayoutParams) tmpLayoutParams;
            totalWeight += lp.weight;
            final boolean useExcessSpace = lp.height == 0 && lp.weight > 0;
            if (heightMode == MeasureSpec.EXACTLY && useExcessSpace) {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + lp.topMargin + lp.bottomMargin);
                skippedMeasure = true;
            } else {
                if (useExcessSpace) {
                    // The heightMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal height. We'll restore the original height of 0
                    // after measurement.
                    lp.height = LayoutParams.WRAP_CONTENT;
                }
                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedHeight = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout_INTERNAL(child, i, widthMeasureSpec, 0,
                        heightMeasureSpec, usedHeight);
                final int childHeight = child.getMeasuredHeight();
                if (useExcessSpace) {
                    // Restore the original height and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.height = 0;
                    consumedExcessSpace += childHeight;
                }
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + childHeight + lp.topMargin +
                        lp.bottomMargin + getNextLocationOffset_INTERNAL(child));
                if (useLargestChild) {
                    largestChildHeight = Math.max(childHeight, largestChildHeight);
                }
            }
            /*
             * If applicable, compute the additional offset to the child's baseline
             * we'll need later when asked {@link #getBaseline}.
             */
            if ((baselineChildIndex >= 0) && (baselineChildIndex == i + 1)) {
                setBaselineChildTop(mTotalLength);
            }
            // if we are trying to use a child index for our baseline, the above
            // book keeping only works if there are no children above it with
            // weight.  fail fast to aid the developer.
            if (i < baselineChildIndex && lp.weight > 0) {
                throw new RuntimeException("A child of LinearLayout with index "
                        + "less than mBaselineAlignedChildIndex has weight > 0, which "
                        + "won't work.  Either remove the weight, or don't set "
                        + "mBaselineAlignedChildIndex.");
            }
            boolean matchWidthLocally = false;
            if (widthMode != MeasureSpec.EXACTLY && lp.width == LayoutParams.MATCH_PARENT) {
                // The width of the linear layout will scale, and at least one
                // child said it wanted to match our width. Set a flag
                // indicating that we need to remeasure at least that view when
                // we know our width.
                matchWidth = true;
                matchWidthLocally = true;
            }
            final int margin = lp.leftMargin + lp.rightMargin;
            final int measuredWidth = child.getMeasuredWidth() + margin;
            maxWidth = Math.max(maxWidth, measuredWidth);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
            allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
            if (lp.weight > 0) {
                /*
                 * Widths of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxWidth = Math.max(weightedMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
            } else {
                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
            }
            i += getChildrenSkipCount_INTERNAL(child, i);
        }
        if (nonSkippedChildCount > 0 && hasDividerBeforeChildAt_INTERNAL(count)) {
            mTotalLength += mDividerHeight;
        }
        if (useLargestChild &&
                (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED)) {
            mTotalLength = 0;
            for (int i = 0; i < count; ++i) {
                final View child = getVirtualChildAt_INTERNAL(i);
                if (shouldSkipMeasure(child))
                    continue;

                if (child == null) {
                    mTotalLength += measureNullChild_INTERNAL(i);
                    continue;
                }
                if (child.getVisibility() == GONE) {
                    i += getChildrenSkipCount_INTERNAL(child, i);
                    continue;
                }
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                // Account for negative margins
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + largestChildHeight +
                        lp.topMargin + lp.bottomMargin + getNextLocationOffset_INTERNAL(child));
            }
        }
        // Add in our padding
        mTotalLength += getPaddingTop() + getPaddingBottom();
        int heightSize = mTotalLength;
        // Check against our minimum height
        heightSize = Math.max(heightSize, getSuggestedMinimumHeight());
        // Reconcile our calculated size with the heightMeasureSpec
        int heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0);
        heightSize = heightSizeAndState & MEASURED_SIZE_MASK;
        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.

        int remainingExcess = heightSize - mTotalLength
                + (mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
        if (skippedMeasure
                || ((sRemeasureWeightedChildren || remainingExcess != 0) && totalWeight > 0.0f)) {
            float remainingWeightSum = getWeightSum() > 0.0f ? getWeightSum() : totalWeight;
            mTotalLength = 0;
            for (int i = 0; i < count; ++i) {
                final View child = getVirtualChildAt_INTERNAL(i);
                if (shouldSkipMeasure(child))
                    continue;

                if (child == null || child.getVisibility() == View.GONE) {
                    continue;
                }
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                final float childWeight = lp.weight;
                if (childWeight > 0) {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;
                    final int childHeight;
                    if (isMeasureWithLargestChildEnabled() && heightMode != MeasureSpec.EXACTLY) {
                        childHeight = largestChildHeight;
                    } else if (lp.height == 0 && (!mAllowInconsistentMeasurement
                            || heightMode == MeasureSpec.EXACTLY)) {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childHeight = share;
                    } else {
                        // This child had some intrinsic height to which we
                        // need to add its share of excess space.
                        childHeight = child.getMeasuredHeight() + share;
                    }
                    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            Math.max(0, childHeight), MeasureSpec.EXACTLY);
                    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                            lp.width);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    // Child may now not fit in vertical dimension.
                    childState = combineMeasuredStates(childState, child.getMeasuredState()
                            & (MEASURED_STATE_MASK >> MEASURED_HEIGHT_STATE_SHIFT));
                }
                final int margin = lp.leftMargin + lp.rightMargin;
                final int measuredWidth = child.getMeasuredWidth() + margin;
                maxWidth = Math.max(maxWidth, measuredWidth);
                boolean matchWidthLocally = widthMode != MeasureSpec.EXACTLY &&
                        lp.width == LayoutParams.MATCH_PARENT;
                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
                allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredHeight() +
                        lp.topMargin + lp.bottomMargin + getNextLocationOffset_INTERNAL(child));
            }
            // Add in our padding
            mTotalLength += getPaddingTop() + getPaddingBottom();
            // TODO: Should we recompute the heightSpec based on the new total length?
        } else {
            alternativeMaxWidth = Math.max(alternativeMaxWidth,
                    weightedMaxWidth);
            // We have no limit, so make all weighted views as tall as the largest child.
            // Children will have already been measured once.
            if (useLargestChild && heightMode != MeasureSpec.EXACTLY) {
                for (int i = 0; i < count; i++) {
                    final View child = getVirtualChildAt_INTERNAL(i);
                    if (shouldSkipMeasure(child))
                        continue;

                    if (child == null || child.getVisibility() == View.GONE) {
                        continue;
                    }
                    ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                    final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                    float childExtra = lp.weight;
                    if (childExtra > 0) {
                        child.measure(
                                MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                                        MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(Math.max(largestChildHeight, 0),
                                        MeasureSpec.EXACTLY));
                    }
                }
            }
        }
        if (!allFillParent && widthMode != MeasureSpec.EXACTLY) {
            maxWidth = alternativeMaxWidth;
        }
        maxWidth += getPaddingLeft() + getPaddingRight();
        // Check against our minimum width
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                heightSizeAndState);
        if (matchWidth) {
            forceUniformWidth_INTERNAL(count, heightMeasureSpec);
        }
    }

    protected void setBaselineChildTop(int mBaselineChildTop) {
        try {
            ReflectionUtils.setPrivateFieldValueWithThrows(this, "mBaselineChildTop", mBaselineChildTop);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void forceUniformWidth_INTERNAL(int count, int heightMeasureSpec) {
        // Pretend that the linear layout has an exact size.
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
                MeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child))
                continue;

            if (child != null && child.getVisibility() != GONE) {
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                LayoutParams lp = ((LayoutParams) tmpLayoutParams);
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured height
                    // FIXME: this may not be right for something like wrapping text?
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    // Remeasure with new dimensions
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    /**
     * Measures the children when the orientation of this LinearLayout is set
     * to {@link #HORIZONTAL}.
     *
     * @param widthMeasureSpec  Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onMeasure(int, int)
     */
    void measureHorizontal_INTERNAL(int widthMeasureSpec, int heightMeasureSpec) {
        mTotalLength = 0;
        int maxHeight = 0;
        int childState = 0;
        int alternativeMaxHeight = 0;
        int weightedMaxHeight = 0;
        boolean allFillParent = true;
        float totalWeight = 0;
        final int count = getVirtualChildCount_INTERNAL();
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchHeight = false;
        boolean skippedMeasure = false;

        if (mMaxAscent == null || mMaxDescent == null) {
            mMaxAscent = new int[VERTICAL_GRAVITY_COUNT];
            mMaxDescent = new int[VERTICAL_GRAVITY_COUNT];
        }
        final int[] maxAscent = mMaxAscent;
        final int[] maxDescent = mMaxDescent;
        maxAscent[0] = maxAscent[1] = maxAscent[2] = maxAscent[3] = -1;
        maxDescent[0] = maxDescent[1] = maxDescent[2] = maxDescent[3] = -1;
        final boolean baselineAligned = isBaselineAligned();
        final boolean useLargestChild = isMeasureWithLargestChildEnabled();
        final boolean isExactly = widthMode == MeasureSpec.EXACTLY;
        int largestChildWidth = Integer.MIN_VALUE;
        int usedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        // See how wide everyone is. Also remember max height.
        for (int i = 0; i < count; ++i) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child)) {
                measureChildBeforeLayout_INTERNAL(child, i, widthMeasureSpec, 0,
                        heightMeasureSpec, 0);
                continue;
            }

            if (child == null) {
                mTotalLength += measureNullChild_INTERNAL(i);
                continue;
            }
            if (child.getVisibility() == GONE) {
                i += getChildrenSkipCount_INTERNAL(child, i);
                continue;
            }
            nonSkippedChildCount++;
            if (hasDividerBeforeChildAt_INTERNAL(i)) {
                mTotalLength += mDividerWidth;
            }
            ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

            final LayoutParams lp = (LayoutParams) tmpLayoutParams;
            totalWeight += lp.weight;
            final boolean useExcessSpace = lp.width == 0 && lp.weight > 0;
            if (widthMode == MeasureSpec.EXACTLY && useExcessSpace) {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                if (isExactly) {
                    mTotalLength += lp.leftMargin + lp.rightMargin;
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength +
                            lp.leftMargin + lp.rightMargin);
                }
                // Baseline alignment requires to measure widgets to obtain the
                // baseline offset (in particular for TextViews). The following
                // defeats the optimization mentioned above. Allow the child to
                // use as much space as it wants because we can shrink things
                // later (and re-measure).
                if (baselineAligned) {
                    final int freeWidthSpec = makeSafeMeasureSpec(
                            MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.UNSPECIFIED);
                    final int freeHeightSpec = makeSafeMeasureSpec(
                            MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.UNSPECIFIED);
                    child.measure(freeWidthSpec, freeHeightSpec);
                } else {
                    skippedMeasure = true;
                }
            } else {
                if (useExcessSpace) {
                    // The widthMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal width. We'll restore the original width of 0
                    // after measurement.
                    lp.width = LayoutParams.WRAP_CONTENT;
                }
                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedWidth = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout_INTERNAL(child, i, widthMeasureSpec, usedWidth,
                        heightMeasureSpec, 0);
                final int childWidth = child.getMeasuredWidth();
                if (useExcessSpace) {
                    // Restore the original width and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.width = 0;
                    usedExcessSpace += childWidth;
                }
                if (isExactly) {
                    mTotalLength += childWidth + lp.leftMargin + lp.rightMargin
                            + getNextLocationOffset_INTERNAL(child);
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + childWidth + lp.leftMargin
                            + lp.rightMargin + getNextLocationOffset_INTERNAL(child));
                }
                if (useLargestChild) {
                    largestChildWidth = Math.max(childWidth, largestChildWidth);
                }
            }
            boolean matchHeightLocally = false;
            if (heightMode != MeasureSpec.EXACTLY && lp.height == LayoutParams.MATCH_PARENT) {
                // The height of the linear layout will scale, and at least one
                // child said it wanted to match our height. Set a flag indicating that
                // we need to remeasure at least that view when we know our height.
                matchHeight = true;
                matchHeightLocally = true;
            }
            final int margin = lp.topMargin + lp.bottomMargin;
            final int childHeight = child.getMeasuredHeight() + margin;
            childState = combineMeasuredStates(childState, child.getMeasuredState());
            if (baselineAligned) {
                final int childBaseline = child.getBaseline();
                if (childBaseline != -1) {
                    // Translates the child's vertical gravity into an index
                    // in the range 0..VERTICAL_GRAVITY_COUNT

                    final int gravity = (lp.gravity < 0 ? getSafeGravity() : lp.gravity)
                            & Gravity.VERTICAL_GRAVITY_MASK;
                    final int index = ((gravity >> Gravity.AXIS_Y_SHIFT)
                            & ~Gravity.AXIS_SPECIFIED) >> 1;
                    maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                    maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                }
            }
            maxHeight = Math.max(maxHeight, childHeight);
            allFillParent = allFillParent && lp.height == LayoutParams.MATCH_PARENT;
            if (lp.weight > 0) {
                /*
                 * Heights of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxHeight = Math.max(weightedMaxHeight,
                        matchHeightLocally ? margin : childHeight);
            } else {
                alternativeMaxHeight = Math.max(alternativeMaxHeight,
                        matchHeightLocally ? margin : childHeight);
            }
            i += getChildrenSkipCount_INTERNAL(child, i);
        }
        if (nonSkippedChildCount > 0 && hasDividerBeforeChildAt_INTERNAL(count)) {
            mTotalLength += mDividerWidth;
        }
        // Check mMaxAscent[INDEX_TOP] first because it maps to Gravity.TOP,
        // the most common case
        if (maxAscent[INDEX_TOP] != -1 ||
                maxAscent[INDEX_CENTER_VERTICAL] != -1 ||
                maxAscent[INDEX_BOTTOM] != -1 ||
                maxAscent[INDEX_FILL] != -1) {
            final int ascent = Math.max(maxAscent[INDEX_FILL],
                    Math.max(maxAscent[INDEX_CENTER_VERTICAL],
                            Math.max(maxAscent[INDEX_TOP], maxAscent[INDEX_BOTTOM])));
            final int descent = Math.max(maxDescent[INDEX_FILL],
                    Math.max(maxDescent[INDEX_CENTER_VERTICAL],
                            Math.max(maxDescent[INDEX_TOP], maxDescent[INDEX_BOTTOM])));
            maxHeight = Math.max(maxHeight, ascent + descent);
        }
        if (useLargestChild &&
                (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)) {
            mTotalLength = 0;
            for (int i = 0; i < count; ++i) {
                final View child = getVirtualChildAt_INTERNAL(i);
                if (shouldSkipMeasure(child))
                    continue;

                if (child == null) {
                    mTotalLength += measureNullChild_INTERNAL(i);
                    continue;
                }
                if (child.getVisibility() == GONE) {
                    i += getChildrenSkipCount_INTERNAL(child, i);
                    continue;
                }

                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                if (isExactly) {
                    mTotalLength += largestChildWidth + lp.leftMargin + lp.rightMargin +
                            getNextLocationOffset_INTERNAL(child);
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + largestChildWidth +
                            lp.leftMargin + lp.rightMargin + getNextLocationOffset_INTERNAL(child));
                }
            }
        }
        // Add in our padding
        mTotalLength += getPaddingLeft() + getPaddingRight();
        int widthSize = mTotalLength;
        // Check against our minimum width
        widthSize = Math.max(widthSize, getSuggestedMinimumWidth());
        // Reconcile our calculated size with the widthMeasureSpec
        int widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec, 0);
        widthSize = widthSizeAndState & MEASURED_SIZE_MASK;
        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.

        int remainingExcess = widthSize - mTotalLength
                + (mAllowInconsistentMeasurement ? 0 : usedExcessSpace);
        if (skippedMeasure
                || ((sRemeasureWeightedChildren || remainingExcess != 0) && totalWeight > 0.0f)) {
            float remainingWeightSum = getWeightSum() > 0.0f ? getWeightSum() : totalWeight;
            maxAscent[0] = maxAscent[1] = maxAscent[2] = maxAscent[3] = -1;
            maxDescent[0] = maxDescent[1] = maxDescent[2] = maxDescent[3] = -1;
            maxHeight = -1;
            mTotalLength = 0;
            for (int i = 0; i < count; ++i) {
                final View child = getVirtualChildAt_INTERNAL(i);
                if (shouldSkipMeasure(child))
                    continue;

                if (child == null || child.getVisibility() == View.GONE) {
                    continue;
                }
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                final float childWeight = lp.weight;
                if (childWeight > 0) {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;
                    final int childWidth;
                    if (isMeasureWithLargestChildEnabled() && widthMode != MeasureSpec.EXACTLY) {
                        childWidth = largestChildWidth;
                    } else if (lp.width == 0 && (!mAllowInconsistentMeasurement
                            || widthMode == MeasureSpec.EXACTLY)) {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childWidth = share;
                    } else {
                        // This child had some intrinsic width to which we
                        // need to add its share of excess space.
                        childWidth = child.getMeasuredWidth() + share;
                    }
                    final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            Math.max(0, childWidth), MeasureSpec.EXACTLY);
                    final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    // Child may now not fit in horizontal dimension.
                    childState = combineMeasuredStates(childState,
                            child.getMeasuredState() & MEASURED_STATE_MASK);
                }
                if (isExactly) {
                    mTotalLength += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin +
                            getNextLocationOffset_INTERNAL(child);
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredWidth() +
                            lp.leftMargin + lp.rightMargin + getNextLocationOffset_INTERNAL(child));
                }
                boolean matchHeightLocally = heightMode != MeasureSpec.EXACTLY &&
                        lp.height == LayoutParams.MATCH_PARENT;
                final int margin = lp.topMargin + lp.bottomMargin;
                int childHeight = child.getMeasuredHeight() + margin;
                maxHeight = Math.max(maxHeight, childHeight);
                alternativeMaxHeight = Math.max(alternativeMaxHeight,
                        matchHeightLocally ? margin : childHeight);
                allFillParent = allFillParent && lp.height == LayoutParams.MATCH_PARENT;
                if (baselineAligned) {
                    final int childBaseline = child.getBaseline();
                    if (childBaseline != -1) {
                        // Translates the child's vertical gravity into an index in the range 0..2
                        final int gravity = (lp.gravity < 0 ? getSafeGravity() : lp.gravity)
                                & Gravity.VERTICAL_GRAVITY_MASK;
                        final int index = ((gravity >> Gravity.AXIS_Y_SHIFT)
                                & ~Gravity.AXIS_SPECIFIED) >> 1;
                        maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                        maxDescent[index] = Math.max(maxDescent[index],
                                childHeight - childBaseline);
                    }
                }
            }
            // Add in our padding
            mTotalLength += getPaddingLeft() + getPaddingRight();
            // TODO: Should we update widthSize with the new total length?
            // Check mMaxAscent[INDEX_TOP] first because it maps to Gravity.TOP,
            // the most common case
            if (maxAscent[INDEX_TOP] != -1 ||
                    maxAscent[INDEX_CENTER_VERTICAL] != -1 ||
                    maxAscent[INDEX_BOTTOM] != -1 ||
                    maxAscent[INDEX_FILL] != -1) {
                final int ascent = Math.max(maxAscent[INDEX_FILL],
                        Math.max(maxAscent[INDEX_CENTER_VERTICAL],
                                Math.max(maxAscent[INDEX_TOP], maxAscent[INDEX_BOTTOM])));
                final int descent = Math.max(maxDescent[INDEX_FILL],
                        Math.max(maxDescent[INDEX_CENTER_VERTICAL],
                                Math.max(maxDescent[INDEX_TOP], maxDescent[INDEX_BOTTOM])));
                maxHeight = Math.max(maxHeight, ascent + descent);
            }
        } else {
            alternativeMaxHeight = Math.max(alternativeMaxHeight, weightedMaxHeight);
            // We have no limit, so make all weighted views as wide as the largest child.
            // Children will have already been measured once.
            if (useLargestChild && widthMode != MeasureSpec.EXACTLY) {
                for (int i = 0; i < count; i++) {
                    final View child = getVirtualChildAt_INTERNAL(i);
                    if (shouldSkipMeasure(child))
                        continue;

                    if (child == null || child.getVisibility() == View.GONE) {
                        continue;
                    }

                    ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                    final LayoutParams lp = (LayoutParams) tmpLayoutParams;
                    float childExtra = lp.weight;
                    if (childExtra > 0) {
                        child.measure(
                                MeasureSpec.makeMeasureSpec(Math.max(largestChildWidth, 0), MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                                        MeasureSpec.EXACTLY));
                    }
                }
            }
        }
        if (!allFillParent && heightMode != MeasureSpec.EXACTLY) {
            maxHeight = alternativeMaxHeight;
        }
        maxHeight += getPaddingTop() + getPaddingBottom();
        // Check against our minimum height
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        setMeasuredDimension(widthSizeAndState | (childState & MEASURED_STATE_MASK),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        (childState << MEASURED_HEIGHT_STATE_SHIFT)));
        if (matchHeight) {
            forceUniformHeight_INTERNAL(count, widthMeasureSpec);
        }
    }

    private void forceUniformHeight_INTERNAL(int count, int widthMeasureSpec) {
        // Pretend that the linear layout has an exact size. This is the measured height of
        // ourselves. The measured height should be the max height of the children, changed
        // to accommodate the heightMeasureSpec from the parent
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
                MeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child))
                continue;

            if (child != null && child.getVisibility() != GONE) {
                ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);

                LayoutParams lp = (LayoutParams) tmpLayoutParams;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured width
                    // FIXME: this may not be right for something like wrapping text?
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    // Remeasure with new dimensions
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        tmpLayoutSize.set(l, t, r, b);

        if (getOrientation() == VERTICAL) {
            layoutVertical(l, t, r, b, null, null, null);
        } else {
            layoutHorizontal(l, t, r, b, null, null, null);
        }
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to {@link #VERTICAL}.
     *
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onLayout(boolean, int, int, int, int)
     */
    void layoutVertical(int left, int top, int right, int bottom, View targetChild, ViewGroup.LayoutParams targetLayout, OnLayoutSizeReadyListener listener) {
        final int paddingLeft = getPaddingLeft();
        int childTop;
        int childLeft;
        // Where right end of child should go
        final int width = right - left;
        int childRight = width - getPaddingRight();
        // Space available for child
        int childSpace = width - paddingLeft - getPaddingRight();

        final int count = getVirtualChildCount_INTERNAL();
        final int majorGravity = getSafeGravity() & Gravity.VERTICAL_GRAVITY_MASK;
        final int minorGravity = getSafeGravity() & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        switch (majorGravity) {
            case Gravity.BOTTOM:
                // mTotalLength contains the padding already
                childTop = getPaddingTop() + bottom - top - mTotalLength;
                break;
            // mTotalLength contains the padding already
            case Gravity.CENTER_VERTICAL:
                childTop = getPaddingTop() + (bottom - top - mTotalLength) / 2;
                break;
            case Gravity.TOP:
            default:
                childTop = getPaddingTop();
                break;
        }

        final int parentBottom = bottom - top - getPaddingBottom();
        layoutSize.set(paddingLeft, getPaddingTop(), childRight, parentBottom);

        final ArrayList<View> skipped = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child)) {
                skipped.add(child);
                continue;
            }

            if (child == null) {
                childTop += measureNullChild_INTERNAL(i);
            } else if (child.getVisibility() != GONE || child == targetChild) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                final LayoutParams lp;
                if (child == targetChild && targetLayout != null) {
                    lp = (LayoutParams) targetLayout;
                } else {
                    ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);
                    lp = (LayoutParams) tmpLayoutParams;
                }

                int gravity = lp.gravity;
                if (gravity < 0) {
                    gravity = minorGravity;
                }
                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = paddingLeft + ((childSpace - childWidth) / 2)
                                + lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = childRight - childWidth - lp.rightMargin;
                        break;
                    case Gravity.LEFT:
                    default:
                        childLeft = paddingLeft + lp.leftMargin;
                        break;
                }
                if (hasDividerBeforeChildAt_INTERNAL(i)) {
                    childTop += mDividerHeight;
                }
                childTop += lp.topMargin;

                if (listener == null) {
                    setChildFrame_INTERNAL(child, childLeft, childTop + getLocationOffset_INTERNAL(child),
                            childWidth, childHeight);
                } else {
                    if (child == targetChild) {
                        listener.onReady(child, new LayoutSize(childLeft, childTop + getLocationOffset_INTERNAL(child),
                                childLeft + childWidth, childTop + getLocationOffset_INTERNAL(child) + childHeight));
                        break;
                    }
                }
                childTop += childHeight + lp.bottomMargin + getNextLocationOffset_INTERNAL(child);
                i += getChildrenSkipCount_INTERNAL(child, i);
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
                setChildFrame_INTERNAL(skippedChild, lp.left, lp.top, w, h);
                skippedChild.invalidate();
            }
        }
    }


    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to {@link #HORIZONTAL}.
     *
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onLayout(boolean, int, int, int, int)
     */
    void layoutHorizontal(int left, int top, int right, int bottom, View targetChild, ViewGroup.LayoutParams targetLayout, OnLayoutSizeReadyListener listener) {
        final boolean isLayoutRtl = isLayoutRtl_INTERNAL();
        final int paddingTop = getPaddingTop();
        int childTop;
        int childLeft;
        // Where bottom of child should go
        final int height = bottom - top;
        int childBottom = height - getPaddingBottom();
        // Space available for child
        int childSpace = height - paddingTop - getPaddingBottom();
        final int count = getVirtualChildCount_INTERNAL();
        final int majorGravity = getSafeGravity() & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        final int minorGravity = getSafeGravity() & Gravity.VERTICAL_GRAVITY_MASK;
        final boolean baselineAligned = isBaselineAligned();

        final int[] maxAscent = mMaxAscent;
        final int[] maxDescent = mMaxDescent;
        final int layoutDirection = getLayoutDirection();
        switch (Gravity.getAbsoluteGravity(majorGravity, layoutDirection)) {
            case Gravity.RIGHT:
                // mTotalLength contains the padding already
                childLeft = getPaddingLeft() + right - left - mTotalLength;
                break;
            case Gravity.CENTER_HORIZONTAL:
                // mTotalLength contains the padding already
                childLeft = getPaddingLeft() + (right - left - mTotalLength) / 2;
                break;
            case Gravity.LEFT:
            default:
                childLeft = getPaddingLeft();
                break;
        }

        final int parentRight = right - left - getPaddingRight();
        layoutSize.set(getPaddingLeft(), paddingTop, parentRight, childBottom);

        int start = 0;
        int dir = 1;
        //In case of RTL, start drawing from the last child.
        if (isLayoutRtl) {
            start = count - 1;
            dir = -1;
        }

        final ArrayList<View> skipped = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final int childIndex = start + dir * i;
            final View child = getVirtualChildAt_INTERNAL(childIndex);
            if (shouldSkipMeasure(child)) {
                skipped.add(child);
                continue;
            }

            if (child == null) {
                childLeft += measureNullChild_INTERNAL(childIndex);
            } else if (child.getVisibility() != GONE || child == targetChild) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                int childBaseline = -1;

                final LayoutParams lp;
                if (child == targetChild && targetLayout != null) {
                    lp = (LayoutParams) targetLayout;
                } else {
                    ViewGroup.LayoutParams tmpLayoutParams = getSafeLayoutParams(child);
                    lp = (LayoutParams) tmpLayoutParams;
                }

                if (baselineAligned && lp.height != LayoutParams.MATCH_PARENT) {
                    childBaseline = child.getBaseline();
                }
                int gravity = lp.gravity;
                if (gravity < 0) {
                    gravity = minorGravity;
                }
                switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                    case Gravity.TOP:
                        childTop = paddingTop + lp.topMargin;
                        if (childBaseline != -1) {
                            childTop += maxAscent[INDEX_TOP] - childBaseline;
                        }
                        break;
                    case Gravity.CENTER_VERTICAL:
                        // Removed support for baseline alignment when layout_gravity or
                        // gravity == center_vertical. See bug #1038483.
                        // Keep the code around if we need to re-enable this feature
                        // if (childBaseline != -1) {
                        //     // Align baselines vertically only if the child is smaller than us
                        //     if (childSpace - childHeight > 0) {
                        //         childTop = paddingTop + (childSpace / 2) - childBaseline;
                        //     } else {
                        //         childTop = paddingTop + (childSpace - childHeight) / 2;
                        //     }
                        // } else {
                        childTop = paddingTop + ((childSpace - childHeight) / 2)
                                + lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = childBottom - childHeight - lp.bottomMargin;
                        if (childBaseline != -1) {
                            int descent = child.getMeasuredHeight() - childBaseline;
                            childTop -= (maxDescent[INDEX_BOTTOM] - descent);
                        }
                        break;
                    default:
                        childTop = paddingTop;
                        break;
                }
                if (hasDividerBeforeChildAt_INTERNAL(childIndex)) {
                    childLeft += mDividerWidth;
                }
                childLeft += lp.leftMargin;
                if (listener == null) {
                    setChildFrame_INTERNAL(child, childLeft, childTop + getLocationOffset_INTERNAL(child),
                            childWidth, childHeight);
                } else {
                    if (child == targetChild) {
                        listener.onReady(child, new LayoutSize(childLeft, childTop + getLocationOffset_INTERNAL(child),
                                childLeft + childWidth, childTop + getLocationOffset_INTERNAL(child) + childHeight));
                        break;
                    }
                }
                childLeft += childWidth + lp.rightMargin +
                        getNextLocationOffset_INTERNAL(child);
                i += getChildrenSkipCount_INTERNAL(child, childIndex);
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
                setChildFrame_INTERNAL(skippedChild, lp.left, lp.top, w, h);
                skippedChild.invalidate();
            }
        }
    }

    // internal private methods

    int getVirtualChildCount_INTERNAL() {
        return getChildCount();
    }

    View getVirtualChildAt_INTERNAL(int index) {
        return getChildAt(index);
    }

    protected boolean hasDividerBeforeChildAt_INTERNAL(int childIndex) {
        if (childIndex == getVirtualChildCount_INTERNAL()) {
            // Check whether the end divider should draw.
            return (getShowDividers() & SHOW_DIVIDER_END) != 0;
        }
        boolean allViewsAreGoneBefore = allViewsAreGoneBefore_INTERNAL(childIndex);
        if (allViewsAreGoneBefore) {
            // This is the first view that's not gone, check if beginning divider is enabled.
            return (getShowDividers() & SHOW_DIVIDER_BEGINNING) != 0;
        } else {
            return (getShowDividers() & SHOW_DIVIDER_MIDDLE) != 0;
        }
    }

    private boolean allViewsAreGoneBefore_INTERNAL(int childIndex) {
        for (int i = childIndex - 1; i >= 0; i--) {
            final View child = getVirtualChildAt_INTERNAL(i);
            if (shouldSkipMeasure(child))
                continue;

            if (child != null && child.getVisibility() != GONE) {
                return false;
            }
        }
        return true;
    }

    int measureNullChild_INTERNAL(int childIndex) {
        return 0;
    }

    int getChildrenSkipCount_INTERNAL(View child, int index) {
        return 0;
    }

    int getNextLocationOffset_INTERNAL(View child) {
        return 0;
    }

    int getLocationOffset_INTERNAL(View child) {
        return 0;
    }

    void measureChildBeforeLayout_INTERNAL(View child, int childIndex,
                                           int widthMeasureSpec, int totalWidth, int heightMeasureSpec,
                                           int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth,
                heightMeasureSpec, totalHeight);
    }

    /**
     * Like {@link MeasureSpec#makeMeasureSpec(int, int)}, but any spec with a mode of UNSPECIFIED
     * will automatically get a size of 0. Older apps expect this.
     *
     * @hide internal use only for compatibility with system widgets and older apps
     * Reference: MeasureSpec#makeSafeMeasureSpec(int, int)
     */
    protected int makeSafeMeasureSpec(int size, int mode) {
        if (sUseZeroUnspecifiedMeasureSpec && mode == MeasureSpec.UNSPECIFIED) {
            return 0;
        }
        return MeasureSpec.makeMeasureSpec(size, mode);
    }

    protected int getSafeGravity() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return getGravity();
        } else {
            return ReflectionUtils.getPrivateFieldValue(this, "mGravity",
                    Gravity.START | Gravity.TOP);
        }
    }

    // *************** AnimatedLayout ***************

    protected boolean shouldSkipMeasure(View child) {
        if (child == null)
            return true;

        if (child.getLayoutParams() instanceof AnimatedLayoutParams)
            return ((AnimatedLayoutParams) child.getLayoutParams()).skipMeasure;

        return false;
    }

    protected ViewGroup.LayoutParams getSafeLayoutParams(View child) {
        if (child.getLayoutParams() instanceof AnimatedLayoutParams) {
            return ((AnimatedLayoutParams) child.getLayoutParams()).original;
        } else {
            return child.getLayoutParams();
        }
    }

    protected ViewGroup.LayoutParams getRealLayoutParams(View child) {
        return child.getLayoutParams();
    }

    protected void setChildFrame_INTERNAL(View child, int left, int top, int width, int height) {
        ViewGroup.LayoutParams lp = getRealLayoutParams(child);
        if (lp instanceof AnimatedLayoutParams) {
            AnimatedLayoutParams alp = (AnimatedLayoutParams) lp;
            child.layout(alp.left, alp.top, alp.right, alp.bottom);
        } else {
            child.layout(left, top, left + width, top + height);
        }

        if (viewLayoutSizes.containsKey(child)) {
            LayoutSize layoutSize = viewLayoutSizes.get(child);
            if (layoutSize != null) {
                layoutSize.set(left, top, left + width, top + height);
            } else {
                layoutSize = new LayoutSize(left, top, left + width, top + height);
            }
            viewLayoutSizes.put(child, layoutSize);
        } else {
            viewLayoutSizes.put(child, new LayoutSize(left, top, left + width, top + height));
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        viewLayoutSizes.remove(child);
    }

    @Override
    public void getLayoutSize(final View view, final OnLayoutSizeReadyListener listener) {
        getLayoutSize(view, null, listener);
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
            if (layoutParams == null) {
                if (viewLayoutSizes.containsKey(view)) {
                    listener.onReady(view, viewLayoutSizes.get(view));
                } else {
                    getLayoutSize(view, view.getLayoutParams(), listener);
                }
            } else {
                if (getOrientation() == VERTICAL) {
                    layoutVertical(tmpLayoutSize.left, tmpLayoutSize.top, tmpLayoutSize.right, tmpLayoutSize.bottom, view, layoutParams, listener);
                } else {
                    layoutHorizontal(tmpLayoutSize.left, tmpLayoutSize.top, tmpLayoutSize.right, tmpLayoutSize.bottom, view, layoutParams, listener);
                }
            }
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

    @Nullable
    @Override
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