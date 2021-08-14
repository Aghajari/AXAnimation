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
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.AXAnimation;
import com.aghajari.axanimation.utils.SizeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to translate {@link LiveSize} and debug it.
 *
 * @author Amir Hossein Aghajari
 * @hide
 * @see LiveSize#translate(Context, int)
 */
public class LiveSizeDebugHelper {

    private LiveSizeDebugHelper() {
    }

    public static Map<String, String> debug(LiveSize liveSize, View view, int gravity) {
        HashMap<String, String> map = new HashMap<>();
        map.put("LiveSize", translate(liveSize, gravity, view.getContext()));
        return map;
    }

    public static Map<String, String> debug(LiveSize x, LiveSize y, View view, int gravity) {
        return debug("X", x, "Y", y, view, gravity);
    }

    public static Map<String, String> debug(String xKey, LiveSize x, String yKey, LiveSize y, View view, int gravity) {
        HashMap<String, String> map = new HashMap<>();
        if (x != null)
            map.put(xKey, x.translate(view.getContext(), gravity & Gravity.HORIZONTAL_GRAVITY_MASK));
        if (y != null)
            map.put(yKey, y.translate(view.getContext(), gravity & Gravity.VERTICAL_GRAVITY_MASK));
        return map;
    }

    public static Map<String, String> debug(String[] keys, LiveSizePoint start, LiveSizePoint end, View view, int gravity) {
        HashMap<String, String> map = new HashMap<>();
        int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        int vg = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        if (start.x != null)
            map.put(keys[0], start.x.translate(view.getContext(), hg));
        if (start.y != null)
            map.put(keys[1], start.y.translate(view.getContext(), vg));
        if (end.x != null)
            map.put(keys[2], end.x.translate(view.getContext(), hg));
        if (end.y != null)
            map.put(keys[3], end.y.translate(view.getContext(), vg));
        return map;
    }

    public static Map<String, String> debug(LayoutSize layoutSize, @NonNull View view) {
        HashMap<String, String> map = new HashMap<>();
        if (layoutSize.liveLeft != null)
            map.put("Left", layoutSize.liveLeft.translate(view.getContext(), Gravity.LEFT));
        if (layoutSize.liveRight != null)
            map.put("Right", layoutSize.liveRight.translate(view.getContext(), Gravity.RIGHT));
        if (layoutSize.liveTop != null)
            map.put("Top", layoutSize.liveTop.translate(view.getContext(), Gravity.TOP));
        if (layoutSize.liveBottom != null)
            map.put("Bottom", layoutSize.liveBottom.translate(view.getContext(), Gravity.BOTTOM));
        return map;
    }

    public static Map<String, String> debug(@NonNull View view, LayoutSize... data) {
        if (data.length == 0)
            return null;
        if (data.length == 1)
            return debug(data[0], view);

        HashMap<Integer, Map<String, String>> map = new HashMap<>();
        Map<String, String> m = null;
        for (int i = 0; i < data.length; i++) {
            m = debug(data[i], view);
            if (!m.isEmpty()) {
                map.put(i, m);
            }
        }
        if (map.isEmpty())
            return null;
        if (map.size() == 1)
            return m;

        return convert(map);
    }

    public static Map<String, String> debugLine(@NonNull View view, LiveSizePoint[]... data) {
        if (data.length == 0)
            return null;

        HashMap<Integer, Map<String, String>> map = new HashMap<>();
        Map<String, String> m = null;
        String[] keys = new String[]{"StartX", "StartY", "StopX", "StopY"};
        for (int i = 0; i < data.length; i++) {
            LiveSizePoint[] points = data[i];
            m = debug(keys, points[0], points[1], view, Gravity.CENTER);
            if (!m.isEmpty())
                map.put(i, m);
        }
        if (map.isEmpty())
            return null;
        if (map.size() == 1)
            return m;

        return convert(map);
    }

    private static HashMap<String, String> convert(HashMap<Integer, Map<String, String>> map) {
        HashMap<String, String> result = new HashMap<>();
        for (Integer index : map.keySet()) {
            Map<String, String> m2 = map.get(index);
            if (m2 == null)
                continue;

            StringBuilder reader = new StringBuilder("{\n     ");
            for (String key : m2.keySet()) {
                reader.append(key).append(": ").append(m2.get(key)).append(",\n     ");
            }
            reader.delete(reader.length() - 2, reader.length()).append("}");

            result.put(index.toString(), reader.toString());
        }
        return result;
    }

    public static String translate(LiveSize liveSize, int gravity, Context context) {
        StringBuilder sb = new StringBuilder();

        String target = get("target.", gravity);
        if (target == null)
            target = "target";
        target += " = ";

        boolean firstValue = true;

        for (LiveSize.Pair<Integer, ArrayList<LiveSize.Pair<Integer, Float>>> p : liveSize.value) {
            StringBuilder sbi = new StringBuilder();
            sbi.append("(");
            boolean firstInnerValue = true;

            for (LiveSize.Pair<Integer, Float> p2 : p.second) {
                String r2;

                if (liveSize.relatedViews.containsKey(p2)) {
                    r2 = get(p2, p2.second.intValue(), context);
                } else {
                    r2 = calculate(p2.second, gravity);
                }

                if (!firstInnerValue) {
                    switch (p2.first) {
                        case LiveSize.TYPE_DIVIDE:
                            sbi.append(" / ").append(r2);
                            break;
                        case LiveSize.TYPE_MULTIPLE:
                            sbi.append(" * ").append(r2);
                            break;
                        case LiveSize.TYPE_MINUS:
                            sbi.append(" - ").append(r2);
                            break;
                        case LiveSize.TYPE_PLUS:
                        default:
                            sbi.append(" + ").append(r2);
                            break;
                    }
                } else {
                    switch (p2.first) {
                        case LiveSize.TYPE_DIVIDE:
                            sbi.append("1 / ").append(r2);
                            break;
                        case LiveSize.TYPE_MULTIPLE:
                            sbi.append("1 * ").append(r2);
                            break;
                        case LiveSize.TYPE_MINUS:
                            sbi.append("-(").append(r2).append(")");
                            break;
                        case LiveSize.TYPE_PLUS:
                        default:
                            sbi.append(r2);
                            break;
                    }
                }
                sbi.trimToSize();
                firstInnerValue = false;
            }
            sbi.append(")");

            if (!firstValue) {
                sb = new StringBuilder("(" + sb);
                switch (p.first) {
                    case LiveSize.TYPE_DIVIDE:
                        sb.append(" / ").append(sbi);
                        break;
                    case LiveSize.TYPE_MULTIPLE:
                        sb.append(" * ").append(sbi);
                        break;
                    case LiveSize.TYPE_MINUS:
                        sb.append(" - ").append(sbi);
                        break;
                    case LiveSize.TYPE_PLUS:
                    default:
                        sb.append(" + ").append(sbi);
                        break;
                }
                sb.append(")");
            } else {
                sb.append(sbi);
            }
            firstValue = false;
        }
        if (sb.charAt(0) == '(') {
            return target + sb.substring(1, sb.length() - 1);
        } else {
            return target + sb.toString();
        }
    }

    private static String get(LiveSize.Pair<?, ?> pair, int gravity, Context context) {
        int id = pair.viewID;
        if (pair.view != null)
            id = pair.view.getId();

        String viewName;
        try {
            viewName = context.getResources().getResourceEntryName(id);
        } catch (Resources.NotFoundException ignore) {
            viewName = "unknownView";
        }
        viewName += ".";

        return get(viewName, 1, gravity);
    }

    private static String get(String viewName, int value, int gravity) {
        String out = get(viewName, gravity);
        if (out != null)
            return out;

        return String.valueOf(value);
    }

    private static String get(String viewName, int gravity) {
        if (Gravity.isHorizontal(gravity)) {
            int hg = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            switch (hg) {
                case Gravity.LEFT:
                    return viewName + "left";
                case Gravity.RIGHT:
                    return viewName + "right";
                case Gravity.CENTER_HORIZONTAL:
                    return viewName + "centerX";
                case Gravity.FILL_HORIZONTAL:
                    return viewName + "width";
            }
        } else {
            int vg = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (vg) {
                case Gravity.TOP:
                    return viewName + "top";
                case Gravity.BOTTOM:
                    return viewName + "bottom";
                case Gravity.CENTER_VERTICAL:
                    return viewName + "centerY";
                case Gravity.FILL_VERTICAL:
                    return viewName + "height";
            }
        }
        return null;
    }

    private static String calculate(float value, int gravity) {
        if (!SizeUtils.isCustomSize((int) value))
            return get(value);

        if (value == AXAnimation.PARENT_WIDTH)
            return "parent.width";
        else if (value == AXAnimation.PARENT_HEIGHT)
            return "parent.height";
        else if (value == AXAnimation.MATCH_PARENT)
            return Gravity.isHorizontal(gravity) ? "parent.width" : "parent.height";
        else if (value == AXAnimation.WRAP_CONTENT)
            return Gravity.isHorizontal(gravity) ? "target.measuredWidth" : "target.measuredHeight";
        else if (value == AXAnimation.CONTENT_WIDTH)
            return "target.measuredWidth";
        else if (value == AXAnimation.CONTENT_HEIGHT)
            return "target.measuredHeight";
        else if (value == AXAnimation.ORIGINAL)
            return get("original.", (int) value, gravity);
        else if (value == AXAnimation.TARGET)
            return get("target.", (int) value, gravity);
        else if (value == AXAnimation.PARENT)
            return get("parent.", (int) value, gravity);
        else if (((int) value & SizeUtils.MASK) == SizeUtils.PARENT)
            return get("parent.", (int) value, ((int) value &
                    (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK)));
        else if (((int) value & SizeUtils.MASK) == SizeUtils.ORIGINAL)
            return get("original.", (int) value, ((int) value &
                    (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK)));
        else if (((int) value & SizeUtils.MASK) == SizeUtils.TARGET)
            return get("target.", (int) value, ((int) value &
                    (Gravity.VERTICAL_GRAVITY_MASK | Gravity.HORIZONTAL_GRAVITY_MASK)));

        return get(value);
    }

    private static String get(float value) {
        return value == (int) value ? Integer.toString((int) value) : Float.toString(value);
    }
}