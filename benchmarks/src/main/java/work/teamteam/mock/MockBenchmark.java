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

package work.teamteam.mock;import org.mockito.Mockito;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/*
 Benchmark                                                      Mode  Cnt         Score        Error  Units
MockBenchmark.benchmarkCallMockedMethodMockery                thrpt   20   5331026.301 ±  95661.425  ops/s
MockBenchmark.benchmarkCallMockedMethodMockito                thrpt   20     85706.094 ±   2898.825  ops/s
MockBenchmark.benchmarkCallMockedMethodWithoutHistoryMockery  thrpt   20   5264662.096 ± 134407.460  ops/s
MockBenchmark.benchmarkCallMockedMethodWithoutHistoryMockito  thrpt   20     90093.499 ±   2896.912  ops/s
MockBenchmark.benchmarkCallMultipleMockedMethodMockery        thrpt   20   2926466.444 ±  54314.172  ops/s
MockBenchmark.benchmarkCallMultipleMockedMethodMockito        thrpt   20     42731.195 ±   1367.676  ops/s
MockBenchmark.benchmarkCallWhenMockery                        thrpt   20   2014668.259 ±  37970.570  ops/s
MockBenchmark.benchmarkCallWhenMockito                        thrpt   20      2467.884 ±     29.706  ops/s
MockBenchmark.benchmarkCallWhenPrimitiveMockery               thrpt   20   2645382.820 ±  34503.139  ops/s
MockBenchmark.benchmarkCallWhenPrimitiveMockito               thrpt   20      1893.185 ±     22.615  ops/s
MockBenchmark.benchmarkCreatingMockMockery                    thrpt   20  30420720.292 ± 583631.930  ops/s
MockBenchmark.benchmarkCreatingMockMockito                    thrpt   20   1932487.150 ±  39684.368  ops/s
MockBenchmark.benchmarkResetCallSingleArgWhenChainMockery     thrpt   20    718397.012 ±  10915.708  ops/s
MockBenchmark.benchmarkResetCallSingleArgWhenChainMockito     thrpt   20     16573.017 ±    476.281  ops/s
MockBenchmark.benchmarkResetCallWhenChainMockery              thrpt   20   1050775.816 ±  13502.864  ops/s
MockBenchmark.benchmarkResetCallWhenChainMockito              thrpt   20     17258.047 ±    554.500  ops/s
MockBenchmark.benchmarkResetCallWhenMockery                   thrpt   20   2028126.288 ±  38689.473  ops/s
MockBenchmark.benchmarkResetCallWhenMockito                   thrpt   20     28509.275 ±   1007.732  ops/s
MockBenchmark.benchmarkResetThenVerifyMockery                 thrpt   20   1738147.820 ±  14056.533  ops/s
MockBenchmark.benchmarkResetThenVerifyMockito                 thrpt   20     29528.976 ±   1011.451  ops/s
MockBenchmark.benchmarkSpyCreationMockery                     thrpt   20  23252810.694 ±  91828.124  ops/s
MockBenchmark.benchmarkSpyCreationMockito                     thrpt   20   1806122.406 ±  30543.991  ops/s
MockBenchmark.benchmarkSpyMockery                             thrpt   20    898540.079 ±   9542.878  ops/s
MockBenchmark.benchmarkSpyMockito                             thrpt   20     17783.933 ±    587.604  ops/s
MockBenchmark.benchmarkVerifyMockery                          thrpt   20   1729869.162 ±  21367.168  ops/s
MockBenchmark.benchmarkVerifyMockito                          thrpt   20      1124.063 ±      4.414  ops/s
 */
@Warmup(iterations = 10, time = 10)
@Measurement(iterations = 10, time = 10)
@Fork(2)
public class MockBenchmark {
    @State(Scope.Benchmark)
    public static class Mocks {
        Target mockeryTarget;
        Target mockitoTarget;
        Target mockeryDisabledTarget;
        Target mockitoDisabledTarget;
        Target mockerySpy;
        Target mockitoSpy;

        @Setup(Level.Trial)
        public void setUp() {
            mockeryTarget = Mockery.mock(Target.class);
            mockitoTarget = Mockito.mock(Target.class);

            mockeryDisabledTarget = Mockery.mock(Target.class, false);
            mockitoDisabledTarget = Mockito.mock(Target.class, Mockito.withSettings().stubOnly());

            mockerySpy = Mockery.spy(new Impl());
            mockitoSpy = Mockito.spy(new Impl());
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            Mockery.reset();
            Mockery.reset(mockeryTarget);
            Mockito.reset(mockitoTarget);
            Mockery.reset(mockeryDisabledTarget);
            Mockito.reset(mockitoDisabledTarget);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkSpyCreationMockery(final Blackhole blackhole) {
        blackhole.consume(Mockery.spy(new Impl()));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkSpyCreationMockito(final Blackhole blackhole) {
        blackhole.consume(Mockito.spy(new Impl()));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkSpyMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.reset(mocks.mockerySpy);
        Mockery.when(mocks.mockerySpy.doSomething()).thenReturn("foo");
        blackhole.consume(mocks.mockerySpy.doSomething());
        blackhole.consume(mocks.mockerySpy.doSomethingElse());
        blackhole.consume(mocks.mockerySpy.doSomething("foo"));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkSpyMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.reset(mocks.mockitoSpy);
        Mockito.when(mocks.mockitoSpy.doSomething()).thenReturn("foo");
        blackhole.consume(mocks.mockitoSpy.doSomething());
        blackhole.consume(mocks.mockitoSpy.doSomethingElse());
        blackhole.consume(mocks.mockitoSpy.doSomething("foo"));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetCallWhenChainMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.reset(mocks.mockeryTarget);
        Mockery.when(mocks.mockeryTarget.doSomething()).thenReturn("foo")
                .thenAnswer(a -> "bar")
                .thenReturn("foo2");
        blackhole.consume(mocks.mockeryTarget.doSomething());
        blackhole.consume(mocks.mockeryTarget.doSomething());
        blackhole.consume(mocks.mockeryTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetCallWhenChainMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.reset(mocks.mockitoTarget);
        Mockito.when(mocks.mockitoTarget.doSomething()).thenReturn("foo")
                .thenAnswer(a -> "bar")
                .thenReturn("foo2");
        blackhole.consume(mocks.mockitoTarget.doSomething());
        blackhole.consume(mocks.mockitoTarget.doSomething());
        blackhole.consume(mocks.mockitoTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetCallSingleArgWhenChainMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.reset(mocks.mockeryTarget);
        Mockery.when(mocks.mockeryTarget.doSomething("lol")).thenReturn("foo")
                .thenAnswer(a -> "bar")
                .thenReturn("foo2");
        blackhole.consume(mocks.mockeryTarget.doSomething("lol"));
        blackhole.consume(mocks.mockeryTarget.doSomething("lol"));
        blackhole.consume(mocks.mockeryTarget.doSomething("lol"));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetCallSingleArgWhenChainMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.reset(mocks.mockitoTarget);
        Mockito.when(mocks.mockitoTarget.doSomething("lol")).thenReturn("foo")
                .thenAnswer(a -> "bar")
                .thenReturn("foo2");
        blackhole.consume(mocks.mockitoTarget.doSomething("lol"));
        blackhole.consume(mocks.mockitoTarget.doSomething("lol"));
        blackhole.consume(mocks.mockitoTarget.doSomething("lol"));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetCallWhenMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.reset(mocks.mockeryTarget);
        Mockery.when(mocks.mockeryTarget.doSomething()).thenReturn("foo");
        blackhole.consume(mocks.mockeryTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetCallWhenMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.reset(mocks.mockitoTarget);
        Mockito.when(mocks.mockitoTarget.doSomething()).thenReturn("foo");
        blackhole.consume(mocks.mockitoTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetThenVerifyMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.reset(mocks.mockeryTarget);
        blackhole.consume(mocks.mockeryTarget.doSomething());
        Mockery.verify(mocks.mockeryTarget, Times.eq(1)).doSomething();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkResetThenVerifyMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.reset(mocks.mockitoTarget);
        blackhole.consume(mocks.mockitoTarget.doSomething());
        Mockito.verify(mocks.mockitoTarget, Mockito.times(1)).doSomething();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkVerifyMockery(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockeryTarget.doSomething());
        Mockery.verify(mocks.mockeryTarget, Times.ge(1)).doSomething();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkVerifyMockito(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockitoTarget.doSomething());
        Mockito.verify(mocks.mockitoTarget, Mockito.atLeastOnce()).doSomething();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallWhenMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.when(mocks.mockeryTarget.doSomething()).thenReturn("foo");
        blackhole.consume(mocks.mockeryTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallWhenMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.when(mocks.mockitoTarget.doSomething()).thenReturn("foo");
        blackhole.consume(mocks.mockitoTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallWhenPrimitiveMockery(final Mocks mocks, final Blackhole blackhole) {
        Mockery.when(mocks.mockeryTarget.doSomethingElse()).thenReturn(100);
        blackhole.consume(mocks.mockeryTarget.doSomethingElse());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallWhenPrimitiveMockito(final Mocks mocks, final Blackhole blackhole) {
        Mockito.when(mocks.mockitoTarget.doSomethingElse()).thenReturn(100);
        blackhole.consume(mocks.mockitoTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallMockedMethodWithoutHistoryMockery(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockeryDisabledTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallMockedMethodWithoutHistoryMockito(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockitoDisabledTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallMockedMethodMockery(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockeryTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallMockedMethodMockito(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockitoTarget.doSomething());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallMultipleMockedMethodMockery(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockeryTarget.doSomething());
        blackhole.consume(mocks.mockeryTarget.doSomethingElse());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCallMultipleMockedMethodMockito(final Mocks mocks, final Blackhole blackhole) {
        blackhole.consume(mocks.mockitoTarget.doSomething());
        blackhole.consume(mocks.mockitoTarget.doSomethingElse());
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCreatingMockMockery(final Blackhole blackhole) {
        blackhole.consume(Mockery.mock(Target.class));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void benchmarkCreatingMockMockito(final Blackhole blackhole) {
        blackhole.consume(Mockito.mock(Target.class));
    }

    public interface Target {
        String doSomething();
        String doSomething(final String arg1);
        int doSomethingElse();
    }

    public static class Impl implements Target {
        @Override
        public String doSomething() {
            return "foo";
        }

        @Override
        public String doSomething(final String arg1) {
            return "bar";
        }

        @Override
        public int doSomethingElse() {
            return 0;
        }
    }
}
