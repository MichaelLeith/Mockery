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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Visitor<T> {
    private static final BiPredicate<String, Object[]> DEFAULT_VERIFIER = (k, a) -> true;
    // @note: mutation is not thread safe, we assume all your setup is run before using the mock
    private final List<Callback> callbacks;
    private final Tracker tracker;
    private final T impl;
    private final Defaults defaults;
    private BiPredicate<String, Object[]> verifier;

    public Visitor(final T impl, final Defaults defaults) {
        this.callbacks = new ArrayList<>();
        this.tracker = new Tracker();
        this.impl = impl;
        this.defaults = Objects.requireNonNull(defaults);
        this.verifier = DEFAULT_VERIFIER;
    }

    public Object run(final String key, final Class<?> clazz, final Object... args) throws Throwable {
        // returns true if we've visited the tracker
        if (!(verifier.test(key, args) && tracker.visit(this, key, args))) {
            return defaults.get(clazz);
        }
        for (final Callback callback: callbacks) {
            if (callback.matches(key, args)) {
                return callback.fn.apply(args);
            }
        }
        return getFallback(clazz, key, args);
    }

    private Object getFallback(final Class<?> clazz, final String key, final Object... args) throws Throwable {
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

    public void registerCallback(final Fn fn, final String key, final List<Predicate<Object>> args) {
        for (int i = 0; i < callbacks.size(); i++) {
            if (callbacks.get(i).key.equals(key)) {
                callbacks.set(i, new Callback(key, args, fn));
                return;
            }
        }
        callbacks.add(new Callback(key, args, fn));
    }

    public void reset() {
        tracker.reset();
        callbacks.clear();
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setVerification(final Verifier verifier) {
        this.verifier = (k, a) -> {
            verifier.verify(tracker, k, Matchers.getMatchers(), a);
            this.verifier = DEFAULT_VERIFIER;
            return false;
        };
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

    public static final class Description {
        private final String key;
        private final Object[] args;

        public Description(final String key, final Object... args) {
            this.key = key;
            this.args = args;
        }

        public String getKey() {
            return key;
        }

        public Object[] getArgs() {
            return args;
        }
    }

    public interface Fn {
        Object apply(final Object[] args) throws Throwable;
    }
}
