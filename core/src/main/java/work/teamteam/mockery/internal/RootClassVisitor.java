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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;

public class RootClassVisitor extends MethodCollector {
    private String signature;
    private int version;
    private String name;

    public RootClassVisitor() {
        super(Opcodes.ACC_FINAL, true, new HashSet<>(), new HashSet<>());
    }

    @Override
    public ClassVisitor getChildVisitor() {
        return new MethodCollector(Opcodes.ACC_FINAL, false, constructors, methods);
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.signature = signature;
        this.version = version;
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }
}
