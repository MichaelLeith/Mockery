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
import work.teamteam.mock.internal.Tracker;
import work.teamteam.mock.internal.Verifier;
import work.teamteam.mock.internal.Visitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class Mockery {
    private Mockery() {}
    
    private static <T> Class<?> inject(final Class<T> clazz) throws Exception {
        final ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClazzVisitor visitor = new ClazzVisitor(wr);
        new ClassReader(clazz.getName()).accept(visitor, ClassReader.EXPAND_FRAMES);
        return ByteClassLoader.defineClass(visitor.clazz.replace('/', '.'), wr.toByteArray());
    }

    private static <T> T init(final T instance,
                              final Class<?> clazz,
                              final T impl,
                              final Defaults defaults) throws Exception{
        final Field visitor = clazz.getDeclaredField("visitor");
        visitor.setAccessible(true);
        visitor.set(instance, new Visitor<>(impl, defaults));
        return instance;
    }

    private static <T> T build(final Class<?> clazz, final T impl, final Defaults defaults) {
        try {
            final Class<?> c = inject(clazz);
            return init((T) new ObjenesisStd().newInstance(c), c, impl, defaults);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T mock(final Class<T> clazz) {
        return mock(clazz, Defaults.Impl.IMPL);
    }

    public static <T> T mock(final Class<T> clazz, final Defaults defaults) {
        return build(clazz, null, defaults);
    }

    public static <T> T spy(final T impl) {
        return build(impl.getClass(), impl, Defaults.Impl.IMPL);
    }

    public static <T> T spy(final Class<T> clazz, final Object... args) throws Exception {
        for (final Constructor<?> constructor: clazz.getConstructors()) {
            if (constructor.getParameterCount() == args.length) {
                final Class<?>[] params = constructor.getParameterTypes();
                for (int i = 0; i < args.length; i++) {
                    if (!params[i].isAssignableFrom(args[i].getClass())) {
                        break;
                    }
                }
                return spy(clazz.cast(constructor.newInstance(args)));
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

    private static <T> Visitor<?> getVisitor(final T o) {
        if (o instanceof Trackable) {
            return ((Trackable) o).getVisitor(Sentinel.SENTINEL);
        }
        throw new RuntimeException(o.getClass() + " is not a mock");
    }

    public interface Trackable {
        Visitor<?> getVisitor(final Sentinel s);
    }

    // Sentinel class to guarantee that the getVisitor interface is unique
    private static final class Sentinel {
        private static final Sentinel SENTINEL = new Sentinel();
        private Sentinel() {}
    }

    private static final class ClazzVisitor extends ClassVisitor {
        private static final String IMPL_NAME = Type.getInternalName(Visitor.class);
        private static final String IMPL_DESC = Type.getDescriptor(Visitor.class);
        private static final String IMPL = "visitor";
        private static final Type[] PRIMITIVES = new Type[]{
                Type.getType(Void.class),
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
                        "(" + Type.getDescriptor(Sentinel.class) + ")L" + IMPL_NAME + ";",
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
            // call visitors and return using the impl
            vis.visitFieldInsn(Opcodes.GETFIELD, clazz, IMPL, IMPL_DESC);
            vis.visitLdcInsn(name + descriptor);

            final Type ret = Type.getReturnType(descriptor);
            pushClass(vis, ret);
            final Type[] args = Type.getArgumentTypes(descriptor);
            writeArgsArray(vis, args);
            vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, IMPL_NAME, "run",
                    "(Ljava/lang/String;Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;", false);
            cast(vis, ret);
            vis.visitInsn(ret.getOpcode(Opcodes.IRETURN));
            vis.visitMaxs(2, 1 + args.length);
            return null;
        }

        private void pushClass(final MethodVisitor vis, final Type ret) {
            if (ret.getSort() < PRIMITIVES.length) {
                //GETSTATIC java/lang/Void.TYPE : Ljava/lang/Class;
                vis.visitFieldInsn(Opcodes.GETSTATIC, PRIMITIVES[ret.getSort()].getInternalName(), "TYPE", "Ljava/lang/Class;");
            } else {
                //LDC Ljava/lang/String;.class;
                vis.visitLdcInsn(ret);
            }
        }

        private void cast(final MethodVisitor vis, final Type ret) {
            if (ret.getSort() != Type.VOID) {
                if (ret.getSort() >= PRIMITIVES.length) {
                    vis.visitTypeInsn(Opcodes.CHECKCAST, ret.getInternalName());
                } else {
                    // @todo: worth making this safer. Just now it's not fully type safe. See testThrowOnBadCallback
                    // CHECKCAST java/lang/Integer
                    // INVOKEVIRTUAL java/lang/Integer.intValue ()I
                    final Type clazz = PRIMITIVES[ret.getSort()];
                    vis.visitTypeInsn(Opcodes.CHECKCAST, clazz.getInternalName());
                    vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, clazz.getInternalName(), ret.getClassName() + "Value",
                            "()" + ret.getInternalName(), false);
                }
            }
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
                } else if (arg.getSort() < PRIMITIVES.length) {
                    final Type clazz = PRIMITIVES[arg.getSort()];
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
