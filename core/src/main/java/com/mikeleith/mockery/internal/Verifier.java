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

package com.mikeleith.mockery.internal;

import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Used to verify how many times a method was called
 */
public class Verifier {
    private final IntPredicate numCalls;

    public Verifier(final IntPredicate numCalls) {
        this.numCalls = Objects.requireNonNull(numCalls);
    }

    /**
     * Checks that the given key and matchers (or args) were seen by the tracker the specified number of times
     * @param visitor visitor to get data from
     * @param key method name + description to test
     * @param matchers optional matchers to check, if empty args are used instead
     * @param history history to use if any matchers are given
     * @param args raw arguments to check for
     */
    public void verify(final Visitor<?> visitor,
                       final String key,
                       final Predicate<Object>[] matchers,
                       final List<Object[]> history,
                       final Object... args) {
        int calls;
        if (matchers == null) {
            calls = visitor.get(key, args);
        } else if (matchers.length != args.length) {
            throw new RuntimeException("Not all arguments mocked, you must use eq for literals with Matchers");
        } else {
            calls = 0;
            /*
             * finds the number of times the key was called with arguments that match their respective predicate,
             * e.g args.get(0) is tested against arg 0, .get(1) against 1 and so forth.
             * @param key method name + description
             * @param args list of per-argument predicates to match
             * @return the number of matches
             */
            for (int i = 0; i < history.size(); i++) {
                if (history.get(i).length == args.length && matches(matchers, history.get(i))) {
                    calls++;
                }
            }

        }
        if (!numCalls.test(calls)) {
            throw new RuntimeException("expected " + numCalls.toString() + ", but was called " + calls + " times");
        }
    }

    private static boolean matches(final Predicate<Object>[] conditions, final Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (!conditions[i].test(args[i])) {
                return false;
            }
        }
        return true;
    }
}
