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

import org.objenesis.ObjenesisStd;
import work.teamteam.mock.internal.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class providing methods to override the return value for a mocks method
 * e.g when(foo.doSomething(anyInt())).thenReturn(100)
 *
 * Methods can be chained. In this case subsequent calls to "doSomething" will step through the chain.
 * The last invoked then method will be looped infinitely when the end of the chain is reached.
 *
 * e.g when(foo.doSomething(anyInt())).thenReturn(100).thenThrow(RuntimeException.class)
 * will return 100, then all future calls will throw a RuntimeException
 */
public class Mock {
    // list of "thenX" methods that have been registered to this mock
    private final List<Visitor.Fn> state;

    /**
     * INTERNAL
     * @param last visitor to associate these mocked return values with
     * @param description method signature we are mocking
     */
    public Mock(final Visitor<?> last, final Visitor.Description description) {
        this.state = new ArrayList<>();
        List<Predicate<Object>> matchers = Matchers.getMatchers();
        if (matchers.isEmpty()) {
            matchers = new ArrayList<>(description.getArgs().length);
            for (final Object arg: description.getArgs()) {
                matchers.add(arg == null ? Objects::isNull : arg::equals);
            }
        } else if (description.getArgs().length != matchers.size()) {
            throw new RuntimeException("Not all arguments mocked, you must use eq for literals with Matchers");
        }
        last.registerCallback(a -> state.isEmpty() ? null : (state.size() == 1 ? state.get(0) : state.remove(0))
                        .apply(a), description.getKey(), matchers);
    }

    private Mock add(final Visitor.Fn fn) {
        state.add(fn);
        return this;
    }

    /**
     * The next call to the method will return o
     * @param o value to return
     * @return this mock for chaining
     */
    public Mock thenReturn(final Object o) {
        return add(a -> o);
    }

    /**
     * The next call to the method will call fn
     * @param fn method to be called. The parameters recieved will be passed to this as an Object[]
     * @return this mock for chaining
     */
    public Mock thenAnswer(final Function<Object[], Object> fn) {
        return add(fn::apply);
    }

    /**
     * The next call to the method will throw an instance of e
     * @param e exception to throw
     * @return this mock for chaining
     */
    public Mock thenThrow(final Class<? extends Throwable> e) {
        return thenThrow(new ObjenesisStd().newInstance(e));
    }

    /**
     * The next call to the method will throw e
     * @param e exception to throw
     * @return this mock for chaining
     */
    public <T extends Throwable> Mock thenThrow(final T e) {
        return add(a -> { throw e; });
    }
}
