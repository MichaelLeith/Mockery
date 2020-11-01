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

package work.teamteam.mock.internal;

import java.util.List;
import java.util.Objects;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * Used to verify how many times a method was called
 */
public class Verifier {
    private final LongPredicate numCalls;

    public Verifier(final LongPredicate numCalls) {
        this.numCalls = Objects.requireNonNull(numCalls);
    }

    /**
     * Checks that the given key & matchers (or args) were seen by the tracker the specified number of times
     * @param tracker tracker to get data from
     * @param key method name + description to test
     * @param matchers optional matchers to check, if empty args are used instead
     * @param args raw arguments to check for
     */
    public void verify(final Tracker tracker,
                       final String key,
                       List<Predicate<Object>> matchers,
                       final Object... args) {
        final long calls;
        if (matchers.isEmpty()) {
            calls = tracker.get(key, args);
        } else if (matchers.size() != args.length) {
            throw new RuntimeException("Not all arguments mocked, you must use eq for literals with Matchers");
        } else {
            calls = tracker.getMatches(key, matchers);
        }
        if (!numCalls.test(calls)) {
            throw new RuntimeException("expected " + numCalls.toString() + ", but was called " + calls + " times");
        }
    }
}
