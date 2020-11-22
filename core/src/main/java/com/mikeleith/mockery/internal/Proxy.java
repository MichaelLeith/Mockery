/*
 * Copyright (c) 2020 Michael Leith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mikeleith.mockery.internal;

import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public class Proxy<T> {
    private final String[] keys;
    private final Method[] entries;
    private final T t;

    @SuppressWarnings("unchecked")
    public static <T> Proxy<T> of(final T t) {
        return Proxy.build((Class<T>) t.getClass()).apply(t);
    }

    private Proxy(final T t, final String[] keys, final Method[] methods) {
        this.t = Objects.requireNonNull(t);
        this.entries = Objects.requireNonNull(methods);
        this.keys = Objects.requireNonNull(keys);
    }

    public Object match(final String key, final Object... args) throws InvocationTargetException, IllegalAccessException {
        final int i = Arrays.binarySearch(keys, key);
        if (i < 0) {
            throw new RuntimeException("Method not found: " + key);
        }
        return entries[i].invoke(t, args);
    }

    public static <T> Function<T, Proxy<T>> build(final Class<T> clazz) {
        // @todo: combine these loops, just need a multi-array sort method
        final Method[] methods = clazz.getDeclaredMethods();
        final Entry[] entries = new Proxy.Entry[methods.length];
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            entries[i] = new Proxy.Entry(method.getName() + Type.getMethodDescriptor(method), method);
        }
        Arrays.sort(entries, Comparator.comparing(Proxy.Entry::getKey));

        final String[] keys = new String[entries.length];
        final Method[] methods1 = new Method[entries.length];
        for (int i = 0; i < entries.length; i++) {
            keys[i] = entries[i].key;
            methods1[i] = entries[i].method;
        }
        return t -> new Proxy<>(t, keys, methods1);
    }

    public static final class Entry {
        final String key;
        final Method method;

        public Entry(final String key, final Method method) {
            this.method = method;
            method.setAccessible(true);
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
