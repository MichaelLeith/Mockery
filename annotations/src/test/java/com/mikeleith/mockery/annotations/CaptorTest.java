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
import com.mikeleith.mockery.Capture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.mikeleith.mockery.Matchers.any;
import static com.mikeleith.mockery.Matchers.capture;
import static com.mikeleith.mockery.Mockery.verify;
import static com.mikeleith.mockery.Mockery.when;

public class CaptorTest {
    @Captor Capture<String> captor;
    @Mock Example example;

    @Test
    void testCaptor() {
        MockeryInject.inject(this);
        when(example.doSomething(any())).thenReturn(100);
        assertEquals(100, example.doSomething("well"));
        verify(example, 1).doSomething(capture(captor));
        assertEquals("well", captor.tail());
    }

    public interface Example {
        int doSomething(String s);
    }
}
