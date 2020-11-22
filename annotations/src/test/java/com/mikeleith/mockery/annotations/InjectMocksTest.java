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

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static com.mikeleith.mockery.Mockery.mock;

public class InjectMocksTest {
    @Mock Example example;
    @Mock Example example2;
    // ignored because of "-parameters"
    int i = 1;
    @InjectMocks Target target;

    @Test
    void testMock() {
        MockeryInject.inject(this);
        assertNotNull(target);
        assertEquals(example, target.example);
        assertEquals(example2, target.example2);
    }

    @Test
    void testMockThrowsOnInitializedTarget() {
        final InitializedInject target = new InitializedInject();
        assertThrows(RuntimeException.class, () -> MockeryInject.inject(target));
    }

    @Test
    void testMockThrowsOnNoValidConstructor() {
        final InjectWithMissingParam target = new InjectWithMissingParam();
        assertThrows(RuntimeException.class, () -> MockeryInject.inject(target));
    }

    @Test
    void testMockThrowsOnHandlesThrowingConstructor() {
        final HandlesThrowingConstructor target = new HandlesThrowingConstructor();
        assertThrows(RuntimeException.class, () -> MockeryInject.inject(target));
    }

    @Test
    void testMockWorksWithoutNamedParams() {
        final InjectWithoutNamedParam target = new InjectWithoutNamedParam();
        MockeryInject.inject(target);
        if (target.target.example != target.bar && target.target.example != target.foo) {
            fail();
        }
        if (target.target.example2 != target.bar && target.target.example2 != target.foo) {
            fail();
        }
    }

    @Test
    void testPrimitives() {
        final InjectPrimitives injectPrimitives = new InjectPrimitives();
        MockeryInject.inject(injectPrimitives);
        assertTrue(injectPrimitives.target.hit);
    }

    @Test
    void testThrowsOnNoPublicConstructor() {
        final HandlesPrivateConstructor target = new HandlesPrivateConstructor();
        assertThrows(RuntimeException.class, () -> MockeryInject.inject(target));
    }

    @Test
    void testInjectWithUninitializedParam() {
        final InjectWithUninitializedParam target = new InjectWithUninitializedParam();
        MockeryInject.inject(target);
        assertNotNull(target.target);
    }

    public static final class InjectPrimitives {
        boolean bool = false;
        byte b = 0;
        char c = 1;
        short s = 2;
        int i = 3;
        long l = 4;
        float f = 5;
        double d = 6;
        @InjectMocks Target target;

        public static final class Target {
            boolean hit = false;

            public Target(int i, boolean bo, byte b, float f, double d, long l, short s, char c) {
                assertFalse(bo);
                assertEquals(0, b);
                assertEquals(1, c);
                assertEquals(2, s);
                assertEquals(3, i);
                assertEquals(4, l);
                assertEquals(5, f);
                assertEquals(6, d);
                hit = true;
            }
        }
    }

    public static final class InitializedInject {
        @InjectMocks Target target = new Target(mock(Example.class), mock(Example.class));
    }

    public static final class InjectWithMissingParam {
        @InjectMocks Target target;

        public static class Target {
            public Target(int i) {}
        }
    }

    public static final class InjectWithUninitializedParam {
        String s;
        @InjectMocks Target target;

        public static class Target {
            public Target(String s) {}
        }
    }

    public static final class InjectWithoutNamedParam {
        @Mock Example foo;
        @Mock Example bar;
        @InjectMocks Target target;
    }

    public static final class HandlesThrowingConstructor {
        int i = 1;
        @InjectMocks Target target;

        public static class Target {
            public Target(int i) throws InvocationTargetException {
                throw new InvocationTargetException(null);
            }
        }
    }

    public static final class HandlesPrivateConstructor {
        int i = 1;
        @InjectMocks Target target;

        public static class Target {
            private Target(int i) {
            }
        }
    }

    public static class Target {
        public final Example example;
        public final Example example2;

        public Target(final Example example) {
            throw new RuntimeException("shouldn't be called");
        }

        public Target(final Example example, final Example example2) {
            this.example = example;
            this.example2 = example2;
        }

        public Target() {
            throw new RuntimeException("shouldn't be called");
        }

        public Target(final int i) {
            throw new RuntimeException("shouldn't be called");
        }

        public Target(final Example e, final int j) {
            throw new RuntimeException("shouldn't be called");
        }
    }

    public interface Example {
        int doSomething();
    }
}
