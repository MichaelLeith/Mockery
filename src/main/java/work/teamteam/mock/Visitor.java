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

import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Visitor<T> {
    private static final BiPredicate<String, Object[]> DEFAULT_VERIFIER = (k, a) -> true;
    // @note: mutation is not thread safe, we assume all your setup is run before using the mock
    private final List<Callback> callbacks;
    private final Tracker tracker;
    private final T impl;
    private BiPredicate<String, Object[]> verifier;

    public Visitor(final T impl) {
        this.callbacks = new ArrayList<>();
        this.tracker = new Tracker();
        this.impl = impl;
        this.verifier = DEFAULT_VERIFIER;
    }

    // returns true if we've visited the tracker
    private boolean run(final String key, final Object... args) {
        return verifier.test(key, args) && tracker.visit(this, key, args);
    }

    private <T> T run(final Class<T> clazz, final T fallback, final String key, final Object... args) throws Throwable {
        if (!run(key, args)) {
            return fallback;
        }
        final Object o = match(key, args);
        if (o == null) {
            return getFallback(clazz, fallback, key, args);
        } else if (clazz.isAssignableFrom(o.getClass())) {
            return clazz.cast(o);
        }
        throw new RuntimeException(clazz.getSimpleName() + " is not assignable from " + o);
    }

    private Object match(final String key, final Object... args) throws Throwable {
        for (final Callback callback: callbacks) {
            if (callback.matches(key, args)) {
                return callback.fn.apply(args);
            }
        }
        return null;
    }

    private <T> T getFallback(final Class<T> clazz,
                              final T fallback,
                              final String key,
                              final Object... args) throws Throwable{
        if (impl != null) {
            for (final Method method: impl.getClass().getDeclaredMethods()) {
                if (key.equals(method.getName() + Type.getMethodDescriptor(method))) {
                    method.setAccessible(true);
                    return clazz.cast(method.invoke(impl, args));
                }
            }
            throw new RuntimeException("Should not happen, missing method " + key);
        }
        return fallback;
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
            final List<Predicate<Object>> matchers = Matchers.getMatchers();
            verifier.verify(tracker, k, matchers, a);
            this.verifier = DEFAULT_VERIFIER;
            return false;
        };
    }

    public boolean invokeZ(final String key, final Object... args) throws Throwable {
        return run(Boolean.class, false, key, args);
    }

    public byte invokeB(final String key, final Object... args) throws Throwable {
        return run(Byte.class, (byte) 0, key, args);
    }

    public char invokeC(final String key, final Object... args) throws Throwable {
        return run(Character.class, (char) 0, key, args);
    }

    public short invokeS(final String key, final Object... args) throws Throwable {
        return run(Short.class, (short) 0, key, args);
    }

    public int invokeI(final String key, final Object... args) throws Throwable {
        return run(Integer.class, 0, key, args);
    }

    public long invokeJ(final String key, final Object... args) throws Throwable {
        return run(Long.class, 0L, key, args);
    }

    public float invokeF(final String key, final Object... args) throws Throwable {
        return run(Float.class, 0f, key, args);
    }

    public double invokeD(final String key, final Object... args) throws Throwable {
        return run(Double.class, 0.0, key, args);
    }

    public void invokeV(final String key, final Object... args) throws Throwable {
        run(Void.class, null, key, args);
    }

    public Object invokeL(final String key, final Object... args) throws Throwable {
        return run(Object.class, null, key, args);
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
