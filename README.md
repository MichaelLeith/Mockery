# Mockery

This is a quick library to learn how Mocking frameworks (like Mockito) in Java (currently 14, but likely works for java8+) work with minor changes.

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

## Benchmarks

Benchmarks are all run on An Intel i74870HQ (a mid 2014 Macbook With OSX 10.14.5), with Mockito equivalents for comparison. Take them with a pinch of salt, they're rough & don't cover all cases.

| Benchmark                                       | Mode    | Cnt  | Score        | Error         | Units |
| ----------------------------------------------- | ------- | ---- | ------------ | ------------- | ----- |
| benchmarkCallMockedMethodMockery                | thrpt   | 10   |  5276808.809 | ±  18245.556  | ops/s |
| benchmarkCallMockedMethodMockito                | thrpt   | 10   |    82936.094 | ±   2057.023  | ops/s |
| benchmarkCallMockedMethodMockery                | thrpt   | 10   |  5276808.809 | ±  18245.556  | ops/s |
| benchmarkCallMockedMethodMockito                | thrpt   | 10   |    82936.094 | ±   2057.023  | ops/s |
| benchmarkCallMockedMethodWithoutHistoryMockery  | thrpt   | 10   |  5527766.895 | ±  20608.835  | ops/s |
| benchmarkCallMockedMethodWithoutHistoryMockito  | thrpt   | 10   |    87380.608 | ±    220.377  | ops/s |
| benchmarkCallMultipleMockedMethodMockery        | thrpt   | 10   |  2824244.780 | ±  28088.784  | ops/s |
| benchmarkCallMultipleMockedMethodMockito        | thrpt   | 10   |    40799.564 | ±    716.411  | ops/s |
| benchmarkCallWhenMockery                        | thrpt   | 10   |  2116378.389 | ±   9271.883  | ops/s |
| benchmarkCallWhenMockito                        | thrpt   | 10   |     3383.156 | ±    128.891  | ops/s |
| benchmarkCallWhenPrimitiveMockery               | thrpt   | 10   |  2890055.036 | ±  13057.888  | ops/s |
| benchmarkCallWhenPrimitiveMockito               | thrpt   | 10   |     2581.798 | ±     68.374  | ops/s |
| benchmarkCreatingMockMockery                    | thrpt   | 10   | 29780347.999 | ± 377141.126  | ops/s |
| benchmarkCreatingMockMockito                    | thrpt   | 10   |  1987538.622 | ±   4016.358  | ops/s |
| benchmarkResetCallSingleArgWhenChainMockery     | thrpt   | 10   |   720096.361 | ±   9254.991  | ops/s |
| benchmarkResetCallSingleArgWhenChainMockito     | thrpt   | 10   |    15808.211 | ±     53.136  | ops/s |
| benchmarkResetCallWhenChainMockery              | thrpt   | 10   |  1096411.960 | ±   2046.979  | ops/s |
| benchmarkResetCallWhenChainMockito              | thrpt   | 10   |    16687.147 | ±     77.776  | ops/s |
| benchmarkResetCallWhenMockery                   | thrpt   | 10   |  2040258.564 | ±   4022.442  | ops/s |
| benchmarkResetCallWhenMockito                   | thrpt   | 10   |    28068.975 | ±    138.278  | ops/s |
| benchmarkResetThenVerifyMockery                 | thrpt   | 10   |  1730232.237 | ±   4652.011  | ops/s |
| benchmarkResetThenVerifyMockito                 | thrpt   | 10   |    28421.756 | ±    164.791  | ops/s |
| benchmarkVerifyMockery                          | thrpt   | 10   |  1748486.989 | ±   4217.442  | ops/s |
| benchmarkVerifyMockito                          | thrpt   | 10   |     1545.149 | ±     14.545  | ops/s |

## TODO

* Cleanup Spy/Matcher logic
* Explore Instrumentation
* Test + Optimize concurrency support
* Unwrap recursion when reading asm?
