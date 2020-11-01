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
import work.teamteam.mock.internal.Visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MockTest {
    @Test
    void testMockFailsWithMissingArgs() {
        assertTrue(Matchers.getMatchers().isEmpty());
        Matchers.any();
        assertThrows(RuntimeException.class, () -> new Mock(new Visitor<>(null, Defaults.Impl.IMPL),
                new Visitor.Description("foo", 1, null)));
    }

    @Test
    void testMockWithoutMatchers() throws Throwable {
        assertTrue(Matchers.getMatchers().isEmpty());
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Mock mock = new Mock(visitor,
                new Visitor.Description("foo", 1, null));
        assertNull(visitor.run("foo", Object.class, 1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));

        mock.thenReturn(100);
        assertEquals(100, visitor.run("foo", Object.class, 1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));
    }

    @Test
    void testMockRepeatsLast() throws Throwable {
        assertTrue(Matchers.getMatchers().isEmpty());
        Matchers.eq(1);
        Matchers.any();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Mock mock = new Mock(visitor,
                new Visitor.Description("foo", 1, null));
        assertNull(visitor.run("foo", Object.class, 1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));

        mock.thenReturn(1).thenReturn(100);
        assertEquals(1, visitor.run("foo", Object.class, 1, null));
        for (int i = 0; i < 100; i++) {
            assertEquals(100, visitor.run("foo", Object.class, 1, null));
        }
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));
    }

    @Test
    void testMockThenReturn() throws Throwable {
        assertTrue(Matchers.getMatchers().isEmpty());
        Matchers.eq(1);
        Matchers.any();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Mock mock = new Mock(visitor,
                new Visitor.Description("foo", 1, null));

        mock.thenReturn(100);
        assertEquals(100, visitor.run("foo", Object.class, 1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));
    }

    @Test
    void testMockThenAnswer() throws Throwable {
        assertTrue(Matchers.getMatchers().isEmpty());
        Matchers.eq(1);
        Matchers.any();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Mock mock = new Mock(visitor,
                new Visitor.Description("foo", 1, null));

        mock.thenAnswer(i -> {
            assertEquals(1, i[0]);
            assertNull(i[1]);
            return 100;
        });
        assertEquals(100, visitor.run("foo", Object.class, 1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));
    }

    @Test
    void testMockThenThrow() throws Throwable {
        assertTrue(Matchers.getMatchers().isEmpty());
        Matchers.eq(1);
        Matchers.any();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Mock mock = new Mock(visitor,
                new Visitor.Description("foo", 1, null));

        mock.thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> visitor.run("foo", Object.class, 1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));
    }

    @Test
    void testMockThenThrowClass() throws Throwable {
        assertTrue(Matchers.getMatchers().isEmpty());
        Matchers.eq(1);
        Matchers.any();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Mock mock = new Mock(visitor,
                new Visitor.Description("foo", 1, null));

        mock.thenThrow(IllegalAccessException.class);
        assertThrows(IllegalAccessException.class, () -> visitor.run("foo", Object.class,1, null));
        assertNull(visitor.run("bar", Object.class, 1, null));
        assertNull(visitor.run("foo", Object.class, 2, null));
    }
}