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

package work.teamteam.mock;

import org.objectweb.asm.Type;

/**
 * Interface to support specifying custom returned defaults (per-class) for a mock.
 *
 * E.g by default mock(Foo.class).methodThatReturnsObject(); will return null. By overriding this class
 * we can change that to return e.g "response" for Strings
 */
public interface Defaults {
    <T> T get(final Class<T> clazz);

    final class Impl implements Defaults {
        public static final Impl IMPL = new Impl();
        private static final Object[] PRIMITIVE_DEFAULTS = new Object[] {
                null,
                false,
                (char) 0,
                (byte) 0,
                (short) 0,
                0,
                0.f,
                0L,
                0.0,
        };

        private Impl() {}

        @Override
        public <T> T get(final Class<T> clazz) {
            final int sort = Type.getType(clazz).getSort();
            return PRIMITIVE_DEFAULTS.length > sort ? (T) PRIMITIVE_DEFAULTS[sort] : null;
        }
    }
}
