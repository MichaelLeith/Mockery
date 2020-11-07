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

import org.objectweb.asm.Type;
import work.teamteam.mock.Defaults;
import work.teamteam.mock.Matchers;
import work.teamteam.mock.Mock;

import java.lang.reflect.Method;
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
    private static volatile Visitor<?> lastCall = null;
    // @note: mutation is not thread safe, we assume all your setup is run before using the mock
    private List<Callback> callbacks;
    private final T impl;
    private final Defaults defaults;
    private TriPredicate<String, Object[], List<Object[]>> verifier;
    private final Map<String, List<Object[]>> trackers;
    private Map<String, CallHistory> callHistories;
    private String lastKey;

    public Visitor(final T impl, final Defaults defaults) {
        this.callbacks = null;
        this.impl = impl;
        this.defaults = Objects.requireNonNull(defaults);
        this.verifier = DEFAULT_VERIFIER;
        this.trackers = new HashMap<>();
        callHistories = null;
        lastKey = null;
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
            target.add(args);
        }
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                if (callback.matches(key, args)) {
                    return callback.fn.apply(args);
                }
            }
        }
        return getFallback(key, clazz, args);
    }

    /**
     * Special case of run for methods with no args
     * @param target
     * @param key
     * @param clazz
     * @return
     * @throws Throwable
     */
    public Object run(final List<Object[]> target,
                      final String key,
                      final Class<?> clazz) throws Throwable {
        return run(target, key, clazz, EMPTY);
    }

    public List<Object[]> init(final String key) {
        final List<Object[]> history = new ArrayList<>();
        trackers.put(key, history);
        return history;
    }

    /**
     * Attempts to call the "real" objects relevant method if this is a spy, otherwise falling back to the Defaults
     * @param clazz expected return type
     * @param key method name + description being called
     * @param args arguments passed to the method
     * @return either the spied methods result or the default for this class
     * @throws Throwable throws if the spied method throws, or if there is no equivalent method in the spied object
     */
    private Object getFallback(final String key, final Class<?> clazz, final Object... args) throws Throwable {
        if (impl != null) {
            for (final Method method: impl.getClass().getDeclaredMethods()) {
                if (key.equals(method.getName() + Type.getMethodDescriptor(method))) {
                    method.setAccessible(true);
                    return method.invoke(impl, args);
                }
            }
            throw new RuntimeException("Should not happen, missing method " + key);
        }
        return defaults.get(clazz);
    }

    /**
     * Appends the given combination of parameters to the callbacks we will attempt to use when methods are called.
     * Note that this replaces previous callbacks for this key. We do not append because it makes removing previous
     * callbacks non-trivial (as the api doesn't currently return index info).
     * @param fn callback to register
     * @param key method name + description we're targetting
     * @param args list of conditions for using this predicate
     */
    public void registerCallback(final Fn fn, final String key, final List<Predicate<Object>> args) {
        if (callbacks == null) {
            callbacks = new ArrayList<>(4);
        }
        for (int i = 0; i < callbacks.size(); i++) {
            if (callbacks.get(i).key.equals(key)) {
                callbacks.set(i, new Callback(key, args, fn));
                return;
            }
        }
        callbacks.add(new Callback(key, args, fn));
    }

    /**
     * Resets the current tracker & clears all callbacks
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

    public static <T> Mock<T> rollbackLast() {
        synchronized (DEFAULT_VERIFIER) {
            final List<Object[]> last = lastCall.trackers.get(lastCall.lastKey);
            return new Mock<>(lastCall, lastCall.lastKey, last.remove(last.size() - 1));
        }
    }

    /**
     * Sets the verifier to use for the next method call
     * @param verifier verifier to uset
     */
    public void setVerification(final Verifier verifier) {
        this.verifier = (k, a, l) -> {
            verifier.verify(this, k, Matchers.getMatchers(), l, a);
            this.verifier = DEFAULT_VERIFIER;
            return false;
        };
    }


    /**
     * Returns the number of times the key + args combination was called
     * @param key method name + description
     * @param args arguments used
     * @return number of calls to this combination
     */
    public int get(final String key, final Object... args) {
        return collect(key).get(args);
    }

    /**
     * Converts the call history into a Map<MethodName+Description, CallHistory> to make lookup easier
     * at the expense of absolute ordering
     * @return reformatted call history
     */
    public CallHistory collect(final String key) {
        synchronized (this) {
            if (callHistories == null) {
                callHistories = new HashMap<>();
            }
            CallHistory callHistory = callHistories.get(key);
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
    }

    private static final class Callback {
        private final String key;
        private final List<Predicate<Object>> args;
        private final Fn fn;

        public Callback(final String key, final List<Predicate<Object>> args, final Fn fn) {
            this.key = key;
            this.args = args;
            this.fn = fn;
        }

        public boolean matches(final String name, final Object... args) {
            if (this.args.size() != args.length || !this.key.equals(name)) {
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                if (!this.args.get(i).test(args[i])) {
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

        /**
         * Returns the number of times the CallHistory has seen the specific set of arguments
         * @param args list of args to check for
         * @return number of times these have been seen
         */
        public int get(final Object... args) {
            return perArgset.getOrDefault(Arrays.asList(args), 0);
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
