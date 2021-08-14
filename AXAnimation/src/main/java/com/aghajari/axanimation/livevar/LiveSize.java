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
package com.aghajari.axanimation.livevar;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.utils.SizeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * LiveSize helps you to move view base on it's original size, target size, the parent size
 * Or a related view.
 * <p>
 * {@link com.aghajari.axanimation.AXAnimation#PARENT} is the parent's layout.
 * {@link com.aghajari.axanimation.AXAnimation#ORIGINAL} is the original layout of view before animating.
 * {@link com.aghajari.axanimation.AXAnimation#TARGET} is current layout of view.
 * {@link com.aghajari.axanimation.AXAnimation#CONTENT_WIDTH} = {@link View#getMeasuredWidth()}
 * {@link com.aghajari.axanimation.AXAnimation#CONTENT_HEIGHT} = {@link View#getMeasuredHeight()}
 * {@link com.aghajari.axanimation.AXAnimation#PARENT_WIDTH} = the parent's width
 * {@link com.aghajari.axanimation.AXAnimation#PARENT_HEIGHT} = the parent's height
 * <p>
 * Example: target.left = target.top / 2
 * <code>AXAnimation
 * .create()
 * .duration(800)
 * .toLeft(LiveSize.create(AXAnimation.TARGET|Gravity.TOP).divide(2))
 * .start(View)</code>
 * <p>
 * Example: target.height = (target.measuredWidth * 2) - (10)
 * <code>AXAnimation
 * .create()
 * .duration(800)
 * .resizeHeight(Gravity.CENTER, LiveSize.create(AXAnimation.CONTENT_WIDTH).multiple(2).thenMinus().plus(10))
 * .start(View)</code>
 * <p>
 * Example: target.left = view2.left - view2.width
 * <code>AXAnimation
 * .create()
 * .duration(800)
 * .toLeft(LiveSize.create().plus(R.id.view2,Gravity.LEFT).minus(R.id.view2,Gravity.FILL_HORIZONTAL))
 * .start(View)</code>
 * <p>
 * translate LiveSize by {@link #translate(Context, int)} for a better debug.
 *
 * @author Amir Hossein Aghajari
 */
@SuppressWarnings("unused")
public class LiveSize extends LiveVar<ArrayList<LiveSize.Pair<Integer, ArrayList<LiveSize.Pair<Integer, Float>>>>>
        implements Cloneable, LiveSizeDebugger {

    static final int TYPE_PLUS = 0;
    static final int TYPE_MINUS = 1;
    static final int TYPE_MULTIPLE = 2;
    static final int TYPE_DIVIDE = 3;

    final HashMap<Pair<Integer, Float>, LayoutSize> relatedViews = new HashMap<>();
    private final ArrayList<Pair<Integer, Float>> openListTmp = new ArrayList<>();

    private int openType;

    private LiveSize(int type, float value) {
        super(new ArrayList<Pair<Integer, ArrayList<Pair<Integer, Float>>>>());
        openType = type;
        if (value != 0)
            addValue(type, value);
    }

    @Override
    public void update(Object var) {
        relatedViews.clear();
        value.clear();
        openListTmp.clear();

        if (var == null) {
            return;
        }

        if (var instanceof LiveSize) {
            value.addAll(((LiveSize) var).value);
            relatedViews.putAll(((LiveSize) var).relatedViews);
            openListTmp.addAll(((LiveSize) var).openListTmp);
        } else if (var instanceof Number) {
            openType = TYPE_PLUS;
            addValue(TYPE_PLUS, ((Number) var).floatValue());
            closeCurrentEntry();
        }
    }

    public static LiveSize create(float value) {
        return new LiveSize(TYPE_PLUS, value);
    }

    public static LiveSize create() {
        return new LiveSize(TYPE_PLUS, 0);
    }

    private void closeCurrentEntry() {
        if (!openListTmp.isEmpty())
            value.add(Pair.create(openType, new ArrayList<>(openListTmp)));
        openListTmp.clear();
    }

    private void openNewEntry(int type) {
        closeCurrentEntry();
        openType = type;
    }

    private void addValue(int type, float value) {
        openListTmp.add(Pair.create(type, value));
    }

    private void addValue(int type, float value, int viewID) {
        Pair<Integer, Float> p = Pair.create(type, value, viewID);
        relatedViews.put(p, null);
        openListTmp.add(p);
    }

    private void addValue(int type, float value, View view) {
        Pair<Integer, Float> p = Pair.create(type, value, view);
        relatedViews.put(p, null);
        openListTmp.add(p);
    }

    public LiveSize plus(float value) {
        addValue(TYPE_PLUS, value);
        return this;
    }

    public LiveSize minus(float value) {
        addValue(TYPE_MINUS, value);
        return this;
    }

    public LiveSize multiple(float value) {
        addValue(TYPE_MULTIPLE, value);
        return this;
    }

    public LiveSize divide(float value) {
        addValue(TYPE_DIVIDE, value);
        return this;
    }

    public LiveSize plus(int viewID, int gravity) {
        addValue(TYPE_PLUS, gravity, viewID);
        return this;
    }

    public LiveSize minus(int viewID, float gravity) {
        addValue(TYPE_MINUS, gravity, viewID);
        return this;
    }

    public LiveSize multiple(int viewID, float gravity) {
        addValue(TYPE_MULTIPLE, gravity, viewID);
        return this;
    }

    public LiveSize divide(int viewID, float gravity) {
        addValue(TYPE_DIVIDE, gravity, viewID);
        return this;
    }

    public LiveSize plus(View view, int gravity) {
        addValue(TYPE_PLUS, gravity, view);
        return this;
    }

    public LiveSize minus(View view, float gravity) {
        addValue(TYPE_MINUS, gravity, view);
        return this;
    }

    public LiveSize multiple(View view, float gravity) {
        addValue(TYPE_MULTIPLE, gravity, view);
        return this;
    }

    public LiveSize divide(View view, float gravity) {
        addValue(TYPE_DIVIDE, gravity, view);
        return this;
    }

    public LiveSize thenPlus() {
        openNewEntry(TYPE_PLUS);
        return this;
    }

    public LiveSize thenMinus() {
        openNewEntry(TYPE_MINUS);
        return this;
    }

    public LiveSize thenMultiple() {
        openNewEntry(TYPE_MULTIPLE);
        return this;
    }

    public LiveSize thenDivide() {
        openNewEntry(TYPE_DIVIDE);
        return this;
    }

    public float calculate(int viewWidth, int viewHeight,
                           LayoutSize parent, LayoutSize target, LayoutSize original,
                           int gravity) {
        closeCurrentEntry();
        float res = 0;
        for (Pair<Integer, ArrayList<Pair<Integer, Float>>> p : value) {
            float r = 0;
            for (Pair<Integer, Float> p2 : p.second) {
                float r2 = 1;

                if (relatedViews.containsKey(p2)) {
                    LayoutSize size = relatedViews.get(p2);
                    if (size != null)
                        r2 = size.get(1, p2.second.intValue());
                } else {
                    r2 = SizeUtils.calculate(p2.second, viewWidth, viewHeight,
                            parent, target, original, gravity);
                }

                switch (p2.first) {
                    case TYPE_DIVIDE:
                        r /= r2;
                        break;
                    case TYPE_MULTIPLE:
                        r *= r2;
                        break;
                    case TYPE_MINUS:
                        r -= r2;
                        break;
                    case TYPE_PLUS:
                    default:
                        r += r2;
                        break;
                }
            }
            switch (p.first) {
                case TYPE_DIVIDE:
                    res /= r;
                    break;
                case TYPE_MULTIPLE:
                    res *= r;
                    break;
                case TYPE_MINUS:
                    res -= r;
                    break;
                case TYPE_PLUS:
                default:
                    res += r;
                    break;
            }
        }
        return res;
    }

    /**
     * @hide
     */
    public void measure(boolean supportsLP, float density) {
        for (Pair<Integer, ArrayList<Pair<Integer, Float>>> p : value) {
            for (Pair<Integer, Float> p2 : p.second) {
                p2.second = measure(supportsLP, density, p2.second);
            }
        }
    }

    private float measure(boolean supportLP, float density, final float v) {
        if (SizeUtils.isCustomSize((int) v))
            return v;

        if (supportLP && (v == AXAnimation.MATCH_PARENT || v == AXAnimation.WRAP_CONTENT))
            return v;

        return v * density;
    }

    /**
     * @hide
     */
    public HashMap<Pair<Integer, Float>, LayoutSize> getRelatedViews() {
        return relatedViews;
    }

    /**
     * Translate this LiveSize for a better debug.
     * <p>
     * Example:
     * <code>LiveSize.create(AXAnimation.TARGET|Gravity.TOP)
     * .divide(2)
     * .thenPlus()
     * .plus(R.id.view2,Gravity.LEFT)
     * .translate(context, Gravity.LEFT)</code>
     * <p>
     * Output:
     * target.left = (target.top / 2) + (view2.left)
     *
     * @return Returns The mathematical calculation formula of this LiveSize
     */
    public String translate(Context context, int gravity) {
        closeCurrentEntry();
        return LiveSizeDebugHelper.translate(this, gravity, context);
    }

    /**
     * @hide
     */
    public void setRelatedLayout(Pair<Integer, Float> pair, LayoutSize size) {
        if (size == null)
            relatedViews.remove(pair);

        if (relatedViews.containsKey(pair))
            relatedViews.put(pair, size);
    }

    /**
     * @hide
     */
    public static class Pair<F, S> {
        public final F first;
        public S second;
        public final int viewID;
        public final View view;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
            this.view = null;
            this.viewID = -1;
        }

        public Pair(F first, S second, int viewID) {
            this.first = first;
            this.second = second;
            this.view = null;
            this.viewID = viewID;
        }

        public Pair(F first, S second, View view) {
            this.first = first;
            this.second = second;
            this.view = view;
            this.viewID = -1;
        }

        public static <A, B> Pair<A, B> create(A a, B b) {
            return new Pair<>(a, b);
        }

        public static <A, B> Pair<A, B> create(A a, B b, int viewID) {
            return new Pair<>(a, b, viewID);
        }

        public static <A, B> Pair<A, B> create(A a, B b, View view) {
            return new Pair<>(a, b, view);
        }
    }

    /**
     * @hide
     */
    @Override
    public Map<String, String> debugLiveSize(@NonNull View view) {
        return LiveSizeDebugHelper.debug(this, view, Gravity.NO_GRAVITY);
    }

}
