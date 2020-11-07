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

package work.teamteam.mock.annotations;

import work.teamteam.mock.Capture;
import work.teamteam.mock.Mockery;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class MockeryInject {
    private MockeryInject() {}

    public static void inject(final Object o) {
        try {
            injectInner(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void injectInner(final Object o) throws IllegalAccessException {
        final List<Field> fields = getInheritedFields(o.getClass());
        final List<Field> toInject = new ArrayList<>();
        final Map<Class<?>, List<Field>> typeMapping = new HashMap<>();
        for (final Field field: fields) {
            if (field.isAnnotationPresent(Mock.class)) {
                init(o, field, Mockery::mock);
            } else if (field.isAnnotationPresent(Spy.class)) {
                init(o, field, i -> {
                    try {
                        final Object target = field.get(o);
                        return Mockery.spy(target != null ? target : i.getConstructor().newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException("No zero arg constructor found");
                    }
                });
            } else if (field.isAnnotationPresent(Captor.class)) {
                init(o, field, Capture::of);
            } else if (field.isAnnotationPresent(InjectMocks.class)) {
                toInject.add(field);
            }

            List<Field> clazzes = typeMapping.get(field.getClass());
            if (clazzes == null) {
                clazzes = new ArrayList<>();
            }
            clazzes.add(field);
            typeMapping.put(field.getClass(), clazzes);
        }

        if (toInject.isEmpty()) {
            return;
        }
        fields.sort(Comparator.comparing(f -> f.getType().getName()));
        // @todo: graph traversal
        for (final Field field: toInject) {
            if (field.get(o) != null) {
                throw new RuntimeException("InjectMocks annotated field must not be initialized");
            }
            final Class<?> type = field.getType();
            final Constructor<?> match = Objects.requireNonNull(weigh(type.getConstructors(), fields),
                    "No matching constructors found");
            final Object[] args = buildParams(o, match, fields);
            try {
                match.setAccessible(true);
                field.set(o, match.newInstance(args));
            } catch (InstantiationException|InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                match.setAccessible(false);
            }
        }
    }

    private static List<Field> getInheritedFields(final Class<?> type) {
        final List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
    /**
     * @implNote assumes the fields are sorted
     * @param constructors constructors to match
     * @param fields fields available to inject
     * @return best matching constructor or null
     */
    private static Constructor<?> weigh(final Constructor<?>[] constructors, final List<Field> fields) {
        int bestMatch = -1;
        Constructor<?> match = null;
        for (final Constructor<?> constructor: constructors) {
            final Parameter[] params = constructor.getParameters();
            int weights = 0;
            for (final Parameter param : params) {
                final Class<?> c = param.getType();
                final String name = c.getName();
                int i = Collections.binarySearch(fields, name, MockeryInject::compareFieldToString);
                if (i < 0) {
                    weights = -1;
                    break;
                }
                weights += 1;
                for (; i < fields.size() && c.equals(fields.get(i).getType()); i++) {
                    // @todo: should we prioritise mocks?
                    if (fields.get(i).getName().equals(param.getName())) {
                        // weight this slightly higher because it's named
                        weights += 1;
                        break;
                    }
                }
            }
            if (weights > bestMatch) {
                match = constructor;
                bestMatch = weights;
            }
        }
        return match;
    }

    private static Object[] buildParams(final Object o,
                                        final Constructor<?> constructor,
                                        final List<Field> fields) throws IllegalAccessException {
        final Object[] args = new Object[constructor.getParameterCount()];
        final Parameter[] params = constructor.getParameters();
        for (int i = 0; i < params.length; i++) {
            final Class<?> clazz = params[i].getType();
            final int j = Collections.binarySearch(fields, clazz.getName(), MockeryInject::compareFieldToString);
            boolean match = false;
            for (int k = j; k < fields.size() && clazz.equals(fields.get(k).getType()); k++) {
                if (fields.get(k).getName().equals(params[i].getName())) {
                    args[i] = get(o, fields.get(k));
                    match = true;
                    break;
                }
            }
            if (!match) {
                args[i] = get(o, fields.get(j));
            }
        }
        return args;
    }

    private static int compareFieldToString(final Object a, final Object b) {
        return ((Field) a).getType().getName().compareTo((String) b);
    }

    private static void init(final Object o, final Field field, final Function<Class<?>, ?> creator) {
        try {
            field.setAccessible(true);
            if (field.getType().isPrimitive()) {
                throw new RuntimeException("primitive mocking is not supported");
            }
            field.set(o, creator.apply(field.getType()));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    private static final Map<Class<?>, BiFn> LOOKUP = new HashMap<>();
    static {
        LOOKUP.put(boolean.class, (o, f) -> f.getBoolean(o));
        LOOKUP.put(byte.class, (o, f) -> f.getByte(o));
        LOOKUP.put(char.class, (o, f) -> f.getChar(o));
        LOOKUP.put(short.class, (o, f) -> f.getShort(o));
        LOOKUP.put(int.class, (o, f) -> f.getInt(o));
        LOOKUP.put(long.class, (o, f) -> f.getLong(o));
        LOOKUP.put(float.class, (o, f) -> f.getFloat(o));
        LOOKUP.put(double.class, (o, f) -> f.getDouble(o));
    }

    private interface BiFn {
        Object apply(final Object o, final Field f) throws IllegalAccessException;
    }

    private static Object get(final Object o, final Field field) throws IllegalAccessException {
        final Class<?> type = field.getType();
        if (type.isPrimitive()) {
            return LOOKUP.get(type).apply(o, field);
        }
        return field.get(o); // @todo: primitives
    }
}
