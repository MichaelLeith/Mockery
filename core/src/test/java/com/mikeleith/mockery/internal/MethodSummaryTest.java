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

package com.mikeleith.mockery.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MethodSummaryTest {
    @Test
    void testEquals() {
        final MethodSummary a = new MethodSummary("foo", "bar", "lol", new String[]{"well"});
        final MethodSummary b = new MethodSummary("foo", "bar", "lol", new String[]{"well"});
        final MethodSummary c = new MethodSummary("foo2", "bar", "lol", new String[]{"well"});
        final MethodSummary d = new MethodSummary("foo", "bar2", "lol", new String[]{"well"});
        final MethodSummary e = new MethodSummary("foo", "bar", "lol2", new String[]{"well"});
        final MethodSummary f = new MethodSummary("foo", "bar", "lol", new String[]{"well2"});

        assertEquals("foo", a.getName());
        assertEquals("bar", a.getDescriptor());
        assertEquals("lol", a.getSignature());
        assertArrayEquals(new String[]{"well"}, a.getExceptions());

        assertNotEquals(a, null);
        assertNotEquals(a, "foo");

        assertEquals(a, a);
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a, e);
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(a, d);
        assertNotEquals(a.hashCode(), e.hashCode());
        // exceptions are ignored
        assertEquals(a, f);
        assertEquals(a.hashCode(), f.hashCode());
    }
}
