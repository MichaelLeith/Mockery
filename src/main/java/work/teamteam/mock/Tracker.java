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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class Tracker {
    private static volatile Visitor<?> lastCall = null;
    private final List<Visitor.Description> callHistory = new ArrayList<>();
    private final Map<String, CallHistory> callHistories = new HashMap<>();
    private int size = 0;

    // key should be name + signature
    public boolean visit(final Visitor<?> visitor, final String key, final Object... args) {
        lastCall = visitor;
        // @todo: improve our concurrency. global lock = bad :/
        synchronized (this) {
            callHistory.add(new Visitor.Description(key, args));
        }
        return true;
    }

    public static Mock rollbackLast() {
        final List<Visitor.Description> hist = lastCall.getTracker().callHistory;
        return new Mock(lastCall, hist.remove(hist.size() - 1));
    }

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

    public long get(final String key, final Object... args) {
        final CallHistory hist = collect().get(key);
        return hist == null ? 0L : hist.get(args);
    }

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

    public void reset() {
        synchronized (this) {
            callHistory.clear();
            callHistories.clear();
        }
    }

    // @todo: thread safety?
    public static final class CallHistory {
        private final Map<List<Object>, Long> perArgset;

        public CallHistory(final Object... args) {
            perArgset = new HashMap<>();
            perArgset.put(Arrays.asList(args), 1L);
        }

        public void update(final Object... args) {
            final List<Object> wrapper = Arrays.asList(args);
            final Long l = perArgset.get(wrapper);
            perArgset.put(wrapper, l == null ? 1L : l + 1);
        }

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
