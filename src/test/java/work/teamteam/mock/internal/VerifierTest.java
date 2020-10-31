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
import work.teamteam.mock.internal.Tracker;
import work.teamteam.mock.internal.Verifier;
import work.teamteam.mock.internal.Visitor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VerifierTest {
    @Test
    void testMatch() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo");
        tracker.visit(visitor, "foo");
        new Verifier(2).verify(tracker, "foo", Collections.emptyList());
    }

    @Test
    void testFail() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo");
        assertThrows(RuntimeException.class, () ->
                new Verifier(2).verify(tracker, "foo", Collections.emptyList()));
    }

    @Test
    void testFailDifferentFnName() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo2");
        new Verifier(1).verify(tracker, "foo2", Collections.emptyList());
        assertThrows(RuntimeException.class, () ->
                new Verifier(1).verify(tracker, "foo", Collections.emptyList()));
    }

    @Test
    void testFailDifferentArgs() {
        final Tracker tracker = new Tracker();
        final Visitor<?> visitor = new Visitor<>(null, Defaults.Impl.IMPL);
        tracker.visit(visitor, "foo", 1);
        new Verifier(1).verify(tracker, "foo", Collections.emptyList(), 1);
        assertThrows(RuntimeException.class, () ->
                new Verifier(1).verify(tracker, "foo", Collections.emptyList()));
    }
}
