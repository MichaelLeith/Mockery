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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VisitorTest {
    @Test
    void testUsesImpl() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL);
        assertEquals(impl.string(), visitor.run("string()Ljava/lang/String;", String.class));
        assertArrayEquals(impl.arr(), (double[]) visitor.run("arr()[D", double[].class));
        visitor.run("v()V", void.class);
        assertEquals(1, impl.count);
        assertEquals(impl.i(), visitor.run("i()I", int.class));
        assertEquals(impl.s(), visitor.run("s()S", short.class));
        assertEquals(impl.c(), visitor.run("c()C", char.class));
        assertEquals(impl.b(), visitor.run("b()B", byte.class));
        assertEquals(impl.f(), visitor.run("f()F", float.class));
        assertEquals(impl.l(), visitor.run("l()J", long.class));
        assertEquals(impl.d(), visitor.run("d()D", double.class));
        assertEquals(impl.bool(), visitor.run("bool()Z", boolean.class));

        // test we throw on the wrong return type
        assertThrows(RuntimeException.class, () ->  visitor.run("string()Ljava/util/Optional;", int.class));
    }

    @Test
    void testFallbacks() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        assertNull(visitor.run("string()Ljava/lang/String;", String.class));
        assertNull(visitor.run("arr()[D", double[].class));
        visitor.run("v()V", void.class);
        assertEquals(0, visitor.run("i()I", int.class));
        assertEquals((short) 0, visitor.run("s()S", short.class));
        assertEquals((char) 0, visitor.run("c()C", char.class));
        assertEquals((byte) 0, visitor.run("b()B", byte.class));
        assertEquals(0.0f, visitor.run("f()F", float.class));
        assertEquals(0L, visitor.run("l()J", long.class));
        assertEquals(0.0, visitor.run("d()D", double.class));
        assertFalse((boolean) visitor.run("bool()Z", boolean.class));
    }

    @Test
    void testUsesImplWithArgs() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL);
        assertEquals(impl.withArgs(), visitor.run("withArgs()I", int.class));
        assertEquals(impl.withArgs(2), visitor.run("withArgs(I)I", int.class, 2));
        assertEquals(impl.withArgs(2L), visitor.run("withArgs(J)I", int.class, 2));
    }

    @Test
    void testUsesCallback() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL);
        visitor.registerCallback(a -> 10, "withArgs(I)I", Collections.singletonList(i -> (int) i == 1));
        assertEquals(impl.withArgs(), visitor.run("withArgs()I", int.class));
        assertEquals(10, visitor.run("withArgs(I)I", int.class, 1));
        assertEquals(impl.withArgs(2), visitor.run("withArgs(I)I", int.class, 2));
        assertEquals(impl.withArgs(2L), visitor.run("withArgs(J)I", int.class, 2));
    }

    @Test
    void testThrowOnBadCallback() {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL);
        visitor.registerCallback(a -> "sad", "withArgs(I)I", Collections.singletonList(i -> (int) i == 1));
        assertDoesNotThrow(() -> visitor.run("withArgs(I)I", int.class, 1));
    }

    @Test
    void testRunWithVerifier() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL);
        assertEquals(impl.withArgs(), visitor.run("withArgs()I", int.class));
        visitor.setVerification(new Verifier(Times.eq(1)));
        assertEquals(0, visitor.run("withArgs()I", int.class));
        assertEquals(impl.withArgs(), visitor.run("withArgs()I", int.class));
    }

    @Test
    void testRunWithVerifierAndCallback() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl, Defaults.Impl.IMPL);
        visitor.registerCallback(a -> 2, "withArgs()I", Collections.emptyList());
        assertEquals(2,visitor.run("withArgs()I", int.class));
        visitor.setVerification(new Verifier(Times.eq(1)));
        assertEquals(0, visitor.run("withArgs()I", int.class));
        assertEquals(2, visitor.run("withArgs()I", int.class));
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
}
