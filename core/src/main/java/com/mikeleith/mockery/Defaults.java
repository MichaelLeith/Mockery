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

package com.mikeleith.mockery;

/**
 * Interface to support specifying custom returned defaults (per-class) for a mock.
 *
 * E.g by default mock(Foo.class).methodThatReturnsObject(); will return null. By overriding this class
 * we can change that to return e.g "response" for Strings
 */
public interface Defaults {
    <T> Object get(final Class<T> clazz);

    /**
     * Default implementation of Defaults.
     */
    final class Impl implements Defaults {
        public static final Impl IMPL = new Impl();

        private Impl() {}

        @SuppressWarnings("unchecked")
        @Override
        public <T> Object get(final Class<T> clazz) {
            if (clazz.isPrimitive()) {
                if (clazz == Integer.TYPE) {
                    return 0;
                } else if (clazz == Void.TYPE) {
                    return null;
                } else if (clazz == Boolean.TYPE) {
                    return false;
                } else if (clazz == Byte.TYPE) {
                    return (byte) 0;
                } else if (clazz == Character.TYPE) {
                    return (char) 0;
                } else if (clazz == Short.TYPE) {
                    return (short) 0;
                } else if (clazz == Double.TYPE) {
                    return 0.0;
                } else if (clazz == Float.TYPE) {
                    return 0.0f;
                } else if (clazz == Long.TYPE) {
                    return 0L;
                }
            }
            return null;
        }
    }
}
