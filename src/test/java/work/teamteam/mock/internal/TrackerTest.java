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

import org.junit.jupiter.api.Test;
import work.teamteam.mock.Defaults;
import work.teamteam.mock.Mock;
import work.teamteam.mock.internal.Tracker;
import work.teamteam.mock.internal.Visitor;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackerTest {
    @Test
    void testVisit() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo");
        tracker.visit(visitor, "foo", 1);
        tracker.visit(visitor, "foo", 1);
        tracker.visit(visitor, "foo", "bar");
        tracker.visit(visitor, "bar");

        final Map<String, Tracker.CallHistory> history = tracker.collect();
        assertEquals(2, history.size());
        final Tracker.CallHistory expected = new Tracker.CallHistory();
        expected.update(1);
        expected.update(1);
        expected.update("bar");
        assertEquals(1, expected.get());
        assertEquals(2, expected.get(1));
        assertEquals(1, expected.get("bar"));
        assertEquals(expected, history.get("foo"));

        final Tracker.CallHistory expected2 = new Tracker.CallHistory();
        assertEquals(1, expected2.get());
        assertEquals(expected2, history.get("bar"));
    }

    @Test
    void testReset() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo");
        assertFalse(tracker.collect().isEmpty());
        tracker.reset();
        assertTrue(tracker.collect().isEmpty());
    }

    @Test
    void testGet() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo");
        assertEquals(1, tracker.get("foo"));
        assertEquals(0, tracker.get("foo", "bar"));
        assertEquals(0, tracker.get("bar"));
    }

    @Test
    void testRollbackFailsWithNoCalls() {
        // hack to reset lastCall, since it's global
        new Tracker().visit(null, "foo");
        assertThrows(NullPointerException.class, Tracker::rollbackLast);
    }

    @Test
    void testRollbackFailsWithEmptyHistory() {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Tracker tracker = visitor.getTracker();
        tracker.visit(visitor, "foo");
        tracker.reset();
        assertThrows(IndexOutOfBoundsException.class, Tracker::rollbackLast);
    }

    @Test
    void testRollbackLast() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Tracker tracker = visitor.getTracker();
        tracker.visit(visitor, "foo");
        tracker.visit(visitor, "foo");
        final Mock mock = Tracker.rollbackLast();
        assertFalse(tracker.collect().isEmpty());
        assertEquals(Collections.singletonMap("foo", new Tracker.CallHistory()), tracker.collect());

        assertNull(visitor.run("foo", Integer.class));
        mock.thenReturn(100);
        assertEquals(100, visitor.run("foo", Integer.class));
    }

    @Test
    void testCollectDoesntClearHistory() throws Throwable {
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        final Tracker tracker = visitor.getTracker();
        tracker.visit(visitor, "foo");
        tracker.visit(visitor, "foo");
        tracker.collect();
        final Mock mock = Tracker.rollbackLast();

        assertNull(visitor.run("foo", Integer.class));
        mock.thenReturn(100);
        assertEquals(100, visitor.run("foo", Integer.class));
    }

    @Test
    void testCallHistoryEquals() {
        final Tracker.CallHistory a = new Tracker.CallHistory(1, 2);
        final Tracker.CallHistory b = new Tracker.CallHistory(1, 2);
        final Tracker.CallHistory c = new Tracker.CallHistory(1);
        final Tracker.CallHistory d = new Tracker.CallHistory(1, 2);
        d.update(1, 2);
        final Tracker.CallHistory e = new Tracker.CallHistory(1, 2);
        e.update(1);

        assertNotEquals(a, null);
        assertNotEquals(a, "foo");
        assertEquals(a, a);
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a, d);
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(a, e);
        assertNotEquals(a.hashCode(), e.hashCode());
    }
}
