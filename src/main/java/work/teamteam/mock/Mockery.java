package work.teamteam.mock;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.util.Objects;

public class Mockery {
    public static final Objenesis OBJENESIS = new ObjenesisStd();

    public static <T> T mock(final Class<T> clazz) throws Exception {
        final ClassReader reader = new ClassReader(clazz.getName());
        final ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClazzVisitor v = new ClazzVisitor(wr);
        reader.accept(v, ClassReader.EXPAND_FRAMES);
        final Class<? extends T> c = (Class<? extends T>) ByteClassLoader.defineClass(v.getName(), wr.toByteArray());
        final T instance = OBJENESIS.newInstance(c);
        final Field visitor = c.getDeclaredField("visitor");
        visitor.setAccessible(true);
        visitor.set(instance, new Visitor());
        return instance;
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

        public ClazzVisitor(final ClassVisitor visitor) {
            super(Opcodes.ASM9, Objects.requireNonNull(visitor));
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
                super.visit(version, Opcodes.ACC_PUBLIC, clazz, "L" + clazz,
                        "java/lang/Object",
                        new String[]{name, Type.getInternalName(Trackable.class)});
                parent = "java/lang/Object";
                createConstructor("()V");
            } else {
                parent = name;
                super.visit(version, Opcodes.ACC_PUBLIC, clazz, signature, name,
                        new String[]{Type.getInternalName(Trackable.class)});
            }

            super.visitField(Opcodes.ACC_PRIVATE, IMPL, IMPL_DESC, null, null);
            {
                final MethodVisitor vis = super.visitMethod(Opcodes.ACC_PUBLIC, "getVisitor",
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
            final MethodVisitor vis = super.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
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
            final MethodVisitor vis = super.visitMethod(Opcodes.ACC_PUBLIC, name, descriptor, signature, exceptions);
            vis.visitCode();
            vis.visitVarInsn(Opcodes.ALOAD, 0); // this

            // call visitors and return using the impl
            vis.visitVarInsn(Opcodes.ALOAD, 0); // this
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
            for (int i = 0; i < args.length; i++) {
                vis.visitInsn(Opcodes.DUP);
                // write the array index
                writeLength(vis, i);
                final Type arg = args[i];
                // load parameter to stack
                vis.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), i + 1);
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
