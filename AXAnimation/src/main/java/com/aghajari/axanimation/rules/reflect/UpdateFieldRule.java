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
package com.aghajari.axanimation.rules.reflect;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.rules.Debugger;
import com.aghajari.axanimation.rules.NotAnimatedRule;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link NotAnimatedRule} which sets a {@link java.lang.reflect.Field} of targetView
 *
 * @author AmirHossein Aghajari
 */
public class UpdateFieldRule extends NotAnimatedRule<Object> implements Debugger {
    private final String fieldName;

    public UpdateFieldRule(Object data, String fieldName) {
        super(data);
        this.fieldName = fieldName;
    }

    public UpdateFieldRule(int viewID, Object data, String fieldName) {
        super(viewID, data);
        this.fieldName = fieldName;
    }

    public UpdateFieldRule(View view, Object data, String fieldName) {
        super(view, data);
        this.fieldName = fieldName;
    }

    @Override
    public Map<String, String> debugValues(@NonNull View view) {
        Map<String, String> map = new HashMap<>();
        map.put("FieldName", fieldName);
        return map;
    }

    @Override
    public void apply(View targetView) {
        try {
            setFieldValue(targetView, fieldName, data);
        } catch (Exception e) {
            Log.e("AXAnimation", "UpdateFieldRule (" + fieldName + ")", e);
        }
    }

    protected void setFieldValue(Object obj, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(), fieldName);
        if (getAccessible())
            field.setAccessible(true);
        field.set(obj, value);
    }

    protected Object getFieldValue(Object obj, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(obj.getClass(), fieldName);
        if (getAccessible())
            field.setAccessible(true);
        return field.get(obj);
    }

    protected boolean getAccessible() {
        return false;
    }

    protected Field getField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return cls.getField(fieldName);
    }
}
