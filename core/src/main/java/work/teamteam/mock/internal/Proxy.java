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

package work.teamteam.mock.internal;

import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

public class Proxy<T> {
    private final Entry[] entries;
    private final T t;

    public Proxy(final T t) {
        this.t = t;
        final Method[] methods = t.getClass().getDeclaredMethods();
        entries = new Entry[methods.length];
        for (int i = 0; i < methods.length; i++) {
            entries[i] = new Entry(methods[i]);
        }
        Arrays.sort(entries, Comparator.comparing(Entry::getKey));
    }

    public Object match(final String key, final Object... args) throws InvocationTargetException, IllegalAccessException {
        final int i = Arrays.binarySearch(entries, key);
        if (i < 0) {
            throw new RuntimeException("Method not found: " + key);
        }
        return entries[i].method.invoke(t, args);
    }

    private static final class Entry implements Comparable<String> {
        final String key;
        final Method method;

        public Entry(final Method method) {
            this.method = method;
            method.setAccessible(true);
            this.key = method.getName() + Type.getMethodDescriptor(method);
        }

        public String getKey() {
            return key;
        }

        @Override
        public int compareTo(final String o) {
            return key.compareTo(o);
        }
    }
}
