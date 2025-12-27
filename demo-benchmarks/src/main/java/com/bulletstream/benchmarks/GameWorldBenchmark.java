package com.bulletstream.benchmarks;

import com.bulletstream.core.GameWorld;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for GameWorld update performance.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class GameWorldBenchmark {

    private GameWorld world;

    @Setup
    public void setup() {
        world = new GameWorld(1000);
        // Populate with entities
        for (int i = 0; i < 1000; i++) {
            world.addEntity(0, 0, 1, 1);
        }
    }

    @Benchmark
    public void updateWorld() {
        world.update(0.016f); // 60 FPS
    }
}
