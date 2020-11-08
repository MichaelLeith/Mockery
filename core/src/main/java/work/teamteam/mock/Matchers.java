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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * Matching logic so we can specify the return value of mocks when specific parameters are passed.
 * E.g when(foo.callMethod(eq(1), any()).thenReturn(100);
 */
public class Matchers {
    private static final List<Predicate<Object>> REGISTER = new ArrayList<>(4);

    private Matchers() {}

    /**
     * matches all values (including null)
     * @param <T> Type of arg we're matching
     * @return Default return value for T
     */
    public static <T> T any() {
        return matches(a -> true);
    }

    /**
     * matches null
     * @param <T> Type of arg we're matching
     * @return Default return value for T
     */
    public static <T> T isNull() {
        return matches(Objects::isNull);
    }

    /**
     * matches all non-null variables assignable from clazz
     * @param clazz class to match
     * @param <T> Type of arg we're matching
     * @return Default return value for T
     */
    public static <T> T any(Class<T> clazz) {
        return matches(a -> a != null && clazz.isAssignableFrom(a.getClass()));
    }

    /**
     * matches any boolean primitive
     * @return Default return value for booleans
     */
    public static boolean anyBool() {
        return matchesBool(Objects::nonNull);
    }

    /**
     * matches any byte primitive
     * @return Default return value for bytes
     */
    public static byte anyByte() {
        return matchesByte(a -> true);
    }

    /**
     * matches any char primitive
     * @return Default return value for chars
     */
    public static char anyChar() {
        return matchesChar(a -> true);
    }

    /**
     * matches any short primitive
     * @return Default return value for shorts
     */
    public static short anyShort() {
        return matchesShort(a -> true);
    }

    /**
     * matches any int primitive
     * @return Default return value for ints
     */
    public static int anyInt() {
        return matchesInt(a -> true);
    }

    /**
     * matches any long primitive
     * @return Default return value for longs
     */
    public static long anyLong() {
        return matchesLong(a -> true);
    }

    /**
     * matches any float primitive
     * @return Default return value for floats
     */
    public static float anyFloat() {
        return matchesFloat(a -> true);
    }

    /**
     * matches any double primitive
     * @return Default return value for doubles
     */
    public static double anyDouble() {
        return matchesDouble(a -> true);
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for booleans
     */
    public static boolean eq(final boolean t) {
        matchesBool(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for bytes
     */
    public static byte eq(final byte t) {
        matchesByte(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for chars
     */
    public static char eq(final char t) {
        matchesChar(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for shorts
     */
    public static short eq(final short t) {
        matchesShort(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for ints
     */
    public static int eq(final int t) {
        matchesInt(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for longs
     */
    public static long eq(final long t) {
        matchesLong(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for floats
     */
    public static float eq(final float t) {
        matchesFloat(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t
     * @param t primitive to match
     * @return Default return value for doubles
     */
    public static double eq(final double t) {
        matchesDouble(a -> a == t);
        return t;
    }

    /**
     * returns true if the parameter matches t.
     * If {@literal t == null } this is equivalent to Matchers::isNull(),
     * otherwise it checks {@literal param -> t.equals(param) }
     * @param t primitive to match
     * @param <T> generic type we're capturing
     * @return Default return value for T
     */
    public static <T> T eq(final T t) {
        matches(t == null ? Objects::isNull : t::equals);
        return t;
    }

    /**
     * Used alongside verify to extract values of a given parameter.
     *
     * E.g
     * {@code
     * final Capture<Integer> capture = Capture.of(Integer.class);
     * verify(foo, 1).doSomething(capture(capture));
     * assertEquals(100, capture.tail()); // get the last value passed to foo.doSomething(int)
     * }
     *
     * @param capture capture to register
     * @param <T> generic type of this parameter we'll capture
     * @return Default return value for T
     */
    @SuppressWarnings("unchecked")
    public static <T> T capture(final Capture<T> capture) {
        REGISTER.add(capture::add);
        return (T) Defaults.Impl.IMPL.get(capture.getClazz());
    }

    /**
     * only matches parameters that match the condition
     * @param condition condition to match
     * @param <T> generic type of the parameter
     * @return null (we don't have type info to do much else safely)
     */
    @SuppressWarnings("unchecked")
    public static <T> T matches(final Predicate<T> condition) {
        REGISTER.add((Predicate<Object>) condition);
        return null;
    }

    /**
     * only matches boolean parameters that match the condition
     * @param condition condition to match
     * @return Default return value for bools
     */
    public static boolean matchesBool(final BooleanPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Boolean.class) && condition.test((boolean) i));
        return (boolean) Defaults.Impl.IMPL.get(boolean.class);
    }

    /**
     * only matches byte parameters that match the condition
     * @param condition condition to match
     * @return Default return value for bytes
     */
    public static byte matchesByte(final BytePredicate condition) {
        REGISTER.add(i -> typeCheck(i, Byte.class) && condition.test((byte) i));
        return (byte) Defaults.Impl.IMPL.get(byte.class);
    }

    /**
     * only matches char parameters that match the condition
     * @param condition condition to match
     * @return Default return value for chars
     */
    public static char matchesChar(final CharPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Character.class) && condition.test((char) i));
        return (char) Defaults.Impl.IMPL.get(char.class);
    }

    /**
     * only matches short parameters that match the condition
     * @param condition condition to match
     * @return Default return value for shorts
     */
    public static short matchesShort(final ShortPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Short.class) && condition.test((short) i));
        return (short) Defaults.Impl.IMPL.get(short.class);
    }

    /**
     * only matches int parameters that match the condition
     * @param condition condition to match
     * @return Default return value for ints
     */
    public static int matchesInt(final IntPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Integer.class) && condition.test((int) i));
        return (int) Defaults.Impl.IMPL.get(int.class);
    }

    /**
     * only matches long parameters that match the condition
     * @param condition condition to match
     * @return Default return value for longs
     */
    public static long matchesLong(final LongPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Long.class) && condition.test((long) i));
        return (long) Defaults.Impl.IMPL.get(long.class);
    }

    /**
     * only matches float parameters that match the condition
     * @param condition condition to match
     * @return Default return value for floats
     */
    public static float matchesFloat(final FloatPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Float.class) && condition.test((float) i));
        return (float) Defaults.Impl.IMPL.get(float.class);
    }

    /**
     * only matches double parameters that match the condition
     * @param condition condition to match
     * @return Default return value for doubles
     */
    public static double matchesDouble(final DoublePredicate condition) {
        REGISTER.add(i -> typeCheck(i, Double.class) && condition.test((double) i));
        return (double) Defaults.Impl.IMPL.get(double.class);
    }

    private static boolean typeCheck(final Object i, final Class<?> clazz) {
        return i != null && clazz.isAssignableFrom(i.getClass());
    }

    /**
     * INTERNAL: returns the current matchers and resets the global list
     * @return list of matcher predicates used since the last global reset
     */
    public static List<Predicate<Object>> getMatchers() {
        if (!REGISTER.isEmpty()) {
            final List<Predicate<Object>> cpy = new ArrayList<>(REGISTER);
            REGISTER.clear();
            return cpy;
        }
        return Collections.emptyList();
    }

    public interface FloatPredicate {
        boolean test(final float f);
    }

    public interface ShortPredicate {
        boolean test(final short f);
    }

    public interface CharPredicate {
        boolean test(final char f);
    }

    public interface BytePredicate {
        boolean test(final byte f);
    }

    public interface BooleanPredicate {
        boolean test(final boolean f);
    }
}
