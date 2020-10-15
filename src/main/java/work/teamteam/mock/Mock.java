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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Mock {
    private final List<Visitor.Fn> state;

    public Mock(final Visitor last, final Visitor.Description description) {
        this.state = new ArrayList<>();
        last.registerCallback(a -> state.isEmpty() ? null : (state.size() == 1 ? state.get(0) : state.remove(0))
                        .apply(a), Objects.requireNonNull(description));
    }

    private Mock add(final Visitor.Fn fn) {
        state.add(fn);
        return this;
    }

    public Mock thenReturn(final Object o) {
        return add(a -> o);
    }

    public Mock thenAnswer(final Function<Object[], Object> fn) {
        return add(fn::apply);
    }

    public Mock thenThrow(final Class<? extends Throwable> e) {
        return thenThrow(new ObjenesisStd().newInstance(e));
    }

    public <T extends Throwable> Mock thenThrow(final T e) {
        return add(a -> { throw e; });
    }
}
