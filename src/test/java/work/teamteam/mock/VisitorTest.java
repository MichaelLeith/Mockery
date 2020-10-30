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

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VisitorTest {
    @Test
    void testUsesImpl() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl);
        assertEquals(impl.string(), visitor.invokeL("string()Ljava/lang/String;"));
        assertArrayEquals(impl.arr(), (double[]) visitor.invokeL("arr()[D"));
        visitor.invokeV("v()V");
        assertEquals(1, impl.count);
        assertEquals(impl.i(), visitor.invokeI("i()I"));
        assertEquals(impl.s(), visitor.invokeS("s()S"));
        assertEquals(impl.c(), visitor.invokeC("c()C"));
        assertEquals(impl.b(), visitor.invokeB("b()B"));
        assertEquals(impl.f(), visitor.invokeF("f()F"));
        assertEquals(impl.l(), visitor.invokeJ("l()J"));
        assertEquals(impl.d(), visitor.invokeD("d()D"));
        assertEquals(impl.bool(), visitor.invokeZ("bool()Z"));

        // test we throw on the wrong return type
        assertThrows(RuntimeException.class, () ->  visitor.invokeL("string()Ljava/util/Optional;"));
        assertThrows(RuntimeException.class, () -> visitor.invokeL("arr()[I"));
        assertThrows(RuntimeException.class, () -> visitor.invokeV("v()S"));
        assertEquals(1, impl.count);
        assertThrows(RuntimeException.class, () ->  visitor.invokeI("i()F"));
        assertThrows(RuntimeException.class, () ->  visitor.invokeS("s()J"));
        assertThrows(RuntimeException.class, () ->  visitor.invokeC("c()V"));
        assertThrows(RuntimeException.class, () ->  visitor.invokeB("b()D"));
        assertThrows(RuntimeException.class, () -> visitor.invokeF("f()Z"));
        assertThrows(RuntimeException.class, () ->  visitor.invokeJ("l()I"));
        assertThrows(RuntimeException.class, () -> visitor.invokeD("d()I"));
        assertThrows(RuntimeException.class, () ->  visitor.invokeZ("bool()D"));
    }

    @Test
    void testFallbacks() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null);
        assertNull(visitor.invokeL("string()Ljava/lang/String;"));
        assertNull(visitor.invokeL("arr()[D"));
        visitor.invokeV("v()V");
        assertEquals(0, visitor.invokeI("i()I"));
        assertEquals(0, visitor.invokeS("s()S"));
        assertEquals(0, visitor.invokeC("c()C"));
        assertEquals(0, visitor.invokeB("b()B"));
        assertEquals(0.0f, visitor.invokeF("f()F"));
        assertEquals(0L, visitor.invokeJ("l()J"));
        assertEquals(0.0, visitor.invokeD("d()D"));
        assertFalse(visitor.invokeZ("bool()Z"));
    }

    @Test
    void testUsesImplWithArgs() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl);
        assertEquals(impl.withArgs(), visitor.invokeI("withArgs()I"));
        assertEquals(impl.withArgs(2), visitor.invokeI("withArgs(I)I", 2));
        assertEquals(impl.withArgs(2L), visitor.invokeI("withArgs(J)I", 2));
    }

    @Test
    void testUsesCallback() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl);
        visitor.registerCallback(a -> 10, "withArgs(I)I", Collections.singletonList(i -> (int) i == 1));
        assertEquals(impl.withArgs(), visitor.invokeI("withArgs()I"));
        assertEquals(10, visitor.invokeI("withArgs(I)I", 1));
        assertEquals(impl.withArgs(2), visitor.invokeI("withArgs(I)I", 2));
        assertEquals(impl.withArgs(2L), visitor.invokeI("withArgs(J)I", 2));
    }

    @Test
    void testThrowOnBadCallback() {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl);
        visitor.registerCallback(a -> "sad", "withArgs(I)I", Collections.singletonList(i -> (int) i == 1));
        assertThrows(RuntimeException.class, () -> visitor.invokeI("withArgs(I)I", 1));
    }

    @Test
    void testRunWithVerifier() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl);
        assertEquals(impl.withArgs(), visitor.invokeI("withArgs()I"));
        visitor.setVerification(new Verifier(1));
        assertEquals(0, visitor.invokeI("withArgs()I"));
        assertEquals(impl.withArgs(), visitor.invokeI("withArgs()I"));
    }

    @Test
    void testRunWithVerifierAndCallback() throws Throwable {
        final Impl impl = new Impl();
        final Visitor<?> visitor = new Visitor<>(impl);
        visitor.registerCallback(a -> 2, "withArgs()I", Collections.emptyList());
        assertEquals(2, visitor.invokeI("withArgs()I"));
        visitor.setVerification(new Verifier(1));
        assertEquals(0, visitor.invokeI("withArgs()I"));
        assertEquals(2, visitor.invokeI("withArgs()I"));
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
