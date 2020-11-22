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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static work.teamteam.mock.Matchers.anyInt;
import static work.teamteam.mock.Mockery.mock;
import static work.teamteam.mock.Mockery.when;

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
        Void vo();
        void arg(final String s);
        void arg(final float f);
        void arg(final double d);
        void arg(final int i);
        void arg(final short s);
        void arg(final byte b);
        void arg(final long l);
        void arg(final double[] arr);
        void arg(final String s, final int i);
        void arg(final Void v);
    }

    @SuppressWarnings("unused")
    public static class Foo {
        private final int i;

        private Foo(int i, int j, int k) {
            this.i = i + j + k;
        }

        public Foo(int i, int j) {
            this.i = i + j;
        }

        public Foo(int i) {
            this.i = i;
        }

        public int getI() {
            return i;
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

    @Test
    void testFinal() {
        assertThrows(RuntimeException.class, () -> mock(Final.class));
    }

    @Test
    void testVerifyThrowsOnFail() {
        assertThrows(RuntimeException.class, () ->
                Mockery.verify(mock(Foo.class), 1).test("woo"));
    }

    @Test
    void testVerify() {
        final Foo impl = mock(Foo.class);
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
    void testVerifyWithoutHistory() {
        final Foo impl = mock(Foo.class, false);
        when(impl.intAcc(anyInt())).thenReturn("woo");
        assertEquals("woo", impl.intAcc(1));
        assertEquals("woo", impl.intAcc(1));
        assertEquals("woo", impl.intAcc(2));
        // only the last call is recorded
        Mockery.verify(impl, 0).intAcc(1);
        Mockery.verify(impl, 0).intAcc(-1);
        Mockery.verify(impl, 1).intAcc(2);
        Mockery.verify(impl, Times.eq(1)).intAcc(2);
        Mockery.verify(impl, Times.ge(1)).intAcc(2);
        Mockery.verify(impl, Times.le(1)).intAcc(2);
    }

    @Test
    void testCapture() {
        final Foo impl = mock(Foo.class);
        impl.intAcc(1);
        impl.intAcc(1);
        impl.intAcc(2);
        final Capture<Integer> capture = Capture.of(int.class);
        Mockery.verify(impl, 2).intAcc(1);
        Mockery.verify(impl, 3).intAcc(Matchers.capture(capture));
        assertEquals(2, capture.tail());
        assertEquals(Arrays.asList(1, 1, 2), capture.captured());
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
        assertEquals(Arrays.asList(1L, 3L), capture1.captured());
        assertEquals(4, capture2.tail());
        assertEquals(Arrays.asList(2, 4), capture2.captured());
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
        assertThrows(RuntimeException.class, () -> when(new Foo(1)));
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
        when(foo.test("woo")).thenReturn("lol");
        assertEquals("lol", foo.test("woo"));
        Mockery.verify(foo, 2).test("woo");
    }

    @Test
    void testSpyClass() {
        final Foo foo = Mockery.spy(Foo.class, 1);
        assertEquals("woo", foo.test("woo"));
        Mockery.verify(foo, 1).test("woo");
        when(foo.test("woo")).thenReturn("lol");
        assertEquals("lol", foo.test("woo"));
        Mockery.verify(foo, 2).test("woo");
    }

    @Test
    void testSpyPrivateConstructor() {
        assertEquals(6, Mockery.spy(Foo.class, 1, 2, 3).getI());
    }

    @Test
    void testSpyWithWrongArgType() {
        assertThrows(RuntimeException.class, () -> Mockery.spy(Foo.class, "lol", 1));
    }

    @Test
    void testSpyWithTooManyArgType() {
        assertThrows(RuntimeException.class, () -> Mockery.spy(Foo.class, 1, 1, 1, 1, 2));
    }

    @Test
    void testMockConcrete() {
        final Foo impl = mock(Foo.class);
        assertNull(impl.test("welp"));
        Mockery.verify(impl, 1).test("welp");
        Mockery.verify(impl, 0).test("woo");
        Mockery.reset(impl);
        Mockery.verify(impl, 0).test("welp");

        when(impl.intAcc("lol", 1L, 0)).thenReturn("welp2");
        when(impl.intAcc("lol", 1L, 1)).thenReturn("welp3");
        assertNull(impl.intAcc("lol", 1L, 0));
        assertEquals("welp3", impl.intAcc("lol", 1L, 1));
        assertNull(impl.intAcc("lol", 1L, 2));
        Mockery.verify(impl, 1).intAcc("lol", 1L, 0);
    }

    @Test
    void testArgumentMatchersWithMixedArgs() {
        final Foo impl = mock(Foo.class);
        assertThrows(RuntimeException.class, () -> {
            when(impl.intAcc(Matchers.any(String.class),
                    1L,
                    Matchers.eq(0))).thenReturn("welp3");
        });
    }

    @Test
    void testArgumentMatchers() {
        final Foo impl = mock(Foo.class);
        when(impl.test(Matchers.any())).thenReturn("welp");
        assertEquals("welp", impl.test("welp"));
        assertEquals("welp", impl.test("lol"));
        assertEquals("welp", impl.test(null));

        when(impl.intAcc(anyInt())).thenReturn("welp");
        assertEquals("welp", impl.intAcc(-1));
        assertEquals("welp", impl.intAcc(0));
        assertEquals("welp", impl.intAcc(1));

        when(impl.intAcc(Matchers.matchesInt(i -> i >= 0))).thenReturn("welp2");
        assertNull(impl.intAcc(-1));
        assertEquals("welp2", impl.intAcc(0));
        assertEquals("welp2", impl.intAcc(1));

        when(impl.intAcc(Matchers.any(String.class),
                Matchers.matchesLong(i -> i >= 0),
                Matchers.eq(0))).thenReturn("welp3");
        assertEquals("welp3", impl.intAcc("foo", 0, 0));
        assertNull(impl.intAcc("foo", -1, 0));
        assertNull(impl.intAcc("foo", 1, 1));
        assertNull(impl.intAcc(null, 1, 0));
    }

    @Test
    void testWhenUsesLast() {
        final Foo impl = mock(Foo.class);
        when(impl.test(Matchers.eq("foo"))).thenReturn("1");
        when(impl.test(Matchers.eq("bar"))).thenReturn("2");
        assertNull(impl.test("foo"));
        assertEquals("2", impl.test("bar"));
        assertNull(impl.test(null));
    }

    @Test
    void testCanUseWhenOnMultipleFunctions() {
        final Foo impl = mock(Foo.class);
        when(impl.intAcc(anyInt())).thenReturn("welp");
        when(impl.test("bar")).thenReturn("2");
        assertEquals("welp", impl.intAcc(-1));
        assertEquals("2", impl.test("bar"));
    }

    @Test
    void testMocksCanBeUsedInWhenThenReturn() {
        final Foo impl = mock(Foo.class);
        when(impl.test(Matchers.any())).thenReturn("foo");
        when(impl.intAcc(anyInt())).thenReturn(impl.test("well"));
        assertEquals("foo", impl.intAcc(-1));
    }

    @Test
    void testMocksCanBeUsedInWhenParameters() {
        final Foo impl = mock(Foo.class);
        when(impl.test(Matchers.any())).thenReturn("foo");
        when(impl.test(impl.test("bar"))).thenReturn("foo");
        assertEquals("foo", impl.test("foo"));
        assertNull(impl.test("bar"));
    }

    @Test
    void testMockReplaces() {
        final Foo impl = mock(Foo.class);
        when(impl.test(Matchers.any())).thenReturn("foo");
        when(impl.test(Matchers.any())).thenReturn("bar");
        assertEquals("bar", impl.test("lol"));

        when(impl.test("welp")).thenReturn("bar");
        assertNull(impl.test("lol"));
        assertEquals("bar", impl.test("welp"));
    }

    @Test
    void testMockInterface() {
        final TestInterface impl = mock(TestInterface.class);
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
        assertNull(impl.vo());
        impl.arg((Void) null);
        Mockery.verify(impl, 1).arg((Void) null);
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

        when(impl.i()).thenReturn(1).thenReturn(2);
        Mockery.verify(impl, 0).i();
        assertEquals(1, impl.i());
        assertEquals(2, impl.i());
        assertEquals(2, impl.i());

        when(impl.s()).thenReturn((short) impl.i());
        assertEquals(2, impl.s());
        assertEquals(impl.s(), impl.i());

        when(impl.s()).thenAnswer(a -> (short) 3).thenReturn((short) 2);
        assertEquals(3, impl.s());
        assertEquals(2, impl.s());

        when(impl.i()).thenThrow(IllegalAccessException.class).thenReturn(100);
        assertThrows(IllegalAccessException.class, impl::i);
        assertEquals(100, impl.i());
    }

    @Test
    void testMockWithCustomDefault() {
        final TestInterface impl = mock(TestInterface.class, new DefaultsOverride());
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
        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(final Class<T> clazz) {
            if (String.class.equals(clazz)) {
                return (T) "foo";
            } else if (int.class.equals(clazz)) {
                return (T) Integer.valueOf(100);
            }
            return (T) Impl.IMPL.get(clazz);
        }
    }

    public interface InterfaceWithDefaultMethod {
        default String get() {
            return "foo";
        }

        String get2();
    }

    public static class ImplementsInterfaceWithDefaultMethod implements InterfaceWithDefaultMethod {
        @Override
        public String get2() {
            return "foo";
        }
    }

    @Test
    void testMockDefaultInterfaceMethod() {
        final InterfaceWithDefaultMethod impl = mock(InterfaceWithDefaultMethod.class);
        assertNull(impl.get());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());

        assertNull(impl.get2());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());
    }

    @Test
    void testMockDefaultImplementsInterfaceWithDefaultMethod() {
        final ImplementsInterfaceWithDefaultMethod impl = mock(ImplementsInterfaceWithDefaultMethod.class);
        assertNull(impl.get());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());

        assertNull(impl.get2());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());
    }

    public static abstract class AbstractClass {
        public String get() {
            return "foo";
        }

        protected abstract String get2();
    }

    public static class ExtendingAbstractClass extends AbstractClass {
        @Override
        public String get2() {
            return "foo";
        }
    }

    public static class Extending extends ExtendingAbstractClass {
        public String get() {
            return "foo2";
        }

        String get3() {
            return "foo";
        }
    }

    @Test
    void testMockAbstractClass() {
        final AbstractClass impl = mock(AbstractClass.class);
        assertNull(impl.get());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());

        assertNull(impl.get2());
        when(impl.get2()).thenReturn("bar");
        assertEquals("bar", impl.get2());
    }

    @Test
    void testMockExtendingAbstractClass() {
        final ExtendingAbstractClass impl = mock(ExtendingAbstractClass.class);
        assertNull(impl.get());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());

        assertNull(impl.get2());
        when(impl.get2()).thenReturn("bar");
        assertEquals("bar", impl.get2());
    }

    @Test
    void testMockExtending() {
        final Extending impl = mock(Extending.class);
        assertNull(impl.get());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());

        assertNull(impl.get2());
        when(impl.get2()).thenReturn("bar");
        assertEquals("bar", impl.get2());

        assertNull(impl.get3());
        when(impl.get3()).thenReturn("bar");
        assertEquals("bar", impl.get3());
    }

    public static class WithFinalMethod {
        public final String get() {
            return "foo";
        }
    }

    @Test
    void testMockFinalMethod() {
        final WithFinalMethod impl = mock(WithFinalMethod.class);
        assertEquals("foo", impl.get());
        assertThrows(NullPointerException.class, () -> when(impl.get()).thenReturn("bar"));
    }

    public static class WithPrivateMethod {
        private String get() {
            return "foo";
        }
    }

    @Test
    void testMockPrivateMethod() {
        final WithPrivateMethod impl = mock(WithPrivateMethod.class);
        assertEquals("foo", impl.get());
        assertThrows(NullPointerException.class, () -> when(impl.get()).thenReturn("bar"));
    }

    public static class WithStaticMethod {
        public static String get() {
            return "foo";
        }
    }

    @Test
    void testMockStaticMethod() {
        final WithStaticMethod impl = mock(WithStaticMethod.class);
        assertEquals("foo", impl.get());
        assertThrows(NullPointerException.class, () -> when(impl.get()).thenReturn("bar"));
    }

    public static class WithNative {
        public native String get();
    }

    @Test
    void testMockNativeMethod() {
        final WithNative impl = mock(WithNative.class);
        assertNull(impl.get());
        when(impl.get()).thenReturn("bar");
        assertEquals("bar", impl.get());
    }

    public interface Target {
        String doSomething();
        String doSomething(final String arg1);
        int doSomethingElse();
    }

    public static class Impl implements Target {
        @Override
        public String doSomething() {
            return "foo";
        }

        @Override
        public String doSomething(final String arg1) {
            return "bar";
        }

        @Override
        public int doSomethingElse() {
            return 0;
        }
    }
}
