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

import org.junit.jupiter.api.Test;
import work.teamteam.mock.Defaults;
import work.teamteam.mock.Times;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VisitorTest {
    @Test
    void testUsesImpl() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, true);
        assertEquals(impl.string(), visitor.run(new ArrayList<>(), "string()Ljava/lang/String;", String.class));
        assertArrayEquals(impl.arr(), (double[]) visitor.run(new ArrayList<>(), "arr()[D", double[].class));
        visitor.run(new ArrayList<>(), "v()V", void.class);
        assertEquals(1, impl.count);
        assertEquals(impl.i(), visitor.run(new ArrayList<>(), "i()I", int.class));
        assertEquals(impl.s(), visitor.run(new ArrayList<>(), "s()S", short.class));
        assertEquals(impl.c(), visitor.run(new ArrayList<>(), "c()C", char.class));
        assertEquals(impl.b(), visitor.run(new ArrayList<>(), "b()B", byte.class));
        assertEquals(impl.f(), visitor.run(new ArrayList<>(), "f()F", float.class));
        assertEquals(impl.l(), visitor.run(new ArrayList<>(), "l()J", long.class));
        assertEquals(impl.d(), visitor.run(new ArrayList<>(), "d()D", double.class));
        assertEquals(impl.bool(), visitor.run(new ArrayList<>(), "bool()Z", boolean.class));

        // test we throw on the wrong return type
        assertThrows(RuntimeException.class, () ->  visitor.run(new ArrayList<>(), "string()Ljava/util/Optional;", int.class));
    }

    @Test
    void testFallbacks() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        assertNull(visitor.run(new ArrayList<>(), "string()Ljava/lang/String;", String.class));
        assertNull(visitor.run(new ArrayList<>(), "arr()[D", double[].class));
        visitor.run(new ArrayList<>(), "v()V", void.class);
        assertEquals(0, visitor.run(new ArrayList<>(), "i()I", int.class));
        assertEquals((short) 0, visitor.run(new ArrayList<>(), "s()S", short.class));
        assertEquals((char) 0, visitor.run(new ArrayList<>(), "c()C", char.class));
        assertEquals((byte) 0, visitor.run(new ArrayList<>(), "b()B", byte.class));
        assertEquals(0.0f, visitor.run(new ArrayList<>(), "f()F", float.class));
        assertEquals(0L, visitor.run(new ArrayList<>(), "l()J", long.class));
        assertEquals(0.0, visitor.run(new ArrayList<>(), "d()D", double.class));
        assertFalse((boolean) visitor.run(new ArrayList<>(), "bool()Z", boolean.class));
    }

    @Test
    void testUsesImplWithArgs() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, true);
        assertEquals(impl.withArgs(), visitor.run(new ArrayList<>(), "withArgs()I", int.class));
        assertEquals(impl.withArgs(2), visitor.run(new ArrayList<>(), "withArgs(I)I", int.class, 2));
        assertEquals(impl.withArgs(2L), visitor.run(new ArrayList<>(), "withArgs(J)I", int.class, 2));
    }

    @Test
    void testUsesCallback() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, true);
        visitor.registerCallback(a -> 10, "withArgs(I)I", Collections.singletonList(i -> (int) i == 1));
        assertEquals(impl.withArgs(), visitor.run(new ArrayList<>(), "withArgs()I", int.class));
        assertEquals(10, visitor.run(new ArrayList<>(), "withArgs(I)I", int.class, 1));
        assertEquals(impl.withArgs(2), visitor.run(new ArrayList<>(), "withArgs(I)I", int.class, 2));
        assertEquals(impl.withArgs(2L), visitor.run(new ArrayList<>(), "withArgs(J)I", int.class, 2));
    }

    @Test
    void testThrowOnBadCallback() {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, true);
        visitor.registerCallback(a -> "sad", "withArgs(I)I", Collections.singletonList(i -> (int) i == 1));
        assertDoesNotThrow(() -> visitor.run(new ArrayList<>(), "withArgs(I)I", int.class, 1));
    }

    @Test
    void testRunWithVerifier() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, true);
        final List<Object[]> hist = visitor.init("withArgs()I");
        assertEquals(impl.withArgs(), visitor.run(hist, "withArgs()I", int.class));
        visitor.setVerification(new Verifier(Times.eq(1)));
        assertEquals(0, visitor.run(hist, "withArgs()I", int.class));
        assertEquals(impl.withArgs(), visitor.run(hist, "withArgs()I", int.class));
    }

    @Test
    void testRunWithVerifierWithoutHistory() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, false);
        final List<Object[]> hist = visitor.init("withArgs()I");
        assertEquals(impl.withArgs(), visitor.run(hist, "withArgs()I", int.class));
        visitor.setVerification(new Verifier(Times.eq(1)));
        assertThrows(RuntimeException.class, () -> visitor.run(hist, "withArgs()I", int.class));
        assertEquals(Collections.emptyList(), hist);
        assertEquals(impl.withArgs(), visitor.run(hist, "withArgs()I", int.class));
    }

    @Test
    void testRunWithVerifierAndCallback() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, true);
        visitor.registerCallback(a -> 2, "withArgs()I", Collections.emptyList());
        final List<Object[]> calls = visitor.init("withArgs()I");
        assertEquals(2,visitor.run(calls, "withArgs()I", int.class));

        visitor.setVerification(new Verifier(Times.eq(1)));
        assertEquals(0, visitor.run(calls, "withArgs()I", int.class));
        assertEquals(2, visitor.run(calls, "withArgs()I", int.class));

        visitor.setVerification(new Verifier(Times.eq(2)));
        assertEquals(0, visitor.run(calls, "withArgs()I", int.class));
        assertEquals(2, visitor.run(calls, "withArgs()I", int.class));
    }

    @Test
    void testWithoutHistory() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL, false);
        final List<Object[]> hist = visitor.init("withArgs()I");
        for (int i = 0; i < 10; i++) {
            visitor.run(hist, "withArgs()I", int.class);
        }
        assertTrue(hist.isEmpty());
    }

    public static final class Impl {
        int count = 0;

        int withArgs() {
            return 1;
        }

        int withArgs(final int i) {
            return i;
        }

        int withArgs(final long i) {
            return (int) i + 1;
        }

        String string() {
            return "s";
        }

        double[] arr() {
            return new double[]{1, 2, 3};
        }

        void v() {
            count++;
        }

        int i() {
            return 1;
        }

        short s() {
            return 2;
        }

        char c() {
            return 3;
        }

        byte b() {
            return 4;
        }

        float f() {
            return 5.0f;
        }

        long l() {
            return 6L;
        }

        double d() {
            return 7.0;
        }

        boolean bool() {
            return true;
        }
    }


    @Test
    void testCallHistoryEquals() {
        final Visitor.CallHistory a = new Visitor.CallHistory();
        a.update(1, 2);
        final Visitor.CallHistory b = new Visitor.CallHistory();
        b.update(1, 2);
        final Visitor.CallHistory c = new Visitor.CallHistory();
        c.update(1);
        final Visitor.CallHistory d = new Visitor.CallHistory();
        d.update(1, 2);
        d.update(1, 2);
        final Visitor.CallHistory e = new Visitor.CallHistory();

        assertNotEquals(a, null);
        assertNotEquals(a, "foo");
        assertEquals(a, a);
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a, d);
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(a, e);
        assertNotEquals(a.hashCode(), e.hashCode());
    }
}
