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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Capture<T> {
    private final Class<T> clazz;
    private final List<Object> capture;

    private Capture(final Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz);
        this.capture = new ArrayList<>();
    }

    public static <T> Capture<T> of(final Class<T> clazz) {
        return new Capture<>(clazz);
    }

    public boolean add(final Object t) {
        synchronized (this) {
            capture.add(t);
        }
        return true;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public T tail() {
        return capture.isEmpty() ? null : (T) capture.get(capture.size() - 1);
    }

    public List<T> captured() {
        return (List<T>) capture;
    }
}
