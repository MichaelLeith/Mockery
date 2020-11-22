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

package com.mikeleith.mockery;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaptureTest {
    @Test
    void test() {
        final Capture<?> capture = Capture.of(String.class);
        assertEquals(String.class, capture.getClazz());
        assertNull(capture.tail());
        assertTrue(capture.captured().isEmpty());

        capture.add("foo");
        assertEquals("foo", capture.tail());
        capture.add("bar");
        assertEquals("bar", capture.tail());
        assertEquals(Arrays.asList("foo", "bar"), capture.captured());
    }

    @Test
    void testCaptureOrdered() {
        final Foo foo = Mockery.mock(Foo.class);
        final Capture<String> capture = Capture.of(String.class);
        foo.doSomething("abc");
        foo.doSomethingElse("welp");
        foo.doSomething("abc");
        foo.doSomething("def");

        Mockery.verify(foo, 3).doSomething(Matchers.capture(capture));
        assertEquals(Arrays.asList("abc", "abc", "def"), capture.captured());
    }

    public interface Foo {
        String doSomething(final String str);
        int doSomethingElse(final String str);
    }
}
