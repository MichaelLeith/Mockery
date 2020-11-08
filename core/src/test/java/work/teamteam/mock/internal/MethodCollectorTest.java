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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodCollectorTest {
    @Test
    void testThrowOnMissingInterface() {
        final MethodCollector collector = new MethodCollector(Opcodes.ACC_FINAL,
                false, Collections.emptySet(), new HashSet<>());
        assertThrows(RuntimeException.class, () -> collector.visit(Opcodes.ASM9, 0, "foo",
                "bar()V", null, new String[]{"fake"}));
    }

    @Test
    void testTraversal() throws IOException {
        final Set<MethodSummary> methods = new HashSet<>();
        final MethodCollector collector = new MethodCollector(Opcodes.ACC_FINAL,
                false, Collections.emptySet(), methods);
        new ClassReader(Root.class.getName()).accept(collector, ClassReader.EXPAND_FRAMES);
        assertNotEquals(Collections.emptySet(), methods);
        assertEquals(6, methods.size());
        final MethodSummary rootMethod = new MethodSummary("rootMethod",
                "()Ljava/lang/String;",
                null,
                null);
        assertFalse(methods.contains(rootMethod));

        final MethodSummary interface1Method = new MethodSummary("interface1Method",
                "()I",
                null,
                new String[]{"RuntimeException"});
        assertTrue(methods.contains(interface1Method));

        final MethodSummary interface2Method = new MethodSummary("interface2Method",
                "()I",
                null,
                null);
        assertTrue(methods.contains(interface2Method));

        final MethodSummary interface3Method = new MethodSummary("interface3Method",
                "()I",
                null,
                null);
        assertTrue(methods.contains(interface3Method));

        final MethodSummary interface4Method = new MethodSummary("interface4Method",
                "()I",
                null,
                null);
        assertTrue(methods.contains(interface4Method));

        final MethodSummary base1Method = new MethodSummary("base1Method",
                "()Ljava/lang/String;",
                null,
                null);
        assertTrue(methods.contains(base1Method));

        final MethodSummary base2Method = new MethodSummary("base2Method",
                "()Ljava/lang/String;",
                null,
                null);
        assertTrue(methods.contains(base2Method));
    }

    @Test
    void testThrowsOnFinalClass() {
        final Set<MethodSummary> methods = new HashSet<>();
        final MethodCollector collector = new MethodCollector(Opcodes.ACC_FINAL,
                false, Collections.emptySet(), methods);
        assertThrows(RuntimeException.class, () ->
                new ClassReader(Invalid.class.getName()).accept(collector, ClassReader.EXPAND_FRAMES));
    }

    @Test
    void testCollectInterface() throws IOException {
        final Set<MethodSummary> methods = new HashSet<>();
        final MethodCollector collector = new MethodCollector(Opcodes.ACC_FINAL,
                false, Collections.emptySet(), methods);
        new ClassReader(Interface3.class.getName()).accept(collector, ClassReader.EXPAND_FRAMES);
        assertEquals(1, methods.size());
        final MethodSummary interface3Method = new MethodSummary("interface3Method",
                "()I",
                null,
                null);
        assertTrue(methods.contains(interface3Method));
    }

    @Test
    void testCollectObjectDirectly() throws IOException {
        final Set<MethodSummary> methods = new HashSet<>();
        final MethodCollector collector = new MethodCollector(0,
                false, Collections.emptySet(), methods);
        new ClassReader(Object.class.getName()).accept(collector, ClassReader.EXPAND_FRAMES);
        assertNotEquals(Collections.emptySet(), methods);
    }

    static final class Invalid {}

    static class Root extends Base1 implements Interface1, Interface2 {
        final String rootMethod() {
            return null;
        }

        public int interface1Method() throws RuntimeException {
            return 1;
        }
    }

    static class Base1 extends Base2 {
        String base1Method() {
            return null;
        }
    }

    static class Base2 implements Interface3 {
        String base2Method() {
            return null;
        }

        public int interface3Method() {
            return 0;
        }
    }

    interface Interface1 {
        int interface1Method();
    }

    interface Interface2 extends Interface4 {
        default int interface2Method() {
            return 0;
        }
    }

    interface Interface3 {
        int interface3Method();
    }

    interface Interface4 {
        default int interface4Method() {
            return 0;
        }
    }
}
