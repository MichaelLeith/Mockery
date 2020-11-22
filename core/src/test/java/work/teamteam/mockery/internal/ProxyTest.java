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

package work.teamteam.mockery.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyTest {
    @Test
    void testMatch() throws InvocationTargetException, IllegalAccessException {
        final Proxy<Clazz> proxy = Proxy.of(new Clazz());
        assertEquals("hi", proxy.match("doStuff(I)Ljava/lang/String;", 1));
        assertEquals("hi bob", proxy.match("doStuff(ILjava/lang/String;)Ljava/lang/String;", 1, "bob"));
        assertEquals(1, proxy.match("doOtherStuff(I)I", 1));

        assertThrows(RuntimeException.class, () -> proxy.match("doOtherStuff(ILjava/lang/String;)I", 1));
        assertThrows(RuntimeException.class, () -> proxy.match("doStuff(I)V", 1));
        assertThrows(RuntimeException.class, () -> proxy.match("missing(ILjava/lang/String;)I", 1));
    }

    private static final class Clazz {
        String doStuff(final int i) {
            return "hi";
        }

        String doStuff(final int i, final String j) {
            return "hi " + j;
        }

        int doOtherStuff(final int i) {
            return i;
        }
    }
}