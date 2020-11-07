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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchersTest {
    @BeforeEach
    void setUp() {
        // reset all global state
        Matchers.getMatchers();
    }

    @ParameterizedTest
    @EnumSource(Match.class)
    void testMatcher(final Match match) {
        Matchers.getMatchers();
        match.fn.get();
        final List<Predicate<Object>> predicates = Matchers.getMatchers();
        assertEquals(match.matches, predicates.get(0).test(match.obj));
    }

    @SuppressWarnings({"unused", "boxing"})
    enum Match {
        // any
        ANY_NULL(Matchers::any, null, true),
        ANY_OBJ(Matchers::any, "foo", true),
        ANY_OBJ2(Matchers::any, Collections.emptyList(), true),
        // any Class
        ANY_CLASS_NULL(() -> Matchers.any(String.class), null, false),
        ANY_CLASS_OBJ(() -> Matchers.any(String.class), "foo", true),
        ANY_CLASS_OBJ2(() -> Matchers.any(String.class), Collections.emptyList(), false),
        ANY_CLASS_BOXED(() -> Matchers.any(Integer.class), 1, true),
        ANY_CLASS_PRIMITIVE(() -> Matchers.any(int.class), 1, false), // false due to boxing
        // isNull
        IS_NULL(Matchers::isNull, null, true),
        IS_NULL_OBJ(Matchers::isNull, "foo", false),
        IS_NULL_OBJ2(Matchers::isNull, Collections.emptyList(), false),
        // anyBool
        ANY_BOOL_NULL(Matchers::anyBool, null, false),
        ANY_BOOL_OBJ(Matchers::anyBool, "foo", false),
        ANY_BOOL_OBJ2(Matchers::anyBool, Collections.emptyList(), false),
        ANY_BOOL_PRIMITIVE(Matchers::anyBool, true, true),
        ANY_BOOL_BOXED(Matchers::anyBool, Boolean.FALSE, true),
        // anyByte
        ANY_BYTE_NULL(Matchers::anyByte, null, false),
        ANY_BYTE_OBJ(Matchers::anyByte, "foo", false),
        ANY_BYTE_OBJ2(Matchers::anyByte, Collections.emptyList(), false),
        ANY_BYTE_INT(Matchers::anyByte, 1, false),
        ANY_BYTE_PRIMITIVE(Matchers::anyByte, (byte) 1, true),
        ANY_BYTE_BOXED(Matchers::anyByte, Byte.valueOf((byte) 1), true),
        // anyChar
        ANY_CHAR_NULL(Matchers::anyChar, null, false),
        ANY_CHAR_OBJ(Matchers::anyChar, "foo", false),
        ANY_CHAR_OBJ2(Matchers::anyChar, Collections.emptyList(), false),
        ANY_CHAR_PRIMITIVE(Matchers::anyChar, 'c', true),
        ANY_CHAR_BOXED(Matchers::anyChar, Character.valueOf('c'), true),
        // anyShort
        ANY_SHORT_NULL(Matchers::anyShort, null, false),
        ANY_SHORT_OBJ(Matchers::anyShort, "foo", false),
        ANY_SHORT_OBJ2(Matchers::anyShort, Collections.emptyList(), false),
        ANY_SHORT_INT(Matchers::anyShort, 1, false),
        ANY_SHORT_PRIMITIVE(Matchers::anyShort, (short) 1, true),
        ANY_SHORT_BOXED(Matchers::anyShort, Short.valueOf((short) 1), true),
        // anyInt
        ANY_INT_NULL(Matchers::anyInt, null, false),
        ANY_INT_OBJ(Matchers::anyInt, "foo", false),
        ANY_INT_OBJ2(Matchers::anyInt, Collections.emptyList(), false),
        ANY_INT_PRIMITIVE(Matchers::anyInt, 1, true),
        ANY_INT_BOXED(Matchers::anyInt, Integer.valueOf(1), true),
        // anyLong
        ANY_LONG_NULL(Matchers::anyLong, null, false),
        ANY_LONG_OBJ(Matchers::anyLong, "foo", false),
        ANY_LONG_OBJ2(Matchers::anyLong, Collections.emptyList(), false),
        ANY_LONG_INT(Matchers::anyLong, 1, false),
        ANY_LONG_PRIMITIVE(Matchers::anyLong, 1L, true),
        ANY_LONG_BOXED(Matchers::anyLong, Long.valueOf(1), true),
        // anyFloat
        ANY_FLOAT_NULL(Matchers::anyFloat, null, false),
        ANY_FLOAT_OBJ(Matchers::anyFloat, "foo", false),
        ANY_FLOAT_OBJ2(Matchers::anyFloat, Collections.emptyList(), false),
        ANY_FLOAT_INT(Matchers::anyFloat, 1, false),
        ANY_FLOAT_PRIMITIVE(Matchers::anyFloat, 1.f, true),
        ANY_FLOAT_BOXED(Matchers::anyFloat, Float.valueOf(1.f), true),
        // anyDouble
        ANY_DOUBLE_NULL(Matchers::anyDouble, null, false),
        ANY_DOUBLE_OBJ(Matchers::anyDouble, "foo", false),
        ANY_DOUBLE_OBJ2(Matchers::anyDouble, Collections.emptyList(), false),
        ANY_DOUBLE_INT(Matchers::anyDouble, 1, false),
        ANY_DOUBLE_PRIMITIVE(Matchers::anyDouble, 1., true),
        ANY_DOUBLE_BOXED(Matchers::anyDouble, Double.valueOf(1.), true),
        // eq boolean
        EQ_BOOLEAN_NULL(() -> Matchers.eq(true), null, false),
        EQ_BOOLEAN_OBJ(() -> Matchers.eq(true), "foo", false),
        EQ_BOOLEAN_INT(() -> Matchers.eq(true), 1, false),
        EQ_BOOLEAN_PRIMITIVE(() -> Matchers.eq(true), true, true),
        EQ_BOOLEAN_PRIMITIVE_FALSE(() -> Matchers.eq(true), false, false),
        EQ_BOOLEAN_BOXED(() -> Matchers.eq(true), Boolean.TRUE, true),
        // eq byte
        EQ_BYTE_NULL(() -> Matchers.eq((byte) 1), null, false),
        EQ_BYTE_OBJ(() -> Matchers.eq((byte) 1), "foo", false),
        EQ_BYTE_INT(() -> Matchers.eq((byte) 1), 1, false),
        EQ_BYTE_PRIMITIVE(() -> Matchers.eq((byte) 1), (byte) 1, true),
        EQ_BYTE_PRIMITIVE_FALSE(() -> Matchers.eq((byte) 1), (byte) 2, false),
        EQ_BYTE_BOXED(() -> Matchers.eq((byte) 1), Byte.valueOf((byte) 1), true),
        // eq char
        EQ_CHAR_NULL(() -> Matchers.eq((char) 1), null, false),
        EQ_CHAR_OBJ(() -> Matchers.eq((char) 1), "foo", false),
        EQ_CHAR_INT(() -> Matchers.eq((char) 1), 1, false),
        EQ_CHAR_PRIMITIVE(() -> Matchers.eq((char) 1), (char) 1, true),
        EQ_CHAR_PRIMITIVE_FALSE(() -> Matchers.eq((char) 1), (char) 2, false),
        EQ_CHAR_BOXED(() -> Matchers.eq((char) 1), Character.valueOf((char) 1), true),
        // eq short
        EQ_SHORT_NULL(() -> Matchers.eq((short) 1), null, false),
        EQ_SHORT_OBJ(() -> Matchers.eq((short) 1), "foo", false),
        EQ_SHORT_INT(() -> Matchers.eq((short) 1), 1, false),
        EQ_SHORT_PRIMITIVE(() -> Matchers.eq((short) 1), (short) 1, true),
        EQ_SHORT_PRIMITIVE_FALSE(() -> Matchers.eq((short) 1), (short) 2, false),
        EQ_SHORT_BOXED(() -> Matchers.eq((short) 1), Short.valueOf((short) 1), true),
        // eq int
        EQ_INT_NULL(() -> Matchers.eq(1), null, false),
        EQ_INT_OBJ(() -> Matchers.eq(1), "foo", false),
        EQ_INT_SHORT(() -> Matchers.eq(1), (short) 1, false),
        EQ_INT_PRIMITIVE(() -> Matchers.eq(1), 1, true),
        EQ_INT_PRIMITIVE_FALSE(() -> Matchers.eq(1), 2, false),
        EQ_INT_BOXED(() -> Matchers.eq(1), Integer.valueOf(1), true),
        // eq long
        EQ_LONG_NULL(() -> Matchers.eq(1L), null, false),
        EQ_LONG_OBJ(() -> Matchers.eq(1L), "foo", false),
        EQ_LONG_SHORT(() -> Matchers.eq(1L), (short) 1, false),
        EQ_LONG_PRIMITIVE(() -> Matchers.eq(1L), 1L, true),
        EQ_LONG_PRIMITIVE_FALSE(() -> Matchers.eq(1L), 2L, false),
        EQ_LONG_BOXED(() -> Matchers.eq(1L), Long.valueOf(1L), true),
        // eq float
        EQ_FLOAT_NULL(() -> Matchers.eq(1.f), null, false),
        EQ_FLOAT_OBJ(() -> Matchers.eq(1.f), "foo", false),
        EQ_FLOAT_SHORT(() -> Matchers.eq(1.f), (short) 1, false),
        EQ_FLOAT_PRIMITIVE(() -> Matchers.eq(1.f), 1.f, true),
        EQ_FLOAT_PRIMITIVE_FALSE(() -> Matchers.eq(1.f), 2.f, false),
        EQ_FLOAT_BOXED(() -> Matchers.eq(1.f), Float.valueOf(1.f), true),
        // eq double
        EQ_DOUBLE_NULL(() -> Matchers.eq(1.), null, false),
        EQ_DOUBLE_OBJ(() -> Matchers.eq(1.), "foo", false),
        EQ_DOUBLE_SHORT(() -> Matchers.eq(1.), (short) 1, false),
        EQ_DOUBLE_PRIMITIVE(() -> Matchers.eq(1.), 1., true),
        EQ_DOUBLE_PRIMITIVE_FALSE(() -> Matchers.eq(1.), 2., false),
        EQ_DOUBLE_BOXED(() -> Matchers.eq(1.), Double.valueOf(1.), true),
        // eq object
        EQ_NULL_NULL(() -> Matchers.eq(null), null, true),
        EQ_NULL_OBJ(() -> Matchers.eq(null), "foo", false),
        EQ_NULL(() -> Matchers.eq("foo"), null, false),
        EQ_OBJ(() -> Matchers.eq("foo"), "foo", true),
        EQ_OBJ_FALSE(() -> Matchers.eq("foo"), "foob", false),
        EQ_PRIMITIVE(() -> Matchers.eq("foo"), 1., false),
        ;

        private final Supplier<Object> fn;
        private final Object obj;
        private final boolean matches;

        Match(final Supplier<Object> fn, final Object obj, final boolean matches) {
            this.fn = fn;
            this.obj = obj;
            this.matches = matches;
        }
    }
}
