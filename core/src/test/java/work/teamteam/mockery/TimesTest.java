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

package work.teamteam.mockery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimesTest {
    @Test
    void testEq() {
        assertFalse(Times.eq(1).test(2));
        assertTrue(Times.eq(1).test(1));
        assertFalse(Times.eq(1).test(0));
    }

    @Test
    void testGt() {
        assertTrue(Times.gt(1).test(2));
        assertFalse(Times.gt(1).test(1));
        assertFalse(Times.gt(1).test(0));
    }

    @Test
    void testGe() {
        assertTrue(Times.ge(1).test(2));
        assertTrue(Times.ge(1).test(1));
        assertFalse(Times.ge(1).test(0));
    }

    @Test
    void testLt() {
        assertFalse(Times.lt(1).test(2));
        assertFalse(Times.lt(1).test(1));
        assertTrue(Times.lt(1).test(0));
    }

    @Test
    void testLe() {
        assertFalse(Times.le(1).test(2));
        assertTrue(Times.le(1).test(1));
        assertTrue(Times.le(1).test(0));
    }
}
