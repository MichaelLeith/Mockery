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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static com.mikeleith.mockery.Mockery.when;

public class MockTest {
    @Mock Example example;

    @Test
    void testMock() {
        assertNull(example);
        MockeryInject.inject(this);
        assertNotNull(example);
        when(example.doSomething()).thenReturn(100);
        assertEquals(100, example.doSomething());
    }

    @Test
    void testMockPrivate() {
        final PrivateMock target = new PrivateMock();
        MockeryInject.inject(target);
        assertNotNull(target.example);
    }

    @Test
    void testMockInherited() {
        final InheritedMock target = new InheritedMock();
        MockeryInject.inject(target);
        assertNotNull(target.example);
    }

    public interface Example {
        int doSomething();
    }

    public class PrivateMock {
        @Mock private Example example;
    }

    public class InheritedMock extends MockTest {

    }
}
