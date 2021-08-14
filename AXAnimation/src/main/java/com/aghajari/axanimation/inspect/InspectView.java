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
package com.aghajari.axanimation.inspect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.aghajari.axanimation.evaluator.DrawableEvaluator;
import com.aghajari.axanimation.livevar.LayoutSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Inspect animated view & related views for a better debug.
 *
 * @author AmirHossein Aghajari
 * @hide
 */
public class InspectView extends View {

    final HashMap<View, Element> elements = new HashMap<>();

    private final int defaultColor;

    Paint selectedPaint;
    Paint helperLinePaint;
    TextPaint textPaint;
    Paint textBackgroundPaint;

    public InspectView(Context context) {
        super(context);
        defaultColor = getThemeColor(context);

        float strokeSize = dp(0.5f);

        selectedPaint = new Paint();
        selectedPaint.setColor(defaultColor);
        selectedPaint.setStrokeWidth(strokeSize * 4);

        helperLinePaint = new Paint();
        helperLinePaint.setColor(defaultColor);
        helperLinePaint.setStrokeWidth(strokeSize);
        helperLinePaint.setStyle(Paint.Style.STROKE);
        helperLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(defaultColor);
        textBackgroundPaint.setStrokeWidth(strokeSize * 4);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14 * 2 * strokeSize);

        selectedPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setStyle(Paint.Style.FILL);

        ViewCompat.setTranslationZ(this, 10000); // bringViewToFront always

        // Double tap to clear inspection.
        setOnClickListener(new OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300;
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    clearInspect();
                    lastClickTime = 0;
                }
                lastClickTime = clickTime;
            }
        });
    }

    protected int getThemeColor(Context context) {
        try {
            int colorAttr;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorAttr = android.R.attr.colorAccent;
            } else {
                colorAttr = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
            }
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(colorAttr, outValue, true);
            int c = outValue.data;
            return c == Color.TRANSPARENT ? Color.RED : c;
        } catch (Exception e) {
            return Color.RED;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getRight();
        int h = getBottom();

        for (View view : elements.keySet()) {
            Element element = elements.get(view);
            if (element == null)
                continue;

            int color = DrawableEvaluator.getColor(view.getBackground(), Color.TRANSPARENT);
            if (color == Color.TRANSPARENT && view instanceof TextView)
                color = ((TextView) view).getCurrentTextColor();
            if (color == Color.TRANSPARENT)
                color = defaultColor;

            helperLinePaint.setColor(color);
            selectedPaint.setColor(color);
            textBackgroundPaint.setColor(color);

            for (LineElement line : element.lineElements) {
                Point start = line.getStartPoint();
                Point end = line.getEndPoint();

                if (start.equals(end))
                    continue;

                canvas.drawCircle(start.x, start.y, dp(6), selectedPaint);
                canvas.drawCircle(end.x, end.y, dp(2), selectedPaint);

                drawArrow(selectedPaint, canvas, start.x, start.y, end.x, end.y);
                CharSequence text;
                if (line.isArea()) {
                    text = read(Math.abs(end.x - start.x), Math.abs(end.y - start.y));
                } else {
                    text = read((float) Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2)));
                }
                drawText(text, start.x + (end.x - start.x) / 2f, start.y + (end.y - start.y) / 2f, textPaint, textBackgroundPaint, w, h, canvas);
            }

            LayoutSize size = element.layoutSize;
            if (size == null || element.gravity.isEmpty())
                continue;

            if (hasVerticalGravity(element, Gravity.TOP, false)) {
                canvas.drawLine(0, size.top, w, size.top, helperLinePaint);
            }
            if (hasVerticalGravity(element, Gravity.TOP, true)) {
                drawArrow(selectedPaint, canvas, size.getCenterX(), size.top, size.getCenterX(), 0);
                drawText(read(size.top), size.getCenterX(), size.top / 2f, textPaint, textBackgroundPaint, w, h, canvas);
            }

            if (hasVerticalGravity(element, Gravity.BOTTOM, false)) {
                canvas.drawLine(0, size.bottom, w, size.bottom, helperLinePaint);
            }
            if (hasVerticalGravity(element, Gravity.BOTTOM, true)) {
                drawArrow(selectedPaint, canvas, size.getCenterX(), size.bottom, size.getCenterX(), h);
                drawText(read(h - size.bottom), size.getCenterX(), size.bottom + (h - size.bottom) / 2f, textPaint, textBackgroundPaint, w, h, canvas);
            }

            if (hasHorizontalGravity(element, Gravity.LEFT, false)) {
                canvas.drawLine(size.left, 0, size.left, h, helperLinePaint);
            }
            if (hasHorizontalGravity(element, Gravity.LEFT, true)) {
                drawArrow(selectedPaint, canvas, size.left, size.getCenterY(), 0, size.getCenterY());
                drawText(read(size.left), size.left / 2f, size.getCenterY(), textPaint, textBackgroundPaint, w, h, canvas);
            }

            if (hasHorizontalGravity(element, Gravity.RIGHT, false)) {
                canvas.drawLine(size.right, 0, size.right, h, helperLinePaint);
            }
            if (hasHorizontalGravity(element, Gravity.RIGHT, true)) {
                drawArrow(selectedPaint, canvas, size.right, size.getCenterY(), w, size.getCenterY());
                drawText(read(w - size.right), size.right + (w - size.right) / 2f, size.getCenterY(), textPaint, textBackgroundPaint, w, h, canvas);
            }
            if (hasVerticalGravity(element, Gravity.CENTER_VERTICAL, false)) {
                canvas.drawLine(0, size.getCenterY(), w, size.getCenterY(), helperLinePaint);
            }
            if (hasHorizontalGravity(element, Gravity.CENTER_HORIZONTAL, false)) {
                canvas.drawLine(size.getCenterX(), 0, size.getCenterX(), h, helperLinePaint);
            }

            if (hasGravity(element, Gravity.FILL, Gravity.FILL_VERTICAL, Gravity.FILL_HORIZONTAL)) {
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                int min = Math.min(Math.min(r, g), b);
                int d = -Math.min(min, 20);
                if (d == 0) {
                    int max = Math.max(Math.max(r, g), b);
                    d = Math.min(255 - max, 20);
                }
                textBackgroundPaint.setColor(Color.argb(190, r + d, g + d, b + d));
                drawText(read(size.getWidth(), size.getHeight()), size.getCenterX(), size.getCenterY(), textPaint, textBackgroundPaint, canvas);
            }
        }
    }

    private boolean hasGravity(Element element, Integer... target) {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(target));
        for (Pair<Integer, Boolean> g : element.gravity) {
            if (list.contains(g.first))
                return true;
        }
        return false;
    }

    private boolean hasHorizontalGravity(Element element, int gr, boolean target) {
        for (Pair<Integer, Boolean> g : element.gravity) {
            if (g.second == target && (g.first & Gravity.HORIZONTAL_GRAVITY_MASK) == gr)
                return true;
        }
        return false;
    }

    private boolean hasVerticalGravity(Element element, int gr, boolean target) {
        for (Pair<Integer, Boolean> g : element.gravity) {
            if (g.second == target && (g.first & Gravity.VERTICAL_GRAVITY_MASK) == gr)
                return true;
        }
        return false;
    }

    private CharSequence read(float w, float h) {
        float d = dp(1);
        String w1 = String.valueOf(round(w / d));
        String h1 = String.valueOf(round(h / d));
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(w1);
        builder.setSpan(new ForegroundColorSpan(Color.WHITE), 0, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("x");
        builder.setSpan(new ForegroundColorSpan(Color.LTGRAY), builder.length() - 1, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(h1);
        builder.setSpan(new ForegroundColorSpan(Color.WHITE), builder.length() - h1.length(), builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("dp");
        builder.setSpan(new ForegroundColorSpan(Color.LTGRAY), builder.length() - 2, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private CharSequence read(float size) {
        String s = String.valueOf(round(size / dp(1)));
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(s);
        builder.setSpan(new ForegroundColorSpan(Color.WHITE), 0, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("dp");
        builder.setSpan(new ForegroundColorSpan(Color.LTGRAY), builder.length() - 2, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * com.android.internal.util.FastMath.round(float)
     */
    private static int round(float value) {
        long lx = (long) (value * (65536 * 256f));
        return (int) ((lx + 0x800000) >> 24);
    }

    private void drawText(CharSequence text, float x, float y, TextPaint paint, Paint backgroundPaint, int maxWidth, int maxHeight, Canvas canvas) {
        if (x <= 0 || y <= 0) return;

        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);

        float padding = dp(4);
        float left = rect.width() / 2f;
        float top = rect.height() / 2f;

        if (x - left - padding <= 0 || y - top - padding <= 0) return;
        if (x + left + padding >= maxWidth || y + top + padding >= maxHeight) return;

        canvas.drawPath(getRoundedRect(x - left - padding, y - top - padding, x + left + padding, y + top + padding, dp(8f), dp(8f), false), backgroundPaint);

        canvas.save();
        canvas.translate(x - left, y - top * 1.25f);
        StaticLayout layout = new StaticLayout(text, paint,
                canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawText(CharSequence text, float x, float y, TextPaint paint, Paint backgroundPaint, Canvas canvas) {
        Rect rect = new Rect();
        paint.getTextBounds(text.toString(), 0, text.length(), rect);

        float left = rect.width() / 2f;
        float top = rect.height() / 2f;

        if (backgroundPaint != null) {
            float padding = dp(4);
            canvas.drawPath(getRoundedRect(x - left - padding, y - top - padding, x + left + padding, y + top + padding, dp(8f), dp(8f), false), backgroundPaint);
        }

        canvas.save();
        canvas.translate(x - left, y - top * 1.25f);
        StaticLayout layout = new StaticLayout(text, paint,
                canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        layout.draw(canvas);
        canvas.restore();
        //canvas.drawText(text, 0, text.length(), x - left, y + top / 1.5f, paint);
    }

    private Path getRoundedRect(float left, float top, float right, float bottom, float rx, float ry, boolean conformToOriginalPost) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);

        if (conformToOriginalPost) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        } else {
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last line to can be removed.

        return path;
    }

    /**
     * Draw an arrow
     * change internal radius and angle to change appearance
     * - angle : angle in degrees of the arrows legs
     * - radius : length of the arrows legs
     *
     * @author Steven Roelants 2017
     */
    private void drawArrow(Paint paint, Canvas canvas, float from_x, float from_y, float to_x, float to_y) {
        drawArrow(paint, canvas, from_x, from_y, to_x, to_y, 60, 30);
    }

    private void drawArrow(Paint paint, Canvas canvas, float from_x, float from_y, float to_x, float to_y, float angle, float radius) {
        float min = dp(4);
        if ((Math.abs(from_x - to_x) < min && from_x != to_x) || (Math.abs(from_y - to_y) < min && from_y != to_y))
            return;
        if (from_x == to_x && from_y == to_y)
            return;

        float anglerad, lineangle;

        //some angle calculations
        anglerad = (float) (Math.PI * angle / 180.0f);
        lineangle = (float) (Math.atan2(to_y - from_y, to_x - from_x));

        //tha line
        canvas.drawLine(from_x, from_y, to_x, to_y, paint);

        //tha triangle
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(to_x, to_y);
        path.lineTo((float) (to_x - radius * Math.cos(lineangle - (anglerad / 2.0))),
                (float) (to_y - radius * Math.sin(lineangle - (anglerad / 2.0))));
        path.lineTo((float) (to_x - radius * Math.cos(lineangle + (anglerad / 2.0))),
                (float) (to_y - radius * Math.sin(lineangle + (anglerad / 2.0))));
        path.close();

        canvas.drawPath(path, paint);
    }

    private float dp(float value) {
        return value * getContext().getResources().getDisplayMetrics().density;
    }

    public void clearInspect() {
        elements.clear();
        invalidate();
    }

    public void inspect(View view, LayoutSize size, int gravity, boolean target) {
        Pair<Integer, Boolean> p = Pair.create(gravity, target);

        if (elements.containsKey(view)) {
            Element e = elements.get(view);
            if (e == null) {
                elements.put(view, new Element(size, p));
                return;
            }
            if (!e.gravity.contains(p)) {
                if (size != null && !size.isEmpty())
                    e.layoutSize = size;
                e.gravity.add(p);
            }
        } else {
            elements.put(view, new Element(size, p));
        }
    }

    public void inspect(View view, LayoutSize start, LayoutSize end, int gravityStart, int gravityEnd, Point delta) {
        LineElement l = new LineElement(start, end, gravityStart, gravityEnd, delta);
        if (elements.containsKey(view)) {
            Element e = elements.get(view);
            if (e == null) {
                elements.put(view, new Element(l));
                return;
            }
            e.lineElements.add(l);
        } else {
            elements.put(view, new Element(l));
        }
    }

    public void inspect(View view, LayoutSize start, int gravityStart, final Point end, final boolean reverse, final boolean horizontal) {
        LineElement l = new LineElement(start, null, gravityStart, Gravity.NO_GRAVITY, null) {
            @Override
            public Point getEndPoint() {
                if (reverse)
                    return super.getStartPoint();

                if (horizontal)
                    end.y = super.getStartPoint().y;
                else
                    end.x = super.getStartPoint().x;

                return end;
            }

            @Override
            public Point getStartPoint() {
                if (reverse) {
                    if (horizontal)
                        end.y = super.getStartPoint().y;
                    else
                        end.x = super.getStartPoint().x;

                    return end;
                }
                return super.getStartPoint();
            }

            @Override
            public boolean isArea() {
                return false;
            }
        };

        if (elements.containsKey(view)) {
            Element e = elements.get(view);
            if (e == null) {
                elements.put(view, new Element(l));
                return;
            }
            e.lineElements.add(l);
        } else {
            elements.put(view, new Element(l));
        }
    }

    public HashMap<View, Element> getElements() {
        return elements;
    }

    private static class Element {
        LayoutSize layoutSize;
        final ArrayList<Pair<Integer, Boolean>> gravity;
        final ArrayList<LineElement> lineElements;

        @SafeVarargs
        Element(LayoutSize layoutSize, Pair<Integer, Boolean>... gravity) {
            this.layoutSize = layoutSize;
            this.gravity = new ArrayList<>(Arrays.asList(gravity));
            this.lineElements = new ArrayList<>();
        }

        Element(LineElement... lineElements) {
            layoutSize = null;
            gravity = new ArrayList<>();
            this.lineElements = new ArrayList<>(Arrays.asList(lineElements));
        }

        @NonNull
        @Override
        public String toString() {
            return "{\n" +
                    gravity.toString() + ",\n" +
                    layoutSize.toString() + "\n" +
                    "}";
        }
    }

    private static class LineElement {
        final LayoutSize layoutStart, layoutEnd;
        final int gravityStart, gravityEnd;
        final Point delta;

        LineElement(LayoutSize start, LayoutSize end, int gravityStart, int gravityEnd, Point delta) {
            this.layoutStart = start;
            this.layoutEnd = end;
            this.gravityStart = gravityStart;
            this.gravityEnd = gravityEnd;
            this.delta = delta;
        }

        public Point getStartPoint() {
            return layoutStart.getPoint(gravityStart);
        }

        public Point getEndPoint() {
            Point end = layoutEnd.getPoint(gravityEnd);
            if (delta != null) {
                end.x += delta.x;
                end.y += delta.y;
            }
            return end;
        }

        public boolean isArea() {
            return true;
        }
    }
}
