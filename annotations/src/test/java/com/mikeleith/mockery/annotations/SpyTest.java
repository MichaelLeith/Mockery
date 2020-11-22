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

package com.mikeleith.mockery.annotations;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static com.mikeleith.mockery.Mockery.when;

public class SpyTest {
    @ParameterizedTest
    @MethodSource
    void testSpy(final Runnable target, final Class<? extends Exception> exception) {
        if (exception != null) {
            assertThrows(exception, () -> MockeryInject.inject(target));
        } else {
            MockeryInject.inject(target);
            target.run();
        }
    }

    private static Stream<Arguments> testSpy() {
        return Stream.of(Arguments.of(new Good(), null),
                Arguments.of(new NoConstructor(), null),
                Arguments.of(new BadConstructor(), RuntimeException.class),
                Arguments.of(new Primitive(), RuntimeException.class),
                Arguments.of(new PrimitiveArray(), RuntimeException.class),
                Arguments.of(new ObjectArray(), RuntimeException.class));
    }

    public static class Good implements Runnable {
        @Spy Example example;
        @Spy Example example1 = new Example(100);

        public void run() {
            assertNotNull(example);
            assertEquals(10, example.doSomething());
            when(example.doSomething()).thenReturn(100);
            assertEquals(100, example.doSomething());

            assertEquals(100, example1.doSomething());
            when(example1.doSomething()).thenReturn(1000);
            assertEquals(1000, example1.doSomething());
        }

        public static class Example {
            private final int val;

            public Example() {
                this(10);
            }

            public Example(final int i) {
                val = i;
            }

            public int doSomething() {
                return val;
            }
        }
    }

    public static final class NoConstructor implements Runnable {
        @Spy Example example;

        @Override
        public void run() {
            assertEquals(100, example.doSomething());
            when(example.doSomething()).thenReturn(1000);
            assertEquals(1000, example.doSomething());
        }

        public static class Example {
            public int doSomething() {
                return 100;
            }
        }
    }

    public static final class BadConstructor implements Runnable {
        @Spy Example example;

        @Override
        public void run() {
            fail();
        }

        public static class Example {
            public Example(final int i) {}
        }
    }

    public static final class Primitive implements Runnable {
        @Spy int example;

        @Override
        public void run() {
            fail();
        }
    }

    public static final class PrimitiveArray implements Runnable {
        @Spy int[] example;

        @Override
        public void run() {
            fail();
        }
    }

    public static final class ObjectArray implements Runnable {
        @Spy Example[] example;

        @Override
        public void run() {
            fail();
        }

        public static class Example {
        }
    }
}
