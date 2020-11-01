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

import work.teamteam.mock.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class tracking the call history of a mock/spy
 */
public final class Tracker {
    // global state to support verify/when syntax.
    // as these methods don't directly receive the mock object we need some global state to record who was last touched
    // e.g when(foo.something(bar).doReturn(...)); will be tracking foo because it was last called
    private static volatile Visitor<?> lastCall = null;
    private final List<Visitor.Description> callHistory = new ArrayList<>();
    private final Map<String, CallHistory> callHistories = new HashMap<>();
    private int size = 0;

    /**
     * Called when a method is hit. Adds the key + args to the call history + records the last visitor.
     * Note that we store references to arguments, and do not deep copy. This means that if they are
     * modified it will propagate through history, so they could become unreliable.
     *
     * TODO: support optional deep copying
     * @param visitor visitor used
     * @param key description of the method call. (internally) method name + signature
     * @param args arguments passed to the method.
     */
    public void visit(final Visitor<?> visitor, final String key, final Object... args) {
        lastCall = visitor;
        // @todo: improve our concurrency. global lock = bad :/
        synchronized (this) {
            callHistory.add(new Visitor.Description(key, args));
        }
    }

    /**
     * Removes the last call from the history & creates a new Mock from it
     * @return a Mock based on the last call in the global history
     */
    public static Mock rollbackLast() {
        final List<Visitor.Description> hist = lastCall.getTracker().callHistory;
        return new Mock(lastCall, hist.remove(hist.size() - 1));
    }

    /**
     * Converts the call history into a Map<MethodName+Description, CallHistory> to make lookup easier
     * at the expense of absolute ordering
     * @return reformatted call history
     */
    public Map<String, CallHistory> collect() {
        synchronized (this) {
            if (!callHistory.isEmpty() && size != callHistory.size()) {
                size = callHistory.size();
                callHistories.clear();
                for (final Visitor.Description description : callHistory) {
                    CallHistory hist = callHistories.get(description.getKey());
                    if (hist == null) {
                        callHistories.put(description.getKey(), new CallHistory(description.getArgs()));
                    } else {
                        hist.update(description.getArgs());
                    }
                }
            }
        }
        return callHistories;
    }

    /**
     * Returns the number of times the key + args combination was called
     * @param key method name + description
     * @param args arguments used
     * @return number of calls to this combination
     */
    public long get(final String key, final Object... args) {
        final CallHistory hist = collect().get(key);
        return hist == null ? 0L : hist.get(args);
    }

    /**
     * Returns the number of times the key was called with arguments that match their respective predicate,
     * e.g args.get(0) is tested against arg 0, .get(1) against 1 and so forth.
     * @param key method name + description
     * @param args list of per-argument predicates to match
     * @return the number of matches
     */
    public long getMatches(final String key, final List<Predicate<Object>> args) {
        final CallHistory hist = collect().get(key);
        if (hist == null) {
            return 0L;
        }
        long numMatches = 0;
        // @todo: doesn't work completely because we don't retain order
        for (final Map.Entry<List<Object>, Long> entry: hist.perArgset.entrySet()) {
            if (entry.getKey().size() == args.size() && matches(args, entry.getKey())) {
                numMatches += entry.getValue();
            }
        }
        return numMatches;
    }

    private boolean matches(final List<Predicate<Object>> conditions, final List<Object> args) {
        for (int i = 0; i < args.size(); i++) {
            if (!conditions.get(i).test(args.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resets the trackers call history. This should be used as often as possible
     * as recorded history is unbounded and grows linearly with mock method calls
     */
    public void reset() {
        synchronized (this) {
            callHistory.clear();
            callHistories.clear();
        }
    }

    /**
     * Resets the last called tracker. Used primarily for unit tests (see Mockery.reset())
     */
    public static void resetLast() {
        if (lastCall != null) {
            lastCall.reset();
            lastCall = null;
        }
    }

    /**
     * Count of calls to a method with various args, provides a simpler access pattern than the raw history.
     * Within the specific CallHistory we retain the order at which specific arguments are first seen.
     * Subsequent calls are not available however, so if you need to test interlacing you must use the raw history.
     */
    // @todo: thread safety?
    public static final class CallHistory {
        private final Map<List<Object>, Long> perArgset;

        public CallHistory(final Object... args) {
            // using a LinkedHashMap map to ensure ordering, we should rethink this
            perArgset = new LinkedHashMap<>();
            perArgset.put(Arrays.asList(args), 1L);
        }

        /**
         * Adds or updates the call history with the given args
         * @param args list of args for a specific method call
         */
        public void update(final Object... args) {
            final List<Object> wrapper = Arrays.asList(args);
            final Long l = perArgset.get(wrapper);
            perArgset.put(wrapper, l == null ? 1L : l + 1);
        }

        /**
         * Returns the number of times the CallHistory has seen the specific set of arguments
         * @param args list of args to check for
         * @return number of times these have been seen
         */
        public long get(final Object... args) {
            return perArgset.getOrDefault(Arrays.asList(args), 0L);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CallHistory that = (CallHistory) o;
            return perArgset.equals(that.perArgset);
        }

        @Override
        public int hashCode() {
            return Objects.hash(perArgset);
        }
    }
}
