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

package com.mikeleith.mockery.internal;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Set;

class MethodCollector extends ClassVisitor {
    private final boolean matchConstructors;
    protected final Set<MethodSummary> constructors;
    protected final Set<MethodSummary> methods;
    private final int ignore;

    public MethodCollector(final int ignore,
                           final boolean matchConstructors,
                           final Set<MethodSummary> constructors,
                           final Set<MethodSummary> methods) {
        super(Opcodes.ASM9, null);
        this.matchConstructors = matchConstructors;
        this.constructors = constructors;
        this.methods = methods;
        this.ignore = ignore;
    }

    public ClassVisitor getChildVisitor() {
        return this;
    }

    public Set<MethodSummary> getConstructors() {
        return constructors;
    }

    public Set<MethodSummary> getMethods() {
        return methods;
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {
        if ((access & Opcodes.ACC_FINAL) != 0) {
            throw new RuntimeException("final provided, expected a concrete class");
        }
        try {
            ClassVisitor vis = null;
            if (interfaces.length != 0) {
                vis = getChildVisitor();
                for (int i = 0; i < interfaces.length; i++) {
                    // there's no root interface, so don't have to worry about hitting java/lang methods
                    new ClassReader(interfaces[i]).accept(vis, ClassReader.EXPAND_FRAMES);
                }
            }
            if (superName != null) {
                new ClassReader(superName).accept(vis == null ? getChildVisitor() : vis, ClassReader.EXPAND_FRAMES);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String name,
                                     final String descriptor,
                                     final String signature,
                                     final String[] exceptions) {
        if ("<init>".equals(name)) {
            if (matchConstructors) {
                constructors.add(new MethodSummary(name, descriptor, signature, exceptions));
            }
        } else if (((access & ignore) == 0)) {
            methods.add(new MethodSummary(name, descriptor, signature, exceptions));
        }
        return null;
    }
}
