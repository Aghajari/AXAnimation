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
package com.aghajari.axanimation.utils;

import androidx.annotation.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * A helper class for reflection
 *
 * @author AmirHossein Aghajari
 */
public class ReflectionUtils {

    private ReflectionUtils(){
    }

    private static final WeakHashMap<String, AccessibleObject> cachedMap = new WeakHashMap<>();
    private static final boolean DEBUG = true;

    public static Method getPrivateMethod(Object object, String methodName) {
        try {
            if (cachedMap.containsKey(methodName)) {
                AccessibleObject o = cachedMap.get(methodName);
                if (o instanceof Method)
                    return (Method) o;
            }
            Method m = object.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            cachedMap.put(methodName, m);
            return m;
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    public static void invokePrivateMethod(Method m, Object object) {
        try {
            if (m != null)
                m.invoke(object);
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
    }

    public static Field getPrivateField(Object object, String name) {
        try {
            if (cachedMap.containsKey(name)) {
                AccessibleObject o = cachedMap.get(name);
                if (o instanceof Field)
                    return (Field) o;
            }
            Field f = object.getClass().getDeclaredField(name);
            f.setAccessible(true);
            cachedMap.put(name, f);
            return f;
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    public static void setPrivateFieldValueWithThrows(Object object, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (cachedMap.containsKey(name)) {
            AccessibleObject o = cachedMap.get(name);
            if (o instanceof Field) {
                ((Field) o).set(object, value);
                return;
            }
        }
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        cachedMap.put(name, f);
        f.set(object, value);
    }

    public static void setPrivateFieldValue(Field f, Object object, Object value) {
        try {
            if (f != null)
                f.set(object, value);
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
    }

    @Nullable
    public static Object getPrivateFieldValue(Object object, String name) {
        try {
            if (cachedMap.containsKey(name)) {
                AccessibleObject o = cachedMap.get(name);
                if (o instanceof Field) {
                    return ((Field) o).get(object);
                }
            }
            Field f = object.getClass().getDeclaredField(name);
            f.setAccessible(true);
            cachedMap.put(name, f);
            return f.get(object);
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    public static <T> T getPrivateFieldValue(Object object, String name, @Nullable T def) {
        try {
            if (cachedMap.containsKey(name)) {
                AccessibleObject ao = cachedMap.get(name);
                if (ao instanceof Field) {
                    Object o = ((Field) ao).get(object);
                    if (o != null) {
                        //noinspection unchecked
                        return (T) o;
                    } else {
                        return def;
                    }
                }
            }
            Field f = object.getClass().getDeclaredField(name);
            f.setAccessible(true);
            cachedMap.put(name, f);
            Object o = f.get(object);
            if (o != null) {
                //noinspection unchecked
                return (T) o;
            }
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return def;
    }

    public static Object getPrivateFieldValueWithThrows(Object object, String name) throws NoSuchFieldException, IllegalAccessException {
        if (cachedMap.containsKey(name)) {
            AccessibleObject o = cachedMap.get(name);
            if (o instanceof Field) {
                return ((Field) o).get(object);
            }
        }
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        cachedMap.put(name, f);
        return f.get(object);
    }

    @Nullable
    public static Object getPrivateFieldValue(Field field, Object object) {
        try {
            if (field != null)
                return field.get(object);
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(Class<?> cls, String methodName, Object... params) throws NoSuchMethodException {
        if (params.length == 0)
            return cls.getMethod(methodName);

        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++)
            paramTypes[i] = (params[i] == null) ? Object.class : params[i].getClass();

        Method firstMethod = null;
        for (int i = 0; i < params.length; i++) {
            try {
                firstMethod = cls.getMethod(methodName, paramTypes);
                break;
            } catch (NoSuchMethodException ignore) {
                Class<?> org = paramTypes[i];
                Class<?> parent = paramTypes[i].getSuperclass();
                if (parent != null) {
                    paramTypes[i] = parent;
                    try {
                        firstMethod = cls.getMethod(methodName, paramTypes);
                        break;
                    } catch (NoSuchMethodException ignore2) {
                        paramTypes[i] = org;
                    }
                }
            }
        }
        if (firstMethod != null && isMatch(firstMethod.getParameterTypes(), params)) {
            return firstMethod;
        }

        Method[] methods = cls.getMethods();
        ArrayList<Method> listOfPossibleMethods = new ArrayList<>();

        for (Method method : methods) {
            if (method.getName().equals(methodName) &&
                    method.getParameterTypes().length == params.length) {
                listOfPossibleMethods.add(method);
            }
        }

        if (listOfPossibleMethods.size() == 1) {
            return listOfPossibleMethods.get(0);
        } else if (listOfPossibleMethods.size() > 1) {
            for (Method m : listOfPossibleMethods) {
                if (isMatch(m.getParameterTypes(), params)) {
                    return m;
                }
            }
        }

        throw new NoSuchMethodException("Method (" + methodName + ") not found in " + cls.getSimpleName());
    }

    private static boolean isMatch(Class<?>[] types, Object[] params) {
        boolean hasEnum = false;
        for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                Class<?> p = params[i].getClass();
                if (types[i].isEnum() && p == String.class) {
                    hasEnum = true;
                } else if (!types[i].isAssignableFrom(p)) {
                    return false;
                }
            }
        }
        if (hasEnum) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    Class<?> p = params[i].getClass();
                    if (types[i].isEnum() && p == String.class) {
                        //noinspection rawtypes
                        Class type = types[i];
                        params[i] = Enum.valueOf(type, (String) params[i]);
                    }
                }
            }
        }
        return true;
    }

}
