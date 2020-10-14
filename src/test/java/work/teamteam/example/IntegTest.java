package work.teamteam.example;

import org.junit.jupiter.api.Test;
import work.teamteam.mock.Mockery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntegTest {
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
        public Foo(int i) {
        }

        // @note: won't work with package-private, can't override them outside of the same pkg... interesting
        protected String test(final String woo) {
            return woo;
        }
    }

    public static final class Final {}

    @Test
    void testFinal() {
        assertThrows(RuntimeException.class, () -> Mockery.mock(Final.class));
    }

    @Test
    void testMockConcrete() throws Exception {
        final Foo impl = Mockery.mock(Foo.class);
        assertNull(impl.test("welp"));
        Mockery.verify(impl, 1).test("welp");
        Mockery.verify(impl, 0).test("woo");
        Mockery.reset(impl);
        Mockery.verify(impl, 0).test("welp");
    }

    @Test
    void testMockInterface() throws Exception {
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
}
