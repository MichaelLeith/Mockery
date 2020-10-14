package work.teamteam.mock;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

public class Visitor<T> {
    private static final BiPredicate<String, Object[]> DEFAULT_VERIFIER = (k, a) -> true;
    // @note: mutation is not thread safe, we assume all your setup is run before using the mock
    private final Map<Description, Fn> callbacks;
    private final Tracker tracker;
    private final T impl;
    private BiPredicate<String, Object[]> verifier;

    public Visitor(final T impl) {
        this.callbacks = new HashMap<>();
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
            return getFallback(clazz, fallback, key, args);
        }
        final Object o = callbacks.getOrDefault(new Description(key, args), a -> fallback).apply(args);
        if (o == null) {
            return getFallback(clazz, fallback, key, args);
        } else if (clazz.isAssignableFrom(o.getClass())) {
            return clazz.cast(o);
        }
        throw new RuntimeException(clazz.getSimpleName() + " is not assignable from " + o);
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
            throw new RuntimeException("Should not happen");
        }
        return fallback;
    }

    public void registerCallback(final Fn fn, final Description description) {
        callbacks.put(description, fn);
    }

    public void reset() {
        tracker.reset();
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setVerification(final Verifier verifier) {
        this.verifier = (k, a) -> {
            verifier.verify(tracker, k, a);
            this.verifier = DEFAULT_VERIFIER;
            return false;
        };
    }

    public boolean invokeZ(final String key, final Object... args) throws Throwable {
        return run(boolean.class, false, key, args);
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

    public void invokeV(final String key, final Object... args) {
        run(key, args);
    }

    public Object invokeL(final String key, final Object... args) throws Throwable {
        return run(Object.class, null, key, args);
    }

    public static final class Description {
        private final String key;
        private final Object[] args;

        public Description(final String key, final Object[] args) {
            this.key = key;
            this.args = args;
        }

        public String getKey() {
            return key;
        }

        public Object[] getArgs() {
            return args;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Description that = (Description) o;
            return key.equals(that.key) && Arrays.equals(args, that.args);
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(key) + Arrays.hashCode(args);
        }
    }

    public interface Fn {
        Object apply(final Object[] args) throws Throwable;
    }
}
