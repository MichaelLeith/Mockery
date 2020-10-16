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
import java.util.function.Predicate;

// https://www.javadoc.io/doc/org.mockito/mockito-core/2.2.7/org/mockito/ArgumentMatchers.html
public class Matchers {
    private Matchers() {}

    private static List<Predicate<?>> args = new ArrayList<>(4);

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
        return matchesByte(Objects::nonNull);
    }

    public static char anyChar() {
        return matchesChar(Objects::nonNull);
    }

    public static short anyShort() {
        return matchesShort(a -> false);
    }

    public static int anyInt() {
        return matchesInt(Objects::nonNull);
    }

    public static long anyLong() {
        return matchesLong(Objects::nonNull);
    }

    public static float anyFloat() {
        return matchesFloat(Objects::nonNull);
    }

    public static double anyDouble() {
        return matchesDouble(Objects::nonNull);
    }

    public static <T> T eq(final T t) {
        matches(a -> a.equals(t));
        return t;
    }

    public static <T> T matches(final Predicate<T> condition) {
        args.add(condition);
        return null;
    }

    public static boolean matchesBool(final Predicate<Boolean> condition) {
        args.add(condition);
        return false;
    }

    public static byte matchesByte(final Predicate<Byte> condition) {
        args.add(condition);
        return 0;
    }

    public static char matchesChar(final Predicate<Character> condition) {
        args.add(condition);
        return 0;
    }

    public static short matchesShort(final Predicate<Short> condition) {
        args.add(condition);
        return 0;
    }

    public static int matchesInt(final Predicate<Integer> condition) {
        args.add(condition);
        return 0;
    }

    public static long matchesLong(final Predicate<Long> condition) {
        args.add(condition);
        return 0;
    }

    public static float matchesFloat(final Predicate<Float> condition) {
        args.add(condition);
        return 0.0f;
    }

    public static double matchesDouble(final Predicate<Double> condition) {
        args.add(condition);
        return 0.0;
    }

    static List<Predicate<?>> getMatchers() {
        final List<Predicate<?>> cpy = args;
        args = new ArrayList<>(4);
        return cpy;
    }
}
