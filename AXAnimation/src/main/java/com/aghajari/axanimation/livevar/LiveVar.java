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

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

/**
 * LiveVar is a data holder class helps you to update animator value during animation.
 *
 * @param <T> The type of data held by this instance
 * @author AmirHossein Aghajari
 */
public class LiveVar<T> implements Cloneable, Comparable<Object> {

    protected T value;

    public LiveVar(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public void update(Object var) {
        if (var == null) {
            value = null;
            return;
        }

        if (var instanceof LiveVar) {
            value = ((LiveVar<T>) var).value;
        } else {
            if (value != null && value.getClass().isArray() && var.getClass().isArray()) {
                // Wtf, we can't cast Float[] to float[],... fix it
                if (value.getClass() == Float[].class && var.getClass() == float[].class) {
                    Float[] tmp = new Float[((float[]) var).length];
                    for (int i = 0; i < tmp.length; i++)
                        tmp[i] = ((float[]) var)[i];
                    value = (T) tmp;
                } else if (value.getClass() == Integer[].class && var.getClass() == int[].class) {
                    Integer[] tmp = new Integer[((int[]) var).length];
                    for (int i = 0; i < tmp.length; i++)
                        tmp[i] = ((int[]) var)[i];
                    value = (T) tmp;
                }
            }
            value = (T) var;
        }
    }

    public void update(Integer... var) {
        update((Object) var);
    }

    public void update(Float... var) {
        update((Object) var);
    }

    public void update(Object... var) {
        update((Object) var);
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @NonNull
    @Override
    public String toString() {
        T value = get();

        if (value != null && value.getClass().isArray())
            return Arrays.toString((Object[]) value);

        return String.valueOf(value);
    }

    /**
     * @return Returns another LiveVar to reverse value of current LiveVar values (if it's an array)
     */
    public CompareArrayLiveVar<T> reverseArray() {
        return new CompareArrayLiveVar<>(this);
    }

    /**
     * @return Returns another LiveVar and compares values of {@link #value} (if it's an array)
     * and returns new array by calling {@link #get()},
     */
    public CompareArrayLiveVar<T> compareArray(Comparator<Object> comparator) {
        return new CompareArrayLiveVar<>(this, comparator);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public int compareTo(Object o) {
        if (this == o)
            return 0;

        T value = get();
        if (value instanceof Comparable)
            return ((Comparable) value).compareTo(o);

        return -1;
    }

    public static <T> LiveVar<T> ofValue(T value) {
        return new LiveVar<>(value);
    }

    @SafeVarargs
    public static <T> LiveVar<T[]> ofArray(T... values) {
        return new LiveVar<>(values);
    }

    public static LiveVar<Float[]> ofFloatArray(float... values) {
        LiveVar<Float[]> liveVar = new LiveVar<>(new Float[0]);
        liveVar.update(values);
        return liveVar;
    }

    public static LiveVar<Integer[]> ofIntArray(int... values) {
        LiveVar<Integer[]> liveVar = new LiveVar<>(new Integer[0]);
        liveVar.update(values);
        return liveVar;
    }

    public static <T> LiveVar<T> reverse(LiveVar<T> liveVar) {
        return liveVar.reverseArray();
    }

    /**
     * Compares values of {@link LiveVar#value} (if it's an array)
     * and returns new array by calling {@link #get()}
     */
    private static class CompareArrayLiveVar<T> extends LiveVar<T> {
        protected final LiveVar<T> liveVar;
        protected final Comparator<Object> comparator;

        public CompareArrayLiveVar(LiveVar<T> liveVar) {
            this(liveVar, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 == o2)
                        return 0;
                    return -1;
                }
            });
        }

        public CompareArrayLiveVar(LiveVar<T> liveVar, @NonNull Comparator<Object> comparator) {
            super(null);
            this.liveVar = liveVar;
            this.comparator = comparator;
        }

        @Override
        public T get() {
            return compare(liveVar.get());
        }

        @Override
        public void set(T value) {
            liveVar.set(compare(value));
        }

        protected T compare(T val) {
            if (val == null || !val.getClass().isArray())
                return val;

            //noinspection unchecked
            T val2 = (T) Arrays.copyOf((Object[]) val, Array.getLength(val));
            Arrays.sort((Object[]) val2, comparator);
            return val2;
        }

        @Override
        public void update(Object var) {
            liveVar.update(var);
            liveVar.set(compare(liveVar.get()));
        }

        @Override
        public void update(Integer... var) {
            liveVar.update(var);
            liveVar.set(compare(liveVar.get()));
        }

        @Override
        public void update(Float... var) {
            liveVar.update(var);
            liveVar.set(compare(liveVar.get()));
        }

        @Override
        public void update(Object... var) {
            liveVar.update(var);
            liveVar.set(compare(liveVar.get()));
        }
    }
}
