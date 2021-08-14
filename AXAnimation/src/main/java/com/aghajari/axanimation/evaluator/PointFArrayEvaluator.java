package com.aghajari.axanimation.evaluator;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * This evaluator can be used to perform type interpolation between <code>PointF[]</code> values.
 */
public class PointFArrayEvaluator implements TypeEvaluator<PointF[]> {

    private PointF[] mArray;

    /**
     * Create a FloatArrayEvaluator that does not reuse the animated value. Care must be taken
     * when using this option because on every evaluation a new <code>PointF[]</code> will be
     * allocated.
     *
     * @see #PointFArrayEvaluator(PointF[])
     */
    public PointFArrayEvaluator() {
    }

    /**
     * Create a FloatArrayEvaluator that reuses <code>reuseArray</code> for every evaluate() call.
     * Caution must be taken to ensure that the value returned from
     * {@link android.animation.ValueAnimator#getAnimatedValue()} is not cached, modified, or
     * used across threads. The value will be modified on each <code>evaluate()</code> call.
     *
     * @param reuseArray The array to modify and return from <code>evaluate</code>.
     */
    public PointFArrayEvaluator(PointF[] reuseArray) {
        mArray = reuseArray;
    }

    /**
     * Interpolates the value at each index by the fraction. If
     * {@link #PointFArrayEvaluator(PointF[])} was used to construct this object,
     * <code>reuseArray</code> will be returned, otherwise a new <code>PointF[]</code>
     * will be returned.
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue The start value.
     * @param endValue   The end value.
     * @return A <code>PointF[]</code> where each element is an interpolation between
     * the same index in startValue and endValue.
     */
    @Override
    public PointF[] evaluate(float fraction, PointF[] startValue, PointF[] endValue) {
        PointF[] array = mArray;
        if (array == null) {
            array = new PointF[startValue.length];
            for (int i = 0; i < startValue.length; i++)
                array[i] = new PointF();
        }

        for (int i = 0; i < array.length; i++) {
            PointF start = startValue[i];
            PointF end = endValue[i];
            int x = (int) (start.x + (fraction * (end.x - start.x)));
            int y = (int) (start.y + (fraction * (end.y - start.y)));
            array[i].set(x, y);
        }
        return array;
    }
}