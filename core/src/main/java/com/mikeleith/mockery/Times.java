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

import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * Methods to use when verifying
 */
public final class Times {
    private Times() {}

    /**
     * {@literal i -> i == count}
     * @param count num times
     * @return predicate
     */
    public static IntPredicate eq(final int count) {
        return new Condition(i -> i == count, String.valueOf(count));
    }

    /**
     * {@literal i -> i > count}
     * @param count num times
     * @return predicate
     */
    public static IntPredicate gt(final int count) {
        return new Condition(i -> i > count, "> " + count);
    }

    /**
     * {@literal i -> i >= count}
     * @param count num times
     * @return predicate
     */
    public static IntPredicate ge(final int count) {
        return new Condition(i -> i >= count, ">= " + count);
    }

    /**
     * {@literal i -> i < count}
     * @param count num times
     * @return predicate
     */
    public static IntPredicate lt(final int count) {
        return new Condition(i -> i < count, "< " + count);
    }

    /**
     * {@literal i -> i <= count}
     * @param count num times
     * @return predicate
     */
    public static IntPredicate le(final int count) {
        return new Condition(i -> i <= count, "<= " + count);
    }

    /**
     * Utility class to provide a IntPredicate with a human readable toString
     */
    public static final class Condition implements IntPredicate {
        final IntPredicate condition;
        final String toString;

        public Condition(final IntPredicate condition, final String toString) {
            this.condition = Objects.requireNonNull(condition);
            this.toString = Objects.requireNonNull(toString);
        }

        @Override
        public boolean test(final int value) {
            return condition.test(value);
        }

        @Override
        public String toString() {
            return toString;
        }
    }
}
