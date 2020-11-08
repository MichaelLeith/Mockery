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

public final class MethodSummary {
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;

    public MethodSummary(final String name, final String descriptor, final String signature, final String[] exceptions) {
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getSignature() {
        return signature;
    }

    public String[] getExceptions() {
        return exceptions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MethodSummary that = (MethodSummary) o;
        return name.equals(that.name) && descriptor.equals(that.descriptor) &&
                (signature == that.signature || signature != null && signature.equals(that.signature));
    }

    @Override
    public int hashCode() {
        return 29791 + name.hashCode() + descriptor.hashCode() + (signature == null ? 0 : signature.hashCode());
    }
}
