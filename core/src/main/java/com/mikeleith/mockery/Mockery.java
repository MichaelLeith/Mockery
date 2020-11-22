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

package com.mikeleith.mockery;

import com.mikeleith.mockery.internal.MethodSummary;
import com.mikeleith.mockery.internal.Proxy;
import com.mikeleith.mockery.internal.RootClassVisitor;
import com.mikeleith.mockery.internal.Verifier;
import com.mikeleith.mockery.internal.Visitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * Mockery is a library for mocking classes when testing. Mocking can be used:
 * * to create a fake version of classes you don't want to call during tests (e.g external apis)
 * * to replace single methods via "spies"
 * * to control the return value of methods for a class outside the tests scope
 * * to record the number of times certain methods were called with given arguments
 */
public class Mockery {
    private static final Map<Class<?>, Description<?>> TYPE_CACHE = new HashMap<>();
    // counter so that we can clear the cache if needed
    private static int counter = 0;
    private static final ObjenesisStd OBJENESIS_STD = new ObjenesisStd();
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

    public static void clearCache() {
        TYPE_CACHE.clear();
    }

    private Mockery() {}

    /**
     * Resets all global state. Global state is used to simplify the api (i.e to mimic Mockito).
     * In an API such as "when(foo.doesSomething(a, b)).thenReturn" there's no other clean way for the when
     * call to get access to the method called (doesSomething) and the parameters (a, b).
     */
    public static void reset() {
        Visitor.resetLast();
        Matchers.getMatchers();
    }

    /**
     * Creates a new mock implementing the given class. Mocks provide a dummy implementation of the class,
     * with every method returning a default value based on their return type (see Defaults.Impl).
     * @param clazz class to implement
     * @param <T> type of the class
     * @return an instance implementing clazz
     */
    public static <T> T mock(final Class<T> clazz) {
        return mock(clazz, true);
    }

    /**
     * Creates a new mock implementing the given class. Mocks provide a dummy implementation of the class,
     * with every method returning a default value based on their return type. This default is specified by "defaults"
     * @param clazz class to implement
     * @param trackHistory whether to track history
     * @param <T> type of the class
     * @return an instance implementing clazz
     */
    public static <T> T mock(final Class<T> clazz, final boolean trackHistory) {
        return mock(clazz, trackHistory, Defaults.Impl.IMPL);
    }

    /**
     * Creates a new mock implementing the given class. Mocks provide a dummy implementation of the class,
     * with every method returning a default value based on their return type. This default is specified by "defaults"
     * @param clazz class to implement
     * @param defaults lets you specify the default values (per class) returned when methods are called
     * @param <T> type of the class
     * @return an instance implementing clazz
     */
    public static <T> T mock(final Class<T> clazz, final Defaults defaults) {
        return mock(clazz, true, defaults);
    }

    /**
     * Creates a new mock implementing the given class. Mocks provide a dummy implementation of the class,
     * with every method returning a default value based on their return type. This default is specified by "defaults"
     * @param clazz class to implement
     * @param defaults lets you specify the default values (per class) returned when methods are called
     * @param trackHistory whether to track history
     * @param <T> type of the class
     * @return an instance implementing clazz
     */
    public static <T> T mock(final Class<T> clazz, final boolean trackHistory, final Defaults defaults) {
        return build(clazz, null, defaults, trackHistory);
    }

    /**
     * Creates a new spy around impl. With spies you can override methods and record their call count. Any method
     * you don't override will invoke the equivalent method in impl.
     * @param impl the object to wrap
     * @param <T> type of the class
     * @return an instance spying on impl
     */
    public static <T> T spy(final T impl) {
        return build(impl.getClass(), impl, Defaults.Impl.IMPL, true);
    }

    public static <T> T spy(final T impl, final boolean trackHistory) {
        return build(impl.getClass(), impl, Defaults.Impl.IMPL, trackHistory);
    }

    /**
     * Creates a new spy. This is equivalent to spy(new clazz(args...)). With spies you can override methods and
     * record their call count. Any method you don't override will invoke the equivalent method in impl.
     * @param clazz the class to spy on
     * @param args args to pass to the instance of clazz we're instanciating
     * @param <T> type of the class
     * @return an instance spying on clazz
     */
    public static <T> T spy(final Class<T> clazz, final Object... args) {
        return spy(clazz, true, args);
    }

    /**
     * Creates a new spy. This is equivalent to spy(new clazz(args...)). With spies you can override methods and
     * record their call count. Any method you don't override will invoke the equivalent method in impl.
     * @param clazz the class to spy on
     * @param trackHistory whether to track history
     * @param args args to pass to the instance of clazz we're instanciating
     * @param <T> type of the class
     * @return an instance spying on clazz
     */
    public static synchronized <T> T spy(final Class<T> clazz, final boolean trackHistory, final Object... args) {
        final Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            if (constructors[i].getParameterCount() == args.length) {
                final Class<?>[] params = constructors[i].getParameterTypes();
                for (int j = 0; j < args.length; j++) {
                    if (!params[j].isAssignableFrom(args[j].getClass())) {
                        break;
                    }
                }
                try {
                    constructors[i].setAccessible(true);
                    return spy(clazz.cast(constructors[i].newInstance(args)), trackHistory);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                } finally {
                    constructors[i].setAccessible(false);
                }
            }
        }
        throw new RuntimeException("no constructor found for args " + Arrays.asList(args));
    }

    /**
     * Used to override the return value of a specific method + arguments.
     * e.g when(foo.doSomething(any(), eq("foo")).thenReturn(100);
     * In this example whenever foo.doSomething(..., "foo") is called we will return 100.
     * Any calls not matching the two predicates ("any()" and "eq("foo")") will return the default
     * @param o object to override. This is provided for convenience, we actually use the last called method of any
     *          mocked object.
     * @param <T> return type of the method being configured
     * @return a Mock for specifying your overrides
     */
    @SuppressWarnings("unused")
    public static <T> Mock<T> when(final T o) {
        return Visitor.rollbackLast();
    }

    /**
     * Used to verify the given method was called exactly "numCalls" times. This returns o, and the next method call
     * will be verified.
     * e.g verify(foo, 1).doSomething(any());
     * @param o object we want to verify the next call of
     * @param numCalls number of times we expect the next method to have been called
     * @param <T> generic type of o
     * @return o
     */
    public static <T> T verify(final T o, final int numCalls) {
        getVisitor(o).setVerification(new Verifier(Times.eq(numCalls)));
        return o;
    }

    /**
     * Used to verify the given method was called N times. This returns o, and the next method call
     * will be verified.
     * e.g verify(foo, Times.le(1)).doSomething(any());
     *
     * See Times for common predicates that can be used.
     * @param o object we want to verify the next call of
     * @param predicate predicate that will be called with the number of times this method has been seen
     * @param <T> generic type of o
     * @return o
     */
    public static <T> T verify(final T o, final IntPredicate predicate) {
        getVisitor(o).setVerification(new Verifier(predicate));
        return o;
    }

    /**
     * Resets internal state such as the method call history associated with the mock/spy o.
     * @param o object to reset
     */
    public static void reset(final Object o) {
        getVisitor(o).reset();
    }

    /**
     * Creates a class that extends clazz with the requirements for mocking/spying (implements Trackable)
     * @param clazz class to extend
     * @param <T> generic type of the class
     * @return A class extending T and implementing Trackable
     * @throws Exception an exception if we fail to extend the class, e.g if it is final
     */
    @SuppressWarnings("unchecked")
    private static <T> Description<?> inject(final Class<T> clazz) throws Exception {
        final RootClassVisitor visitor = new RootClassVisitor();
        new ClassReader(clazz.getName()).accept(visitor, ClassReader.EXPAND_FRAMES);

        final ClassWriter wr = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final String name = visitor.getName() + "Mock" + counter++;
        final String parent = writeClass(wr, clazz, name, visitor);

        // implement trackable (i.e create a new field for the visitor and method getVisitor)
        wr.visitField(Opcodes.ACC_PRIVATE, IMPL, IMPL_DESC, null, null);
        writeGetVisitor(wr, name);
        writeSetVisitor(wr, name);

        final Set<MethodSummary> constructors = visitor.getConstructors();
        for (final MethodSummary constructor: constructors) {
            writeConstructor(wr, parent, constructor);
        }

        final Set<MethodSummary> methods = visitor.getMethods();
        for (final MethodSummary method: methods) {
            writeMethod(wr, name, method);
        }
        wr.visitEnd();
        return new Description<>((Class<?>) loadClass(clazz, name.replace('/', '.'), wr.toByteArray()));
    }

    /**
     * Writes a new class header for clazz
     * @param wr writer
     * @param clazz class being extended
     * @param name name of the class
     * @param visitor visitor information about clazz
     */
    private static String writeClass(final ClassWriter wr,
                                     final Class<?> clazz,
                                     final String name,
                                     final RootClassVisitor visitor) {
        final String[] interfaces;
        final String parent;
        final String signature;
        if (clazz.isInterface()) {
            parent = "java/lang/Object";
            signature = "L" + name;
            interfaces = new String[]{visitor.getName(), Type.getInternalName(Trackable.class)};
        } else {
            parent = visitor.getName();
            signature = visitor.getSignature();
            interfaces = new String[]{Type.getInternalName(Trackable.class)};
        }
        wr.visit(visitor.getVersion(), Opcodes.ACC_PUBLIC & Opcodes.ACC_SYNTHETIC, name, signature, parent, interfaces);
        return parent;
    }

    /**
     * Writes getVisitor(Sentinel);
     * @param wr writer
     * @param clazz class name
     */
    private static void writeGetVisitor(final ClassWriter wr, final String clazz) {
        final MethodVisitor vis = wr.visitMethod(Opcodes.ACC_PUBLIC, "getVisitor",
                "(" + Type.getDescriptor(Sentinel.class) + ")L" + IMPL_NAME + ";",
                null,
                null);
        vis.visitCode();
        vis.visitVarInsn(Opcodes.ALOAD, 0); // this
        vis.visitFieldInsn(Opcodes.GETFIELD, clazz, IMPL, IMPL_DESC);
        vis.visitInsn(Opcodes.ARETURN);
        vis.visitMaxs(1, 1);
        wr.visitEnd();
    }

    /**
     * Writes setVisitor(Visitor);
     * @param wr writer
     * @param clazz class name
     */
    private static void writeSetVisitor(final ClassWriter wr, final String clazz) {
        final MethodVisitor vis = wr.visitMethod(Opcodes.ACC_PUBLIC, "setVisitor",
                "(" + Type.getDescriptor(Visitor.class) + ")V",
                null,
                null);
        vis.visitCode();
        vis.visitVarInsn(Opcodes.ALOAD, 0); // this
        vis.visitVarInsn(Opcodes.ALOAD, 1);
        vis.visitFieldInsn(Opcodes.PUTFIELD, clazz, IMPL, IMPL_DESC);
        vis.visitInsn(Opcodes.RETURN);
        vis.visitMaxs(1, 1);
        wr.visitEnd();
    }

    /**
     * Writes a stub constructor that just calls super
     * @param wr writer
     * @param parent parent class name
     * @param summary method summary
     */
    private static void writeConstructor(final ClassWriter wr, final String parent, final MethodSummary summary) {
        final MethodVisitor vis = wr.visitMethod(Opcodes.ACC_PUBLIC & Opcodes.ACC_SYNTHETIC,
                summary.getName(), summary.getDescriptor(), summary.getSignature(), summary.getExceptions());
        vis.visitCode();
        vis.visitVarInsn(Opcodes.ALOAD, 0); // this
        final Type[] args = Type.getArgumentTypes(summary.getDescriptor());
        for (int i = 0; i < args.length; i++) {
            vis.visitVarInsn(args[i].getOpcode(Opcodes.ILOAD), i + 1);
        }
        vis.visitMethodInsn(Opcodes.INVOKESPECIAL, parent, "<init>", summary.getDescriptor(), false);
        vis.visitInsn(Opcodes.RETURN);
        vis.visitMaxs(1, 1);
        wr.visitEnd();
    }

    /**
     * Writes a stub method that proxies to the visitor
     * @param wr writer
     * @param clazz class name
     * @param summary method summary
     */
    private static void writeMethod(final ClassWriter wr, final String clazz, final MethodSummary summary) {
        final String descriptor = summary.getDescriptor();
        final String key = summary.getName() + descriptor;
        final String var = key.replaceAll("[()/\\[]", "_").replace(';', '-');
        wr.visitField(Opcodes.ACC_PRIVATE & Opcodes.ACC_SYNTHETIC, var, Type.getDescriptor(List.class), null, null).visitEnd();

        // create a shim that loads all arguments into an Object[] and passes them to
        // T Visitor::run(String name+descriptor, Class<T> returnType, Object[] args);
        final MethodVisitor vis = wr.visitMethod(Opcodes.ACC_PUBLIC, summary.getName(),
                descriptor, summary.getSignature(), summary.getExceptions());
        vis.visitCode();

        // null check
        vis.visitVarInsn(Opcodes.ALOAD, 0);
        vis.visitInsn(Opcodes.DUP);
        vis.visitInsn(Opcodes.MONITORENTER); // synchronized so it's safe to create a new list
        vis.visitFieldInsn(Opcodes.GETFIELD, clazz, var, Type.getDescriptor(List.class));
        final Label nonNull = new Label();
        vis.visitJumpInsn(Opcodes.IFNONNULL, nonNull);

        vis.visitVarInsn(Opcodes.ALOAD, 0); // this
        vis.visitInsn(Opcodes.DUP);
        // call visitors and return using the impl
        vis.visitFieldInsn(Opcodes.GETFIELD, clazz, IMPL, IMPL_DESC);
        vis.visitLdcInsn(key);
        vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, IMPL_NAME, "init",
                "(Ljava/lang/String;)Ljava/util/List;", false);
        vis.visitFieldInsn(Opcodes.PUTFIELD, clazz, var, Type.getDescriptor(List.class));

        // else
        vis.visitLabel(nonNull);
        vis.visitVarInsn(Opcodes.ALOAD, 0); // this
        vis.visitInsn(Opcodes.DUP);
        vis.visitInsn(Opcodes.MONITOREXIT); // synchronized exit

        // call visitors and return using the impl
        vis.visitFieldInsn(Opcodes.GETFIELD, clazz, IMPL, IMPL_DESC);
        vis.visitVarInsn(Opcodes.ALOAD, 0);
        vis.visitFieldInsn(Opcodes.GETFIELD, clazz, var, Type.getDescriptor(List.class));
        vis.visitLdcInsn(key);

        final Type ret = Type.getReturnType(descriptor);
        pushClass(vis, ret);
        final Type[] args = Type.getArgumentTypes(descriptor);
        if (args.length == 0) {
            vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, IMPL_NAME, "run",
                    "(Ljava/util/List;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
        } else {
            writeArgsArray(vis, args);
            vis.visitMethodInsn(Opcodes.INVOKEVIRTUAL, IMPL_NAME, "run",
                    "(Ljava/util/List;Ljava/lang/String;Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        }
        cast(vis, ret);
        vis.visitInsn(ret.getOpcode(Opcodes.IRETURN));
        vis.visitMaxs(2, 1 + args.length);
        wr.visitEnd();
    }

    /**
     * Creates a new instance of clazz that's setup for mocking/spying
     * @param clazz class to instantiate
     * @param impl implementation of the class to spy on, or null
     * @param defaults default return types to use in the mock
     * @param <T> type of the class
     * @return a new instance of clazz we can mock
     */
    @SuppressWarnings("unchecked")
    private static <T> T build(final Class<?> clazz,
                               final T impl,
                               final Defaults defaults,
                               final boolean trackHistory) {
        try {
            Description<?> mock;
            // using synchronized because we assume that this has typically single threaded
            // access, so a ConcurrentHashMap is overkill
            synchronized (TYPE_CACHE) {
                mock = TYPE_CACHE.get(clazz);
                if (mock == null) {
                    mock = inject(clazz);
                    TYPE_CACHE.put(clazz, mock);
                }
            }
            final T instance = OBJENESIS_STD.newInstance((Class<T>) mock.clazz);
            final Proxy<T> proxy = impl == null ? null : (Proxy<T>) mock.proxy(impl);
            ((Trackable) instance).setVisitor(new Visitor<>(proxy, defaults, trackHistory));
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the visitor related to a mock, or throws if o isn't Trackable. This is done at runtime to simplify the
     * external api.
     * @param o object to get the visitor from
     * @return the visitor associated with o
     */
    private static Visitor<?> getVisitor(final Object o) {
        if (o instanceof Trackable) {
            return ((Trackable) o).getVisitor(Sentinel.SENTINEL);
        }
        throw new RuntimeException(o.getClass() + " is not a mock");
    }

    /**
     * Base interface to access all tracking data associated with a mock/spy
     */
    public interface Trackable {
        Visitor<?> getVisitor(final Sentinel s);
        void setVisitor(final Visitor<?> visitor);
    }

    /**
     * Sentinel class to guarantee that the getVisitor interface is unique
     */
    private static final class Sentinel {
        private static final Sentinel SENTINEL = new Sentinel();
        private Sentinel() {}
    }

    /**
     * injects a Class<?> (including primitives) matching ret to the stack
     *
     * @param vis visitor to add bytecode to
     * @param ret type we want to get a class for
     */
    private static void pushClass(final MethodVisitor vis, final Type ret) {
        if (ret.getSort() < PRIMITIVES.length) {
            //GETSTATIC java/lang/Void.TYPE : Ljava/lang/Class;
            vis.visitFieldInsn(Opcodes.GETSTATIC, PRIMITIVES[ret.getSort()].getInternalName(), "TYPE", "Ljava/lang/Class;");
        } else {
            //LDC Ljava/lang/String;.class;
            vis.visitLdcInsn(ret);
        }
    }

    /**
     * injects bytecode casting the previous Object on the stack to ret (handling primitive unboxing)
     *
     * @param vis visitor to add bytecode to
     * @param ret type we want to cast to
     */
    private static void cast(final MethodVisitor vis, final Type ret) {
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

    /**
     * Writes the given length to the stack
     *
     * @param vis visitor to add bytecode to
     * @param len length to write to the stack
     */
    private static void writeLength(final MethodVisitor vis, final int len) {
        if (len <= 5) {
            vis.visitInsn(Opcodes.ICONST_0 + len);
        } else if (len < Byte.MAX_VALUE) {
            vis.visitIntInsn(Opcodes.BIPUSH, len);
        } else {
            throw new RuntimeException("Unsupported number of parameters (the jdk specs support up to 255 params");
        }
    }

    /**
     * reads arguments from the stack and writes them back as an Object[] array, based on the type args
     *
     * @param vis  visitor to add bytecode to
     * @param args type of args to read and write to the stack
     */
    private static void writeArgsArray(final MethodVisitor vis, final Type[] args) {
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
            if (arg.getSort() != Type.VOID && arg.getSort() < PRIMITIVES.length) {
                final Type clazz = PRIMITIVES[arg.getSort()];
                vis.visitMethodInsn(Opcodes.INVOKESTATIC, clazz.getInternalName(), "valueOf",
                        Type.getMethodDescriptor(clazz, arg), false);
            }
            vis.visitInsn(Opcodes.AASTORE);
        }
    }

    /**
     * Loads a new class called "name" from bytes
     * @param parent parent class name extends/implements
     * @param name name of the class
     * @param b bytes describing the class
     * @return a new class
     */
    private static Class<?> loadClass(final Class<?> parent, final String name, final byte[] b) throws Exception {
        final Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass",
                String.class, byte[].class, int.class, int.class);
        try {
            method.setAccessible(true);
            return (Class<?>) method.invoke(parent.getClassLoader(), name, b, 0, b.length);
        } finally {
            method.setAccessible(false);
        }
    }

    private static final class Description<T> {
        final Class<T> clazz;
        Map<Class, Function> entries = null;

        public Description(final Class<T> clazz) {
            this.clazz = Objects.requireNonNull(clazz);
        }

        @SuppressWarnings("unchecked")
        public Proxy<T> proxy(final Object impl) {
            if (entries == null) {
                entries = new HashMap<>();
            }
            Function fn = entries.get(impl.getClass());
            if (fn == null) {
                fn = Proxy.build(impl.getClass());
                entries.put(impl.getClass(), fn);
            }
            return (Proxy<T>) fn.apply(impl);
        }
    }
}
