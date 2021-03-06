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

package com.mikeleith.mockery.internal;

import org.junit.jupiter.api.Test;
import com.mikeleith.mockery.Defaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerifierTest {
    @Test
    void testMatch() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = visitor.init("foo");
        visitor.run(list, "foo", Object.class);
        visitor.run(list, "foo", Object.class);
        new Verifier(i -> i == 2).verify(visitor, "foo", null, list);
    }

    @Test
    void testMatchIsRepeatable() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = visitor.init("foo");
        visitor.run(list, "foo", Object.class);
        visitor.run(list, "foo", Object.class);
        for (int j = 0; j < 10; j++) {
            new Verifier(i -> i == 2).verify(visitor, "foo", null, list);
        }
    }

    @Test
    void testFail() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = new ArrayList<>();
        visitor.run(list, "foo", Object.class);
        assertThrows(RuntimeException.class, () ->
                new Verifier(i -> i == 2).verify(visitor, "foo", null, list));
    }

    @Test
    void testFailDifferentFnName() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = visitor.init("foo2");
        visitor.run(list, "foo2", Object.class);
        new Verifier(i -> i == 1).verify(visitor, "foo2", null, list);
        assertThrows(RuntimeException.class, () ->
                new Verifier(i -> i == 1).verify(visitor, "foo", null, new ArrayList<>()));
    }

    @Test
    void testFailDifferentArgs() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = visitor.init("foo");
        visitor.run(list, "foo", Object.class, 1);
        new Verifier(i -> i == 1).verify(visitor, "foo", null, list, 1);
        assertThrows(RuntimeException.class, () ->
                new Verifier(i -> i == 1).verify(visitor, "foo", null, new ArrayList<>()));
    }

    @Test
    void testThrowsOnWrongNumArgs() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = new ArrayList<>();
        visitor.run(list, "foo", Object.class, 1);
        assertThrows(RuntimeException.class, () -> new Verifier(i -> i == 1).verify(visitor, "foo", null, new ArrayList<>()));
        assertThrows(RuntimeException.class, () -> new Verifier(i -> i == 1).verify(visitor, "foo", null, new ArrayList<>(), 1, 2));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMatchCondition() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = new ArrayList<>();
        visitor.run(list, "foo", Object.class, 1);
        visitor.run(list, "foo", Object.class, 1);
        new Verifier(i -> i == 2).verify(visitor, "foo", new Predicate[]{i -> (int) i == 1}, list, 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMatchConditionButWrongNumberOfArgs() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = new ArrayList<>();
        visitor.run(list, "foo", Object.class, 1);
        visitor.run(list, "foo", Object.class, 1);
        assertThrows(RuntimeException.class, () -> new Verifier(i -> i == 2).verify(visitor, "foo",
                new Predicate[]{i -> (int) i == 1, i -> (int) i == 2}, list, 1, 2));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDoesntMatchCondition() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = new ArrayList<>();
        visitor.run(list, "foo", Object.class, 1);
        visitor.run(list, "foo", Object.class, 2);
        visitor.run(list, "foo", Object.class, 1);
        new Verifier(i -> i == 1).verify(visitor, "foo", new Predicate[]{i -> (int) i == 2}, list, 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testThrowsOnWrongNumPredicateArgs() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL, true);
        final List<Object[]> list = new ArrayList<>();
        visitor.run(list, "foo", Object.class, 1);
        // too many
        assertThrows(RuntimeException.class, () -> new Verifier(i -> i == 1).verify(visitor, "foo",
                new Predicate[]{i -> (int) i == 1, Objects::nonNull}, list, 1));
        // too few
        assertThrows(RuntimeException.class, () -> new Verifier(i -> i == 1).verify(visitor, "foo",
                new Predicate[]{i -> (int) i == 1}, list, 1, 2));
    }
}
