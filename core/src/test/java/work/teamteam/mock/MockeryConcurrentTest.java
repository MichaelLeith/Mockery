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

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static work.teamteam.mock.Matchers.anyLong;
import static work.teamteam.mock.Matchers.capture;
import static work.teamteam.mock.Mockery.when;

/**
 * These tests clearly aren't complete, don't trust them!
 */
public class MockeryConcurrentTest {
    @Test
    void testConcurrentMockCreationDoesntThrow() {
        final List<Foo> res = IntStream.range(0, 1_000_000).parallel()
                .mapToObj(i -> Mockery.mock(Foo.class)).collect(Collectors.toList());
        assertEquals(1_000_000, res.size());
    }

    @Test
    void testConcurrentDefault() {
        final Foo foo = Mockery.mock(Foo.class);
        final long i = 1_000_000;
        final long res = LongStream.range(0, i).parallel()
                .map(foo::doStuff).sum();
        assertEquals(0, res);
        final Capture<Long> capture = Capture.of(long.class);
        Mockery.verify(foo, (int) i).doStuff(capture(capture));

        final Set<Long> caught = new HashSet<>(capture.captured());
        assertEquals(capture.captured().size(), caught.size());
        assertTrue(LongStream.range(0, i).parallel().allMatch(caught::contains));
    }

    @Test
    void testConcurrentWithAnswer() {
        final Foo foo = Mockery.mock(Foo.class);
        final Counter counter = new Counter();
        when(foo.doStuff(anyLong())).thenAnswer(i -> counter.add((long) i[0]));
        final long i = 1_000_000;
        final long res = LongStream.range(0, i).parallel()
                .map(foo::doStuff).sum();
        final long calls = (i - 1) * i / 2;
        assertEquals(calls, res);
        // we don't guarantee answers are atomic
        assertTrue(calls >= counter.i);
        assertEquals(calls, counter.l.get());
        final Capture<Long> capture = Capture.of(long.class);
        Mockery.verify(foo, (int) i).doStuff(capture(capture));

        final Set<Long> caught = new HashSet<>(capture.captured());
        assertEquals(capture.captured().size(), caught.size());
        assertTrue(LongStream.range(0, i).parallel().allMatch(caught::contains));
    }

    @Test
    void testConcurrentWithHistoryDisabled() {
        final Foo foo = Mockery.mock(Foo.class, false);
        when(foo.doStuff(anyLong())).thenAnswer(i -> (long) i[0]);
        final long i = 1_000_000;
        final long res = LongStream.range(0, i).parallel()
                .map(foo::doStuff).sum();
        final long calls = (i - 1) * i / 2;
        assertEquals(calls, res);
    }

    @Test
    void testConcurrentVerifyThrows() {
        final Foo foo = Mockery.mock(Foo.class);
        int i = 1_000_000;
        // result is wrong because we haven't locked the verify
        assertThrows(RuntimeException.class, () -> LongStream.range(0, i).parallel()
                .forEach(j -> Mockery.verify(foo, 0).doStuff(1)));
    }

    public interface Foo {
        long doStuff(long i);
    }

    private static final class Counter {
        long i = 0;
        final AtomicLong l = new AtomicLong(0);

        long add(final long i) {
            this.i += i;
            l.addAndGet(i);
            return i;
        }
    }
}
