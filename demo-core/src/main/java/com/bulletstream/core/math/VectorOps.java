package com.bulletstream.core.math;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

/**
 * SIMD utility class for processing primitive arrays in batches.
 * Uses Java Vector API for high-performance parallel operations.
 * Zero-allocation design for hot path execution.
 */
public final class VectorOps {
    
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    
    // Private constructor to prevent instantiation
    private VectorOps() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Integrate positions with velocities using Euler integration: pos += vel * dt.
     * Uses SIMD operations for the main loop and scalar operations for the tail.
     * 
     * @param pos position array to update in-place
     * @param vel velocity array
     * @param dt delta time scalar
     * @param count number of elements to process
     */
    public static void integrate(float[] pos, float[] vel, float dt, int count) {
        int vectorSize = SPECIES.length();
        int i = 0;
        
        // Main loop: Process full vectors
        int upperBound = SPECIES.loopBound(count);
        for (; i < upperBound; i += vectorSize) {
            FloatVector vPos = FloatVector.fromArray(SPECIES, pos, i);
            FloatVector vVel = FloatVector.fromArray(SPECIES, vel, i);
            FloatVector vResult = vPos.add(vVel.mul(dt));
            vResult.intoArray(pos, i);
        }
        
        // Tail loop: Process remaining elements
        for (; i < count; i++) {
            pos[i] += vel[i] * dt;
        }
    }
    
    /**
     * Add two arrays element-wise: result[i] = a[i] + b[i].
     * 
     * @param a first input array
     * @param b second input array
     * @param result output array
     * @param count number of elements to process
     */
    public static void add(float[] a, float[] b, float[] result, int count) {
        int vectorSize = SPECIES.length();
        int i = 0;
        
        // Main loop: Process full vectors
        int upperBound = SPECIES.loopBound(count);
        for (; i < upperBound; i += vectorSize) {
            FloatVector vA = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vB = FloatVector.fromArray(SPECIES, b, i);
            FloatVector vResult = vA.add(vB);
            vResult.intoArray(result, i);
        }
        
        // Tail loop: Process remaining elements
        for (; i < count; i++) {
            result[i] = a[i] + b[i];
        }
    }
    
    /**
     * Multiply array by scalar: result[i] = a[i] * scalar.
     * 
     * @param a input array
     * @param scalar scalar value to multiply
     * @param result output array
     * @param count number of elements to process
     */
    public static void mulScalar(float[] a, float scalar, float[] result, int count) {
        int vectorSize = SPECIES.length();
        int i = 0;
        
        // Main loop: Process full vectors
        int upperBound = SPECIES.loopBound(count);
        for (; i < upperBound; i += vectorSize) {
            FloatVector vA = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vResult = vA.mul(scalar);
            vResult.intoArray(result, i);
        }
        
        // Tail loop: Process remaining elements
        for (; i < count; i++) {
            result[i] = a[i] * scalar;
        }
    }
}
