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

public class Verifier {
    private final long numCalls;

    public Verifier(final long numCalls) {
        this.numCalls = numCalls;
    }

    public void verify(final Tracker tracker,
                       final String key,
                       List<Predicate<Object>> matchers,
                       final Object... args) {
        if (matchers.isEmpty()) {
            final long calls = tracker.get(key, args);
            if (numCalls != calls) {
                throw new RuntimeException("expected " + numCalls + ", but was called " + calls + " times");
            }
            return;
        } else if (matchers.size() != args.length) {
            throw new RuntimeException("Not all arguments mocked, you must use eq for literals with Matchers");
        }
        // get matches from the tracker
        final long calls = tracker.getMatches(key, matchers);
        if (numCalls != calls) {
            throw new RuntimeException("expected " + numCalls + ", but was called " + calls + " times");
        }
    }
}
