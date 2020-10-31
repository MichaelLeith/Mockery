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
}
