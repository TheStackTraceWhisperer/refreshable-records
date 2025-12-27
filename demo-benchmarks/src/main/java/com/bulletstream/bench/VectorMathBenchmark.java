package com.bulletstream.bench;

import com.bulletstream.core.math.VectorOps;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark comparing scalar vs SIMD vector operations.
 * Measures performance improvement from Vector API.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xmx4G", "-Xms4G", "--add-modules=jdk.incubator.vector"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class VectorMathBenchmark {
    
    private static final int ARRAY_SIZE = 100_000;
    
    private float[] positionsA;
    private float[] velocitiesA;
    private float[] positionsB;
    private float[] velocitiesB;
    private float[] arrayA;
    private float[] arrayB;
    private float[] result;
    
    @Setup(Level.Trial)
    public void setup() {
        positionsA = new float[ARRAY_SIZE];
        velocitiesA = new float[ARRAY_SIZE];
        positionsB = new float[ARRAY_SIZE];
        velocitiesB = new float[ARRAY_SIZE];
        arrayA = new float[ARRAY_SIZE];
        arrayB = new float[ARRAY_SIZE];
        result = new float[ARRAY_SIZE];
        
        // Initialize with test data
        for (int i = 0; i < ARRAY_SIZE; i++) {
            positionsA[i] = i * 0.1f;
            velocitiesA[i] = i * 0.01f;
            positionsB[i] = i * 0.1f;
            velocitiesB[i] = i * 0.01f;
            arrayA[i] = i * 0.5f;
            arrayB[i] = i * 0.25f;
        }
    }
    
    // ==================== INTEGRATE BENCHMARKS ====================
    
    @Benchmark
    public void integrateScalar(Blackhole bh) {
        float dt = 0.016f; // 60 FPS delta time
        
        // Scalar implementation
        for (int i = 0; i < ARRAY_SIZE; i++) {
            positionsA[i] += velocitiesA[i] * dt;
        }
        
        bh.consume(positionsA);
    }
    
    @Benchmark
    public void integrateSIMD(Blackhole bh) {
        float dt = 0.016f; // 60 FPS delta time
        
        // SIMD implementation
        VectorOps.integrate(positionsB, velocitiesB, dt, ARRAY_SIZE);
        
        bh.consume(positionsB);
    }
    
    // ==================== ADD BENCHMARKS ====================
    
    @Benchmark
    public void addScalar(Blackhole bh) {
        // Scalar implementation
        for (int i = 0; i < ARRAY_SIZE; i++) {
            result[i] = arrayA[i] + arrayB[i];
        }
        
        bh.consume(result);
    }
    
    @Benchmark
    public void addSIMD(Blackhole bh) {
        // SIMD implementation
        VectorOps.add(arrayA, arrayB, result, ARRAY_SIZE);
        
        bh.consume(result);
    }
    
    // ==================== MULTIPLY SCALAR BENCHMARKS ====================
    
    @Benchmark
    public void mulScalarScalar(Blackhole bh) {
        float scalar = 2.5f;
        
        // Scalar implementation
        for (int i = 0; i < ARRAY_SIZE; i++) {
            result[i] = arrayA[i] * scalar;
        }
        
        bh.consume(result);
    }
    
    @Benchmark
    public void mulScalarSIMD(Blackhole bh) {
        float scalar = 2.5f;
        
        // SIMD implementation
        VectorOps.mulScalar(arrayA, scalar, result, ARRAY_SIZE);
        
        bh.consume(result);
    }
}
