# Mockery

This is a quick library to learn how Mocking frameworks (like Mockito) in Java (currently 14, but likely works for java8+) work.

## Dependencies

Objenesis - for initializing mocks without calling constructors

asm - for generating mocks

## Quick Example

// creates a mockable object

T t = mock(T.class);

// create a spy proxying t

T t2 = spy(t);

// create a spy implementing T using the T::init() constructor

T t = spy(T.class);

// create a spy using the T::init(Arg1 arg1, Arg2 arg2) constructor

T t2 = spy(T.class, arg1, arg2);P

// return x when T::foo is called

when(t.foo()).thenReturn(x);

// throw when T::foo is called

when(t.foo()).thenThrow(Throwable.class); 

// calls Function<Object[], Object> when T::foo is called

when(t.foo()).thenAnswer(args -> { ... });

// when T::foo is called return x

// next throw y

// then for all further calls return z

when(t.foo()).thenReturn(x)
    .thenThrow(y)
    .thenAnswer(args -> z);

// throw an exception if t.foo() was not called exactly once

verify(t, 1).foo();

// Argument Matchers

when(t.bar(anyInt()).thenReturn(x);

when(t.bar(any(SomeClass.class)).thenReturn(x);

when(t.bar(matches(a -> somePredicate(a)), eq("foo"), anyShort()).thenReturn(x);

## Defaults

If you don't specify the return value mocks return the following defaults based on their type:

boolean - false
byte/char/short/int/long/float/double - 0
Object/array - null

## Limitations

* Can't mock final classes/constructors (this requires Instrumentation)
* Arguments are only captured in-order per-method
* ...

## TODO

* Cleanup Spy/Matcher logic
* Explore Instrumentation
* Test + Optimize concurrency support
* Unwrap recursion when reading asm?
