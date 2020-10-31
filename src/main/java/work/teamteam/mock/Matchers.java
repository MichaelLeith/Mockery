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
import java.util.List;
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public class Matchers {
    private static final List<Predicate<Object>> REGISTER = new ArrayList<>(4);

    private Matchers() {}

    public static <T> T any() {
        return matches(a -> true);
    }

    public static <T> T isNull() {
        return matches(Objects::isNull);
    }

    public static <T> T any(Class<T> clazz) {
        return matches(a -> a != null && clazz.isAssignableFrom(a.getClass()));
    }

    public static boolean anyBool() {
        return matchesBool(Objects::nonNull);
    }

    public static byte anyByte() {
        return matchesByte(a -> true);
    }

    public static char anyChar() {
        return matchesChar(a -> true);
    }

    public static short anyShort() {
        return matchesShort(a -> true);
    }

    public static int anyInt() {
        return matchesInt(a -> true);
    }

    public static long anyLong() {
        return matchesLong(a -> true);
    }

    public static float anyFloat() {
        return matchesFloat(a -> true);
    }

    public static double anyDouble() {
        return matchesDouble(a -> true);
    }

    public static boolean eq(final boolean t) {
        matchesBool(a -> a == t);
        return t;
    }

    public static byte eq(final byte t) {
        matchesByte(a -> a == t);
        return t;
    }

    public static char eq(final char t) {
        matchesChar(a -> a == t);
        return t;
    }

    public static short eq(final short t) {
        matchesShort(a -> a == t);
        return t;
    }

    public static int eq(final int t) {
        matchesInt(a -> a == t);
        return t;
    }

    public static long eq(final long t) {
        matchesLong(a -> a == t);
        return t;
    }

    public static float eq(final float t) {
        matchesFloat(a -> a == t);
        return t;
    }

    public static double eq(final double t) {
        matchesDouble(a -> a == t);
        return t;
    }

    public static <T> T eq(final T t) {
        matches(t == null ? Objects::isNull : t::equals);
        return t;
    }

    public static <T> T capture(final Capture<T> capture) {
        REGISTER.add(capture::add);
        return Defaults.Impl.IMPL.get(capture.getClazz());
    }

    public static <T> T matches(final Predicate<T> condition) {
        REGISTER.add((Predicate<Object>) condition);
        return null;
    }

    public static boolean matchesBool(final BooleanPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Boolean.class) && condition.test((boolean) i));
        return Defaults.Impl.IMPL.get(boolean.class);
    }

    public static byte matchesByte(final BytePredicate condition) {
        REGISTER.add(i -> typeCheck(i, Byte.class) && condition.test((byte) i));
        return Defaults.Impl.IMPL.get(byte.class);
    }

    public static char matchesChar(final CharPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Character.class) && condition.test((char) i));
        return Defaults.Impl.IMPL.get(char.class);
    }

    public static short matchesShort(final ShortPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Short.class) && condition.test((short) i));
        return Defaults.Impl.IMPL.get(short.class);
    }

    public static int matchesInt(final IntPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Integer.class) && condition.test((int) i));
        return Defaults.Impl.IMPL.get(int.class);
    }

    public static long matchesLong(final LongPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Long.class) && condition.test((long) i));
        return Defaults.Impl.IMPL.get(long.class);
    }

    public static float matchesFloat(final FloatPredicate condition) {
        REGISTER.add(i -> typeCheck(i, Float.class) && condition.test((float) i));
        return Defaults.Impl.IMPL.get(float.class);
    }

    public static double matchesDouble(final DoublePredicate condition) {
        REGISTER.add(i -> typeCheck(i, Double.class) && condition.test((double) i));
        return Defaults.Impl.IMPL.get(double.class);
    }

    private static boolean typeCheck(final Object i, final Class<?> clazz) {
        return i != null && clazz.isAssignableFrom(i.getClass());
    }

    public static List<Predicate<Object>> getMatchers() {
        final List<Predicate<Object>> cpy = new ArrayList<>(REGISTER);
        REGISTER.clear();
        return cpy;
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
