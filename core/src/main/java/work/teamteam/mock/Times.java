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

import java.util.Objects;
import java.util.function.LongPredicate;

/**
 * Methods to use when verifying
 */
public final class Times {
    private Times() {}

    /**
     * i -> i == count
     * @param count num times
     * @return predicate
     */
    public static LongPredicate eq(final long count) {
        return new Condition(i -> i == count, String.valueOf(count));
    }

    /**
     * i -> i > count
     * @param count num times
     * @return predicate
     */
    public static LongPredicate gt(final long count) {
        return new Condition(i -> i > count, "> " + count);
    }

    /**
     * i -> i >= count
     * @param count num times
     * @return predicate
     */
    public static LongPredicate ge(final long count) {
        return new Condition(i -> i >= count, ">= " + count);
    }

    /**
     * i -> i < count
     * @param count num times
     * @return predicate
     */
    public static LongPredicate lt(final long count) {
        return new Condition(i -> i < count, "< " + count);
    }

    /**
     * i -> i <= count
     * @param count num times
     * @return predicate
     */
    public static LongPredicate le(final long count) {
        return new Condition(i -> i <= count, "<= " + count);
    }

    /**
     * Utility class to provide a LongPredicate with a human readable toString
     */
    public static final class Condition implements LongPredicate {
        final LongPredicate condition;
        final String toString;

        public Condition(final LongPredicate condition, final String toString) {
            this.condition = Objects.requireNonNull(condition);
            this.toString = Objects.requireNonNull(toString);
        }

        @Override
        public boolean test(final long value) {
            return condition.test(value);
        }

        @Override
        public String toString() {
            return toString;
        }
    }
}
