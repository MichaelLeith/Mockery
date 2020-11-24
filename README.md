# Mockery

[![Maven](https://img.shields.io/maven-central/v/com.mikeleith.mockery/mockery.svg)](https://search.maven.org/search?q=com.mikeleith.mockery)
[![Version](https://img.shields.io/badge/version-1.0.0-orange.svg)](https://github.com/MichaelLeith/Mockery/tree/v1.0.0)
[![Apache 2 License](https://img.shields.io/badge/license-Apache2-blue.svg)](https://github.com/MichaelLeith/Mockery/tree/v1.0.0/LICENSE)
[![Javadoc](https://img.shields.io/badge/javadoc-1.0.0-green.svg)](https://mikeleith.com/Mockery/)

A Minimal mocking framework for Java 8+.

## Dependencies

Objenesis - for initializing mocks without calling constructors

asm - for generating mocks

## Quick Example

Mockery mirrors a lot of the Mockito interface. If you know Mockito, you should be good to go.

```java
// creates a mockable object
T t = mock(T.class);

// create a spy proxying t
T t2 = spy(t);

// create a spy implementing T using the T::init() constructor
T t = spy(T.class);

// create a spy using the T::init(Arg1 arg1, Arg2 arg2) constructor
T t2 = spy(T.class, arg1, arg2);

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
verify(t, eq(1)).foo(); // x == 1
verify(t, gt(1)).foo(); // x > 1
verify(t, ge(1)).foo(); // x >= 1
verify(t, lt(1)).foo(); // x < 1
verify(t, le(1)).foo(); // x <= 1
verify(t, i -> i > 4 && i < 2).foo();

// Argument Matchers
when(t.bar(anyInt()).thenReturn(x);
when(t.bar(any(SomeClass.class)).thenReturn(x);
when(t.bar(matches(a -> somePredicate(a)), eq("foo"), anyShort()).thenReturn(x);
```

### Annotations

```java
class MyTest {
    // Inject a mock
    @Mock Impl foo;
    // Inject a spy around new Impl2();
    @Spy Impl2 foo2;
    // Inject a spy(foo3)
    @Spy Impl2 foo3 = new Impl2(...);
    // Inject other fields in MyTest into the best fitting Impl3 constructor (see MockeryInject.java)
    // Currently we don't guarantee that @InjectMocks fields will be initialized (i.e we don't resolve the dag of constructors), 
    // so use nested injection at your own peril
    @InjectMocks Impl3 foo4;
    // Injects an argument captor
    @Captor Capture<String> stringCapture;
    
    @Before
    public void setUp() {
        MockeryInject.inject(this);
    }
}
```

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

Currently all benchmarks show at least a 10x speedup over the equivalent Mockito, with some significantly more than that (up to 1000x).

| Benchmark                                       | Mode  | Cnt  | Score            | Error             | Units |
| :---------------------------------------------- | ----- | ---: | ---------------: | ----------------: | ----- |
| benchmarkCallMockedMethodMockery                | thrpt |   20 |  ``5331026.301`` | ±  ``95661.425``  | ops/s |
| benchmarkCallMockedMethodMockito                | thrpt |   20 |    ``85706.094`` | ±   ``2898.825``  | ops/s |
| benchmarkCallMockedMethodWithoutHistoryMockery  | thrpt |   20 |  ``5264662.096`` | ± ``134407.460``  | ops/s |
| benchmarkCallMockedMethodWithoutHistoryMockito  | thrpt |   20 |    ``90093.499`` | ±   ``2896.912``  | ops/s |
| benchmarkCallMultipleMockedMethodMockery        | thrpt |   20 |  ``2926466.444`` | ±  ``54314.172``  | ops/s |
| benchmarkCallMultipleMockedMethodMockito        | thrpt |   20 |    ``42731.195`` | ±   ``1367.676``  | ops/s |
| benchmarkCallWhenMockery                        | thrpt |   20 |  ``2014668.259`` | ±  ``37970.570``  | ops/s |
| benchmarkCallWhenMockito                        | thrpt |   20 |     ``2467.884`` | ±     ``29.706``  | ops/s |
| benchmarkCallWhenPrimitiveMockery               | thrpt |   20 |  ``2645382.820`` | ±  ``34503.139``  | ops/s |
| benchmarkCallWhenPrimitiveMockito               | thrpt |   20 |     ``1893.185`` | ±     ``22.615``  | ops/s |
| benchmarkCreatingMockMockery                    | thrpt |   20 | ``30420720.292`` | ± ``583631.930``  | ops/s |
| benchmarkCreatingMockMockito                    | thrpt |   20 |  ``1932487.150`` | ±  ``39684.368``  | ops/s |
| benchmarkResetCallSingleArgWhenChainMockery     | thrpt |   20 |   ``718397.012`` | ±  ``10915.708``  | ops/s |
| benchmarkResetCallSingleArgWhenChainMockito     | thrpt |   20 |    ``16573.017`` | ±    ``476.281``  | ops/s |
| benchmarkResetCallWhenChainMockery              | thrpt |   20 |  ``1050775.816`` | ±  ``13502.864``  | ops/s |
| benchmarkResetCallWhenChainMockito              | thrpt |   20 |    ``17258.047`` | ±    ``554.500``  | ops/s |
| benchmarkResetCallWhenMockery                   | thrpt |   20 |  ``2028126.288`` | ±  ``38689.473``  | ops/s |
| benchmarkResetCallWhenMockito                   | thrpt |   20 |    ``28509.275`` | ±   ``1007.732``  | ops/s |
| benchmarkResetThenVerifyMockery                 | thrpt |   20 |  ``1738147.820`` | ±  ``14056.533``  | ops/s |
| benchmarkResetThenVerifyMockito                 | thrpt |   20 |    ``29528.976`` | ±   ``1011.451``  | ops/s |
| benchmarkSpyCreationMockery                     | thrpt |   20 | ``23252810.694`` | ±  ``91828.124``  | ops/s |
| benchmarkSpyCreationMockito                     | thrpt |   20 |  ``1806122.406`` | ±  ``30543.991``  | ops/s |
| benchmarkSpyMockery                             | thrpt |   20 |   ``898540.079`` | ±   ``9542.878``  | ops/s |
| benchmarkSpyMockito                             | thrpt |   20 |    ``17783.933`` | ±    ``587.604``  | ops/s |
| benchmarkVerifyMockery                          | thrpt |   20 |  ``1729869.162`` | ±  ``21367.168``  | ops/s |
| benchmarkVerifyMockito                          | thrpt |   20 |     ``1124.063`` | ±      ``4.414``  | ops/s |

## TODO

* Cleanup Spy/Matcher logic
* Explore Instrumentation
* Test + optimize concurrency support more
* Performance tuning (especially initial mock generation)
* Provide default mocked equals/hashCode methods (because Mockito does)
