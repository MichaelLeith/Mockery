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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class Mockery {
    private Mockery() {}
    
    private static <T> Class<? extends T> inject(final Class<T> clazz) throws Exception {
        final ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClazzVisitor visitor = new ClazzVisitor(wr);
        new ClassReader(clazz.getName()).accept(visitor, ClassReader.EXPAND_FRAMES);
        return (Class<? extends T>) ByteClassLoader.defineClass(visitor.getName(), wr.toByteArray());
    }

    private static <T> T init(final T instance,
                              final Class<? extends T> clazz,
                              final T impl) throws Exception{
        final Field visitor = clazz.getDeclaredField("visitor");
        visitor.setAccessible(true);
        visitor.set(instance, new Visitor(impl));
        return instance;
    }

    public static <T> T mock(final Class<T> clazz) throws Exception {
        final Class<? extends T> c = inject(clazz);
        return init(new ObjenesisStd().newInstance(c), c, null);
    }

    public static <T> T spy(final T impl) throws Exception {
        final Class<? extends T> c = inject((Class<T>) impl.getClass());
        return init(new ObjenesisStd().newInstance(c), c, impl);
    }

    public static <T> T spy(final Class<T> clazz, final Object... args) throws Exception {
        for (final Constructor<?> constructor: clazz.getConstructors()) {
            if (constructor.getParameterCount() == args.length) {
                final Class<?> params[] = constructor.getParameterTypes();
                for (int i = 0; i < args.length; i++) {
                    if (!params[i].isAssignableFrom(args[i].getClass())) {
                        break;
                    }
                }
                return spy((T) constructor.newInstance(args));
            }
        }
        throw new RuntimeException("no constructor found for args " + Arrays.asList(args));
    }

    public static Mock when(final Object o) {
        return Tracker.rollbackLast();
    }

    public static <T> T verify(final T o, final long numCalls) {
        getVisitor(o).setVerification(new Verifier(numCalls));
        return o;
    }

    public static <T> void reset(final T o) {
        getVisitor(o).reset();
    }

    private static <T> Visitor getVisitor(final T o) {
        if (o instanceof Trackable) {
            return ((Trackable) o).getVisitor();
        }
        throw new RuntimeException(o.getClass() + " is not a mock");
    }

    public interface Trackable {
        Visitor getVisitor();
    }

    private static final class ClazzVisitor extends ClassVisitor {
        private static final String IMPL_NAME = Type.getInternalName(Visitor.class);
        private static final String IMPL_DESC = Type.getDescriptor(Visitor.class);
        private static final String IMPL = "visitor";
        private static final Type[] PRIMITIVES = new Type[]{
                Type.getType(Boolean.class),
                Type.getType(Character.class),
                Type.getType(Byte.class),
                Type.getType(Short.class),
                Type.getType(Integer.class),
                Type.getType(Float.class),
                Type.getType(Long.class),
                Type.getType(Double.class)
        };
        private String clazz;
        private String parent;
        private final ClassVisitor visitor;

        public ClazzVisitor(final ClassVisitor visitor) {
            super(Opcodes.ASM9, null);
            this.visitor = Objects.requireNonNull(visitor);
        }

        private String getName() {
            return clazz.replace('/', '.');
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
            final boolean isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
            clazz = name + "Mock";
            if (isInterface) {
                visitor.visit(version, Opcodes.ACC_PUBLIC, clazz, "L" + clazz,
                        "java/lang/Object",
                        new String[]{name, Type.getInternalName(Trackable.class)});
                parent = "java/lang/Object";
                createConstructor("()V");
            } else {
                parent = name;
                visitor.visit(version, Opcodes.ACC_PUBLIC, clazz, signature, name,
                        new String[]{Type.getInternalName(Trackable.class)});
            }

            visitor.visitField(Opcodes.ACC_PRIVATE, IMPL, IMPL_DESC, null, null);
            {
                final MethodVisitor vis = visitor.visitMethod(Opcodes.ACC_PUBLIC, "getVisitor",
                        "()L" + IMPL_NAME + ";",
                        null,
                        null);
                vis.visitCode();
                vis.visitVarInsn(Opcodes.ALOAD, 0); // this
                vis.visitFieldInsn(Opcodes.GETFIELD, clazz, IMPL, IMPL_DESC);
                vis.visitInsn(Opcodes.ARETURN);
                vis.visitMaxs(1, 1);
            }
        }

        public void createConstructor(final String descriptor) {
            final MethodVisitor vis = visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
            vis.visitCode();
            vis.visitVarInsn(Opcodes.ALOAD, 0); // this
            final Type[] args = Type.getArgumentTypes(descriptor);
            for (int i = 0; i < args.length; i++) {
                vis.visitVarInsn(args[i].getOpcode(Opcodes.ILOAD), i + 1);
            }
            vis.visitMethodInsn(Opcodes.INVOKESPECIAL, parent, "<init>", descriptor, false);
            vis.visitInsn(Opcodes.RETURN);
            vis.visitMaxs(1, 1);
        }

        @Override
        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String descriptor,
                                         final String signature,
                                         final String[] exceptions) {
            if ("<init>".equals(name)) {
                createConstructor(descriptor);
                return null;
            }
            final MethodVisitor vis = visitor.visitMethod(Opcodes.ACC_PUBLIC, name, descriptor, signature, exceptions);
            vis.visitCode();
            vis.visitVarInsn(Opcodes.ALOAD, 0); // this
            //vis.visitVarInsn(Opcodes.ALOAD, 0); // this
            // call visitors and return using the impl
            vis.visitFieldInsn(Opcodes.GETFIELD, clazz, IMPL, IMPL_DESC);
            vis.visitLdcInsn(name + descriptor);
            final Type[] args = Type.getArgumentTypes(descriptor);
            writeArgsArray(vis, args);
            final Type ret = Type.getReturnType(descriptor);
            final int opcode = ret.getOpcode(Opcodes.IRETURN);
            if (ret.getSort() == Type.ARRAY || ret.getSort() == Type.OBJECT) {
                vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, IMPL_NAME, "invokeL",
                        "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", false);
                vis.visitTypeInsn(Opcodes.CHECKCAST, ret.getInternalName());
            } else {
                vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, IMPL_NAME, "invoke" + ret.getDescriptor(),
                        "(Ljava/lang/String;[Ljava/lang/Object;)" + ret.getDescriptor(), false);
            }
            vis.visitInsn(opcode);
            vis.visitMaxs(2, 1 + args.length);
            return null;
        }

        private void writeLength(final MethodVisitor vis, final int len) {
            if (len <= 5) {
                vis.visitInsn(Opcodes.ICONST_0 + len);
            } else if (len < Byte.MAX_VALUE) {
                vis.visitIntInsn(Opcodes.BIPUSH, len);
            } else if (len < Short.MAX_VALUE) {
                vis.visitIntInsn(Opcodes.SIPUSH, len);
            } else {
                vis.visitLdcInsn(len);
            }
        }

        private void writeArgsArray(final MethodVisitor vis, final Type[] args) {
            writeLength(vis, args.length);
            vis.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            int j = 1;
            for (int i = 0; i < args.length; i++) {
                vis.visitInsn(Opcodes.DUP);
                // write the array index
                writeLength(vis, i);
                final Type arg = args[i];
                // load parameter to stack
                vis.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), j);
                // handling variable width entries (e.g void/long/doubles)
                j += arg.getSize();

                // handle boxing
                if (arg.getSort() == Type.VOID) {
                    vis.visitInsn(Opcodes.RETURN);
                } else if (arg.getSort() < 9) {
                    final Type clazz = PRIMITIVES[arg.getSort() - 1];
                    vis.visitMethodInsn(Opcodes.INVOKESTATIC, clazz.getInternalName(), "valueOf",
                            Type.getMethodDescriptor(clazz, arg), false);
                }
                vis.visitInsn(Opcodes.AASTORE);
            }
        }
    }

    private static final class ByteClassLoader extends ClassLoader {
        static Class<?> defineClass(final String name, final byte[] bytes) {
            return new ByteClassLoader().defineClass(name, bytes, 0, bytes.length);
        }
    }
}
