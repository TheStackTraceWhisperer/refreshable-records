package com.bulletstream.core.math;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

/**
 * Branchless collision detection using SIMD operations.
 * Optimized for high-throughput collision checks.
 */
public final class CollisionMath {
    
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    
    // Private constructor to prevent instantiation
    private CollisionMath() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Check if a point is inside a circle using branchless logic.
     * 
     * @param px point x coordinate
     * @param py point y coordinate
     * @param cx circle center x coordinate
     * @param cy circle center y coordinate
     * @param rSq circle radius squared
     * @return true if point is inside the circle
     */
    public static boolean isPointInCircle(float px, float py, float cx, float cy, float rSq) {
        float dx = px - cx;
        float dy = py - cy;
        float distSq = dx * dx + dy * dy;
        return distSq <= rSq;
    }
    
    /**
     * Count collisions between points and circles using vectorized operations.
     * Uses VectorMask to count hits in parallel without branching.
     * 
     * @param px point x coordinates array
     * @param py point y coordinates array
     * @param cx circle center x coordinates array
     * @param cy circle center y coordinates array
     * @param rSq circle radius squared array
     * @param count number of elements to process
     * @return number of collisions detected
     */
    public static int countCollisions(float[] px, float[] py, float[] cx, float[] cy, float[] rSq, int count) {
        int vectorSize = SPECIES.length();
        int collisionCount = 0;
        int i = 0;
        
        // Main loop: Process full vectors
        int upperBound = SPECIES.loopBound(count);
        for (; i < upperBound; i += vectorSize) {
            FloatVector vPx = FloatVector.fromArray(SPECIES, px, i);
            FloatVector vPy = FloatVector.fromArray(SPECIES, py, i);
            FloatVector vCx = FloatVector.fromArray(SPECIES, cx, i);
            FloatVector vCy = FloatVector.fromArray(SPECIES, cy, i);
            FloatVector vRSq = FloatVector.fromArray(SPECIES, rSq, i);
            
            // Calculate distance squared
            FloatVector vDx = vPx.sub(vCx);
            FloatVector vDy = vPy.sub(vCy);
            FloatVector vDistSq = vDx.mul(vDx).add(vDy.mul(vDy));
            
            // Create mask for collisions (distSq <= rSq)
            VectorMask<Float> collisionMask = vDistSq.compare(jdk.incubator.vector.VectorOperators.LE, vRSq);
            
            // Count true lanes in the mask
            collisionCount += collisionMask.trueCount();
        }
        
        // Tail loop: Process remaining elements
        for (; i < count; i++) {
            if (isPointInCircle(px[i], py[i], cx[i], cy[i], rSq[i])) {
                collisionCount++;
            }
        }
        
        return collisionCount;
    }
}
