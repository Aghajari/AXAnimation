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
import com.aghajari.axanimation.utils.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link NotAnimatedRule} which invokes a {@link Method} of targetView
 *
 * @author AmirHossein Aghajari
 */
public class InvokeRule extends NotAnimatedRule<Object[]> implements Debugger {
    private final String methodName;

    public InvokeRule(String methodName, Object... args) {
        super(args);
        this.methodName = methodName;
    }

    public InvokeRule(int viewID, String methodName, Object... args) {
        super(viewID, args);
        this.methodName = methodName;
    }

    public InvokeRule(View view, String methodName, Object... args) {
        super(view, args);
        this.methodName = methodName;
    }

    @Override
    public Map<String, String> debugValues(@NonNull View view) {
        Map<String, String> map = new HashMap<>();
        map.put("MethodName", methodName);
        return map;
    }

    @Override
    public void apply(View targetView) {
        try {
            invoke(targetView, methodName, data);
        } catch (Exception e) {
            Log.e("AXAnimation", "InvokeRule (" + methodName + ")", e);
        }
    }

    protected Object invoke(Object cls, String methodName, Object... params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = getMethod(cls.getClass(), methodName, params);
        if (getAccessible())
            method.setAccessible(true);
        return method.invoke(cls, params);
    }

    protected boolean getAccessible() {
        return true;
    }

    protected Method getMethod(Class<?> cls, String methodName, Object... params) throws NoSuchMethodException {
        return ReflectionUtils.getMethod(cls, methodName, params);
    }

}
