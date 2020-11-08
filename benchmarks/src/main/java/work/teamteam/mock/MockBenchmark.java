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
 MockBenchmark.benchmarkCallMockedMethodMockery                thrpt   10   5276808.809 ±  18245.556  ops/s
 MockBenchmark.benchmarkCallMockedMethodMockito                thrpt   10     82936.094 ±   2057.023  ops/s
 MockBenchmark.benchmarkCallMockedMethodWithoutHistoryMockery  thrpt   10   5527766.895 ±  20608.835  ops/s
 MockBenchmark.benchmarkCallMockedMethodWithoutHistoryMockito  thrpt   10     87380.608 ±    220.377  ops/s
 MockBenchmark.benchmarkCallMultipleMockedMethodMockery        thrpt   10   2824244.780 ±  28088.784  ops/s
 MockBenchmark.benchmarkCallMultipleMockedMethodMockito        thrpt   10     40799.564 ±    716.411  ops/s
 MockBenchmark.benchmarkCallWhenMockery                        thrpt   10   2116378.389 ±   9271.883  ops/s
 MockBenchmark.benchmarkCallWhenMockito                        thrpt   10      3383.156 ±    128.891  ops/s
 MockBenchmark.benchmarkCallWhenPrimitiveMockery               thrpt   10   2890055.036 ±  13057.888  ops/s
 MockBenchmark.benchmarkCallWhenPrimitiveMockito               thrpt   10      2581.798 ±     68.374  ops/s
 MockBenchmark.benchmarkCreatingMockMockery                    thrpt   10  29780347.999 ± 377141.126  ops/s
 MockBenchmark.benchmarkCreatingMockMockito                    thrpt   10   1987538.622 ±   4016.358  ops/s
 MockBenchmark.benchmarkResetCallSingleArgWhenChainMockery     thrpt   10    720096.361 ±   9254.991  ops/s
 MockBenchmark.benchmarkResetCallSingleArgWhenChainMockito     thrpt   10     15808.211 ±     53.136  ops/s
 MockBenchmark.benchmarkResetCallWhenChainMockery              thrpt   10   1096411.960 ±   2046.979  ops/s
 MockBenchmark.benchmarkResetCallWhenChainMockito              thrpt   10     16687.147 ±     77.776  ops/s
 MockBenchmark.benchmarkResetCallWhenMockery                   thrpt   10   2040258.564 ±   4022.442  ops/s
 MockBenchmark.benchmarkResetCallWhenMockito                   thrpt   10     28068.975 ±    138.278  ops/s
 MockBenchmark.benchmarkResetThenVerifyMockery                 thrpt   10   1730232.237 ±   4652.011  ops/s
 MockBenchmark.benchmarkResetThenVerifyMockito                 thrpt   10     28421.756 ±    164.791  ops/s
 MockBenchmark.benchmarkVerifyMockery                          thrpt   10   1748486.989 ±   4217.442  ops/s
 MockBenchmark.benchmarkVerifyMockito                          thrpt   10      1545.149 ±     14.545  ops/s
 */
@Warmup(iterations = 1, time = 5)
@Measurement(iterations = 1, time = 5)
@Fork(1)
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
