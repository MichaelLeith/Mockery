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

@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 5)
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
        int doSomethingElse();
    }
}
