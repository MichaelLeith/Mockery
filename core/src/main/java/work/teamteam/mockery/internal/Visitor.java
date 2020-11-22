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

package work.teamteam.mockery.internal;

import work.teamteam.mockery.Defaults;
import work.teamteam.mockery.Matchers;
import work.teamteam.mockery.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class stored within a mock/spy that handles delegating method calls
 * @param <T> class we're mocking/spying on
 */
public class Visitor<T> {
    private static final Object[] EMPTY = new Object[]{};
    private static final TriPredicate<String, Object[], List<Object[]>> DEFAULT_VERIFIER = (k, a, l) -> true;
    // global state to support verify/when syntax.
    // as these methods don't directly receive the mock object we need some global state to record who was last touched
    // e.g when(foo.something(bar).doReturn(...)); will be tracking foo because it was last called
    private static Visitor<?> lastCall = null;
    private final Proxy<T> impl;
    private final Defaults defaults;
    private final Map<String, List<Object[]>> trackers;
    private final boolean trackHistory;
    // @note: mutation is not thread safe, we assume all your setup is run before using the mock
    private List<Callback> callbacks;
    private TriPredicate<String, Object[], List<Object[]>> verifier;
    private Map<String, CallHistory> callHistories;
    private String lastKey;
    private Object[] lastArgs;

    public Visitor(final Proxy<T> impl, final Defaults defaults, final boolean trackHistory) {
        this.trackHistory = trackHistory;
        this.callbacks = null;
        this.impl = impl;
        this.defaults = Objects.requireNonNull(defaults);
        this.verifier = DEFAULT_VERIFIER;
        this.trackers = new HashMap<>();
        callHistories = null;
        lastKey = null;
        lastArgs = null;
    }

    /**
     * Called on every method call. This does the following:
     * 1. Attempts to delegate to a verifier and exit using the default return value for this methods return type
     * 2. Notifies the tracker
     * 3. returns any callbacks (thenReturn/thenAnswer) that match this method/arg combination
     * 4. returns the default return value for this methods return type
     * @param target list to add data to - this should be a class member
     * @param key method name + description being called
     * @param clazz return type the method expects
     * @param args arguments passed to the method
     * @return an instance of the expected return type, either taken from registered callbacks or the default fallback
     * @throws Throwable throws if either the callback or fallbacks throw
     */
    public Object run(final List<Object[]> target,
                      final String key,
                      final Class<?> clazz,
                      final Object... args) throws Throwable {
        // returns true if we've visited the tracker
        if (!verifier.test(key, args, target)) {
            // note that this does not use the impl, since we don't want to risk modifying what we're spying on
            return defaults.get(clazz);
        }
        lastCall = this;
        synchronized (this) {
            lastKey = key;
            lastArgs = args;
            // only add if we're tracking
            if (trackHistory || target.isEmpty()) {
                target.add(args);
            } else {
                target.set(0, args);
            }
            if (callbacks != null) {
                for (Callback callback : callbacks) {
                    if (callback.matches(key, args)) {
                        return callback.fn.apply(args);
                    }
                }
            }
        }
        return getFallback(key, clazz, args);
    }

    /**
     * Special case of run for methods with no args
     * @param target list to add data to - this should be a class member
     * @param key method name + description being called
     * @param clazz return type the method expects
     * @return an instance of the expected return type, either taken from registered callbacks or the default fallback
     * @throws Throwable throws if either the callback or fallbacks throw
     */
    public Object run(final List<Object[]> target,
                      final String key,
                      final Class<?> clazz) throws Throwable {
        return run(target, key, clazz, EMPTY);
    }

    public List<Object[]> init(final String key) {
        // @todo: compare map vs sorted list
        final List<Object[]> history = new ArrayList<>();
        trackers.put(key, history);
        return history;
    }

    /**
     * @todo: improve this method
     * Attempts to call the "real" objects relevant method if this is a spy, otherwise falling back to the Defaults
     * @param clazz expected return type
     * @param key method name + description being called
     * @param args arguments passed to the method
     * @return either the spied methods result or the default for this class
     * @throws Throwable throws if the spied method throws, or if there is no equivalent method in the spied object
     */
    private Object getFallback(final String key, final Class<?> clazz, final Object... args) throws Throwable {
        return impl != null ? impl.match(key, args) : defaults.get(clazz);
    }

    /**
     * Appends the given combination of parameters to the callbacks we will attempt to use when methods are called.
     * Note that this replaces previous callbacks for this key. We do not append because it makes removing previous
     * callbacks non-trivial (as the api doesn't currently return index info).
     * @param fn callback to register
     * @param key method name + description we're targetting
     * @param args list of conditions for using this predicate
     */
    @SuppressWarnings("unchecked")
    public synchronized void registerCallback(final Fn fn, final String key, final Predicate<Object>... args) {
        if (callbacks == null) {
            callbacks = new ArrayList<>(4);
        } else {
            for (int i = 0; i < callbacks.size(); i++) {
                if (callbacks.get(i).key.equals(key)) {
                    callbacks.set(i, new Callback(key, args, fn));
                    return;
                }
            }
        }
        callbacks.add(new Callback(key, args, fn));
    }

    /**
     * Resets the current tracker and clears all callbacks
     * Also resets the trackers call history. This should be used as often as possible
     * as recorded history is unbounded and grows linearly with mock method calls
     */
    public void reset() {
        for (final List<Object[]> descriptions: trackers.values()) {
            descriptions.clear();
        }
        if (callHistories != null) {
            callHistories.clear();
        }
        if (callbacks != null) {
            callbacks.clear();
        }
    }

    /**
     * Resets the last called visitor. Used primarily for unit tests (see Mockery.reset())
     */
    public static void resetLast() {
        synchronized (DEFAULT_VERIFIER) {
            if (lastCall != null) {
                lastCall.reset();
                lastCall = null;
            }
        }
    }

    /**
     * Rollbacks the last called visitors entry, and returns a Mock capturing it
     * @param <T> return type of the last entries output
     * @return A mock describing the last method call seen
     */
    public static <T> Mock<T> rollbackLast() {
        synchronized (DEFAULT_VERIFIER) {
            final List<Object[]> last = lastCall.trackers.get(lastCall.lastKey);
            last.remove(last.size() - 1);
            return new Mock<>(lastCall, lastCall.lastKey, lastCall.lastArgs);
        }
    }

    /**
     * Sets the verifier to use for the next method call
     * @param verifier verifier to uset
     */
    public void setVerification(final Verifier verifier) {
        synchronized (this) {
            this.verifier = (k, a, l) -> {
                synchronized (this) {
                    this.verifier = DEFAULT_VERIFIER;
                    verifier.verify(this, k, Matchers.getMatchers(), l, a);
                }
                return false;
            };
        }
    }

    /**
     * Returns the number of times the key + args combination was called
     * @param key method name + description
     * @param args arguments used
     * @return number of calls to this combination
     */
    public synchronized int get(final String key, final Object... args) {
        return collect(key).perArgset.getOrDefault(Arrays.asList(args), 0);
    }

    /**
     * Converts the call history into a {@literal Map<MethodName+Description, CallHistory>} to make lookup easier
     * at the expense of absolute ordering
     * @param key method to collect a history for
     * @return reformatted call history
     */
    public synchronized CallHistory collect(final String key) {
        CallHistory callHistory = null;
        if (callHistories == null) {
            callHistories = new HashMap<>();
        } else {
            callHistory = callHistories.get(key);
        }
        if (callHistory == null) {
            callHistory = new CallHistory();
            callHistories.put(key, callHistory);
        }
        final List<Object[]> history = trackers.get(key);
        if (history != null && callHistory.size != history.size()) {
            for (int i = callHistory.size; i < history.size(); i++) {
                callHistory.update(history.get(i));
            }
        }
        return callHistory;
    }

    private static final class Callback {
        private final String key;
        private final Predicate<Object>[] args;
        private final Fn fn;

        public Callback(final String key, final Predicate<Object>[] args, final Fn fn) {
            this.key = key;
            this.args = args;
            this.fn = fn;
        }

        public boolean matches(final String name, final Object... args) {
            if (this.args.length != args.length || !this.key.equals(name)) {
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                if (!this.args[i].test(args[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    public static final class CallHistory {
        private final Map<List<Object>, Integer> perArgset = new HashMap<>();
        private int size = 0;

        /**
         * Adds or updates the call history with the given args
         * @param args list of args for a specific method call
         */
        public void update(final Object... args) {
            size++;
            final List<Object> wrapper = Arrays.asList(args);
            perArgset.put(wrapper, perArgset.getOrDefault(wrapper, 0) + 1);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return perArgset.equals(((CallHistory) o).perArgset);
        }

        @Override
        public int hashCode() {
            return 31 + perArgset.hashCode();
        }
    }

    /**
     * Not using Function because we want to support throwing (for thenThrow)
     */
    public interface Fn {
        Object apply(final Object[] args) throws Throwable;
    }

    public interface TriPredicate<A, B, C> {
        boolean test(A a, B b, C c);
    }
}
