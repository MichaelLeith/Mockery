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

/**
 * Benchmark                                                Mode  Cnt         Score        Error  Units
 MockBenchmark.benchmarkCallMockedMethodMockery             thrpt   10   5360611.949 ±  38098.403  ops/s
 MockBenchmark.benchmarkCallMockedMethodMockito             thrpt   10     82863.989 ±   1446.033  ops/s
 MockBenchmark.benchmarkCallMultipleMockedMethodMockery     thrpt   10   2946824.360 ±  37531.862  ops/s
 MockBenchmark.benchmarkCallMultipleMockedMethodMockito     thrpt   10     41536.095 ±    637.083  ops/s
 MockBenchmark.benchmarkCallWhenMockery                     thrpt   10   2145653.095 ±   8438.961  ops/s
 MockBenchmark.benchmarkCallWhenMockito                     thrpt   10      3302.012 ±    145.471  ops/s
 MockBenchmark.benchmarkCallWhenPrimitiveMockery            thrpt   10   2876726.136 ±  28699.631  ops/s
 MockBenchmark.benchmarkCallWhenPrimitiveMockito            thrpt   10      2609.161 ±     83.768  ops/s
 MockBenchmark.benchmarkCreatingMockMockery                 thrpt   10  32250495.245 ± 232366.998  ops/s
 MockBenchmark.benchmarkCreatingMockMockito                 thrpt   10   2085453.988 ±   9575.305  ops/s
 MockBenchmark.benchmarkResetCallSingleArgWhenChainMockery  thrpt   10    740310.532 ±   3359.606  ops/s
 MockBenchmark.benchmarkResetCallSingleArgWhenChainMockito  thrpt   10     16143.108 ±     32.022  ops/s
 MockBenchmark.benchmarkResetCallWhenChainMockery           thrpt   10   1061992.726 ±  34926.096  ops/s
 MockBenchmark.benchmarkResetCallWhenChainMockito           thrpt   10     16621.828 ±    136.469  ops/s
 MockBenchmark.benchmarkResetCallWhenMockery                thrpt   10   1984342.214 ±  18845.121  ops/s
 MockBenchmark.benchmarkResetCallWhenMockito                thrpt   10     28384.565 ±    368.745  ops/s
 MockBenchmark.benchmarkResetThenVerifyMockery              thrpt   10   1789209.255 ±  15050.346  ops/s
 MockBenchmark.benchmarkResetThenVerifyMockito              thrpt   10     28643.001 ±    129.497  ops/s
 MockBenchmark.benchmarkVerifyMockery                       thrpt   10   1761442.480 ±  17790.333  ops/s
 MockBenchmark.benchmarkVerifyMockito                       thrpt   10      1580.378 ±     19.647  ops/s
 */
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Fork(1)
public class MockBenchmark {
    @State(Scope.Benchmark)
    public static class Mocks {
        Target mockeryTarget;
        Target mockitoTarget;

        @Setup(Level.Trial)
        public void setUp() {
            mockeryTarget = Mockery.mock(Target.class);
            mockitoTarget = Mockito.mock(Target.class);
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            Mockery.reset();
            Mockery.reset(mockeryTarget);
            Mockito.reset(mockitoTarget);
        }
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
}
