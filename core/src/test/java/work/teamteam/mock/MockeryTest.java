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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MockeryTest {
    @BeforeEach
    void setUp() {
        Mockery.reset();
    }

    public interface TestInterface {
        String string();
        double[] arr();
        void v();
        int i();
        short s();
        char c();
        byte b();
        float f();
        long l();
        double d();
        void arg(final String s);
        void arg(final float f);
        void arg(final double d);
        void arg(final int i);
        void arg(final short s);
        void arg(final byte b);
        void arg(final long l);
        void arg(final double[] arr);
        void arg(final String s, final int i);
    }

    public static class Foo {
        private final int i;

        public Foo(int i, int j) {
            this.i = i + j;
        }

        public Foo(int i) {
            this.i = i;
        }

        public void testWithVoid(Void foo) {
        }

        // @note: won't work with package-private, can't override them outside of the same pkg... interesting
        protected String test(final String woo) {
            return woo;
        }

        protected String intAcc(final int j) {
            return "woo";
        }

        public String intAcc(final String first, final long second, final int j) {
            return "woo";
        }

        public String intAcc(final String first, final double second, final int j) {
            return "woo";
        }

        public String intAcc(final boolean first, final double second, final long third, final int j) {
            return "woo";
        }

        public String mediumArgLength(final boolean first,
                                      final double second,
                                      final long third,
                                      final int j,
                                      final int k,
                                      final int l) {
            return "woo";
        }
    }

    public static final class Final {}

    // @todo: test inheritance
    // @todo: can you spy an interface?
    @Test
    void testFinal() {
        assertThrows(RuntimeException.class, () -> Mockery.mock(Final.class));
    }

    @Test
    void testVerifyThrowsOnFail() {
        assertThrows(RuntimeException.class, () ->
                Mockery.verify(Mockery.mock(Foo.class), 1).test("woo"));
    }

    @Test
    void testVerify() {
        final Foo impl = Mockery.mock(Foo.class);
        impl.intAcc(1);
        impl.intAcc(1);
        impl.intAcc(2);
        Mockery.verify(impl, 2).intAcc(1);
        Mockery.verify(impl, 0).intAcc(-1);
        Mockery.verify(impl, 1).intAcc(2);
        Mockery.verify(impl, Times.eq(1)).intAcc(2);
        Mockery.verify(impl, Times.ge(1)).intAcc(2);
        Mockery.verify(impl, Times.le(1)).intAcc(2);
        Mockery.verify(impl, Times.lt(2)).intAcc(2);
        Mockery.verify(impl, Times.gt(0)).intAcc(2);
    }

    @Test
    void testCapture() {
        final Foo impl = Mockery.mock(Foo.class);
        impl.intAcc(1);
        impl.intAcc(1);
        impl.intAcc(2);
        final Capture<Integer> capture = Capture.of(int.class);
        Mockery.verify(impl, 2).intAcc(1);
        Mockery.verify(impl, 3).intAcc(Matchers.capture(capture));
        assertEquals(2, capture.tail());
        //assertEquals(Arrays.asList(1, 1, 2), capture.captured());
        Mockery.verify(impl, 0).intAcc(-1);
        Mockery.verify(impl, 1).intAcc(2);

        final Capture<Long> capture1 = Capture.of(long.class);
        final Capture<Integer> capture2 = Capture.of(int.class);
        impl.intAcc("foo", 1L, 2);
        impl.intAcc("bar", 2L, 3);
        impl.intAcc("foo", 3L, 4);
        Mockery.verify(impl, 2).intAcc(Matchers.eq("foo"),
                Matchers.capture(capture1),
                Matchers.capture(capture2));
        assertEquals(3, capture1.tail());
        //assertEquals(Arrays.asList(1L, 3L), capture1.captured());
        assertEquals(4, capture2.tail());
        //assertEquals(Arrays.asList(2, 4), capture2.captured());
    }

    @Test
    void testSpyMissingConstructor() {
        assertThrows(RuntimeException.class, () -> Mockery.spy(Foo.class, 1, 2, 3, 4));
    }

    @Test
    void testResetNonMockFails() {
        assertThrows(RuntimeException.class, () -> Mockery.reset(new Foo(1)));
    }

    @Test
    void testWhenNonMock() {
        assertThrows(RuntimeException.class, () -> Mockery.when(new Foo(1)));
    }

    @Test
    void testVerifyNonMock() {
        assertThrows(RuntimeException.class, () -> Mockery.verify(new Foo(1), 1));
    }

    @Test
    void testSpyConcrete() {
        final Foo foo = Mockery.spy(new Foo(1));
        assertEquals("woo", foo.test("woo"));
        Mockery.verify(foo, 1).test("woo");
        Mockery.when(foo.test("woo")).thenReturn("lol");
        assertEquals("lol", foo.test("woo"));
        Mockery.verify(foo, 2).test("woo");
    }

    @Test
    void testSpyClass() throws Exception {
        final Foo foo = Mockery.spy(Foo.class, 1);
        assertEquals("woo", foo.test("woo"));
        Mockery.verify(foo, 1).test("woo");
        Mockery.when(foo.test("woo")).thenReturn("lol");
        assertEquals("lol", foo.test("woo"));
        Mockery.verify(foo, 2).test("woo");
    }

    @Test
    void testSpyWithWrongArgType() {
        assertThrows(RuntimeException.class, () -> Mockery.spy(Foo.class, "lol", 1));
    }

    @Test
    void testSpyWithTooManyArgType() {
        assertThrows(RuntimeException.class, () -> Mockery.spy(Foo.class, 1, 1, 1));
    }

    @Test
    void testMockConcrete() {
        final Foo impl = Mockery.mock(Foo.class);
        assertNull(impl.test("welp"));
        Mockery.verify(impl, 1).test("welp");
        Mockery.verify(impl, 0).test("woo");
        Mockery.reset(impl);
        Mockery.verify(impl, 0).test("welp");

        Mockery.when(impl.intAcc("lol", 1L, 0)).thenReturn("welp2");
        Mockery.when(impl.intAcc("lol", 1L, 1)).thenReturn("welp3");
        // @todo: can we support this?
        assertNull(impl.intAcc("lol", 1L, 0));
        assertEquals("welp3", impl.intAcc("lol", 1L, 1));
        assertNull(impl.intAcc("lol", 1L, 2));
        Mockery.verify(impl, 1).intAcc("lol", 1L, 0);
    }

    @Test
    void testArgumentMatchersWithMixedArgs() {
        final Foo impl = Mockery.mock(Foo.class);
        assertThrows(RuntimeException.class, () -> {
            Mockery.when(impl.intAcc(Matchers.any(String.class),
                    1L,
                    Matchers.eq(0))).thenReturn("welp3");
        });
    }

    @Test
    void testArgumentMatchers() {
        final Foo impl = Mockery.mock(Foo.class);
        Mockery.when(impl.test(Matchers.any())).thenReturn("welp");
        assertEquals("welp", impl.test("welp"));
        assertEquals("welp", impl.test("lol"));
        assertEquals("welp", impl.test(null));

        Mockery.when(impl.intAcc(Matchers.anyInt())).thenReturn("welp");
        assertEquals("welp", impl.intAcc(-1));
        assertEquals("welp", impl.intAcc(0));
        assertEquals("welp", impl.intAcc(1));

        Mockery.when(impl.intAcc(Matchers.matchesInt(i -> i >= 0))).thenReturn("welp2");
        assertNull(impl.intAcc(-1));
        assertEquals("welp2", impl.intAcc(0));
        assertEquals("welp2", impl.intAcc(1));

        Mockery.when(impl.intAcc(Matchers.any(String.class),
                Matchers.matchesLong(i -> i >= 0),
                Matchers.eq(0))).thenReturn("welp3");
        assertEquals("welp3", impl.intAcc("foo", 0, 0));
        assertNull(impl.intAcc("foo", -1, 0));
        assertNull(impl.intAcc("foo", 1, 1));
        assertNull(impl.intAcc(null, 1, 0));
    }

    @Test
    void testWhenUsesLast() {
        final Foo impl = Mockery.mock(Foo.class);
        Mockery.when(impl.test(Matchers.eq("foo"))).thenReturn("1");
        Mockery.when(impl.test(Matchers.eq("bar"))).thenReturn("2");
        assertNull(impl.test("foo"));
        assertEquals("2", impl.test("bar"));
        assertNull(impl.test(null));
    }

    @Test
    void testCanUseWhenOnMultipleFunctions() {
        final Foo impl = Mockery.mock(Foo.class);
        Mockery.when(impl.intAcc(Matchers.anyInt())).thenReturn("welp");
        Mockery.when(impl.test("bar")).thenReturn("2");
        assertEquals("welp", impl.intAcc(-1));
        assertEquals("2", impl.test("bar"));
    }

    @Test
    void testMocksCanBeUsedInWhenThenReturn() {
        final Foo impl = Mockery.mock(Foo.class);
        Mockery.when(impl.test(Matchers.any())).thenReturn("foo");
        Mockery.when(impl.intAcc(Matchers.anyInt())).thenReturn(impl.test("well"));
        assertEquals("foo", impl.intAcc(-1));
    }

    @Test
    void testMocksCanBeUsedInWhenParameters() {
        final Foo impl = Mockery.mock(Foo.class);
        Mockery.when(impl.test(Matchers.any())).thenReturn("foo");
        Mockery.when(impl.test(impl.test("bar"))).thenReturn("foo");
        assertEquals("foo", impl.test("foo"));
        assertNull(impl.test("bar"));
    }

    @Test
    void testMockReplaces() {
        final Foo impl = Mockery.mock(Foo.class);
        Mockery.when(impl.test(Matchers.any())).thenReturn("foo");
        Mockery.when(impl.test(Matchers.any())).thenReturn("bar");
        assertEquals("bar", impl.test("lol"));

        Mockery.when(impl.test("welp")).thenReturn("bar");
        assertNull(impl.test("lol"));
        assertEquals("bar", impl.test("welp"));
    }

    @Test
    void testMockInterface() {
        final TestInterface impl = Mockery.mock(TestInterface.class);
        assertNull(impl.string());
        assertNull(impl.arr());
        impl.v();
        assertEquals(0, impl.i());
        assertEquals(0, impl.s());
        assertEquals(0, impl.c());
        assertEquals(0, impl.b());
        assertEquals(0.0f, impl.f());
        assertEquals(0.0, impl.d());
        assertEquals(0, impl.l());
        impl.arg("woo");
        impl.arg(1);
        impl.arg((byte) 1);
        impl.arg((short) 1);
        impl.arg((char) 1);
        impl.arg(1L);
        impl.arg(1.0f);
        impl.arg(1.0);
        impl.arg(new double[]{1.0, 2.0});
        impl.arg("woo", 100);

        Mockery.verify(impl, 1).arg("woo", 100);
        Mockery.reset(impl);
        Mockery.verify(impl, 0).arg("woo", 100);

        Mockery.when(impl.i()).thenReturn(1).thenReturn(2);
        Mockery.verify(impl, 0).i();
        assertEquals(1, impl.i());
        assertEquals(2, impl.i());
        assertEquals(2, impl.i());

        // @todo: is it worth adding implicit conversion?
        Mockery.when(impl.s()).thenReturn((short) impl.i());
        assertEquals(2, impl.s());
        assertEquals(impl.s(), impl.i());

        Mockery.when(impl.s()).thenAnswer(a -> (short) 3).thenReturn((short) 2);
        assertEquals(3, impl.s());
        assertEquals(2, impl.s());

        Mockery.when(impl.i()).thenThrow(IllegalAccessException.class).thenReturn(100);
        assertThrows(IllegalAccessException.class, impl::i);
        assertEquals(100, impl.i());
    }

    @Test
    void testMockWithCustomDefault() {
        final TestInterface impl = Mockery.mock(TestInterface.class, new DefaultsOverride());
        assertEquals("foo", impl.string());
        assertEquals(100, impl.i());
        assertNull(impl.arr());
        assertEquals(0, impl.s());
        assertEquals(0, impl.c());
        assertEquals(0, impl.b());
        assertEquals(0.0f, impl.f());
        assertEquals(0.0, impl.d());
        assertEquals(0, impl.l());

    }

    private static final class DefaultsOverride implements Defaults {
        @Override
        public <T> T get(final Class<T> clazz) {
            if (String.class.equals(clazz)) {
                return (T) "foo";
            } else if (int.class.equals(clazz)) {
                return (T) Integer.valueOf(100);
            }
            return Impl.IMPL.get(clazz);
        }
    }
}
