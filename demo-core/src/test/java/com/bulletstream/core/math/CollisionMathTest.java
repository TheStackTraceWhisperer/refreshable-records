package com.bulletstream.core.math;

import com.bulletstream.test.StrictUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CollisionMath branchless collision detection.
 */
class CollisionMathTest extends StrictUnitTest {
    
    @Test
    void testIsPointInCircleInside() {
        // Point at origin, circle at origin with radius 10
        assertTrue(CollisionMath.isPointInCircle(0.0f, 0.0f, 0.0f, 0.0f, 100.0f));
        
        // Point just inside circle
        assertTrue(CollisionMath.isPointInCircle(5.0f, 0.0f, 0.0f, 0.0f, 100.0f));
        
        // Point at exact radius (edge case)
        assertTrue(CollisionMath.isPointInCircle(10.0f, 0.0f, 0.0f, 0.0f, 100.0f));
    }
    
    @Test
    void testIsPointInCircleOutside() {
        // Point far outside circle
        assertFalse(CollisionMath.isPointInCircle(20.0f, 0.0f, 0.0f, 0.0f, 100.0f));
        
        // Point just outside circle
        assertFalse(CollisionMath.isPointInCircle(10.1f, 0.0f, 0.0f, 0.0f, 100.0f));
    }
    
    @Test
    void testIsPointInCircleWithOffset() {
        // Circle centered at (100, 100) with radius 5
        assertTrue(CollisionMath.isPointInCircle(102.0f, 102.0f, 100.0f, 100.0f, 25.0f));
        assertFalse(CollisionMath.isPointInCircle(110.0f, 110.0f, 100.0f, 100.0f, 25.0f));
    }
    
    @Test
    void testCountCollisionsAllHit() {
        int count = 8;
        float[] px = {0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f};
        float[] py = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] cx = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] cy = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] rSq = {100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f};
        
        int collisions = CollisionMath.countCollisions(px, py, cx, cy, rSq, count);
        
        assertEquals(8, collisions, "All points should be inside circles");
    }
    
    @Test
    void testCountCollisionsNoneHit() {
        int count = 8;
        float[] px = {100.0f, 101.0f, 102.0f, 103.0f, 104.0f, 105.0f, 106.0f, 107.0f};
        float[] py = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] cx = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] cy = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] rSq = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        
        int collisions = CollisionMath.countCollisions(px, py, cx, cy, rSq, count);
        
        assertEquals(0, collisions, "No points should be inside circles");
    }
    
    @Test
    void testCountCollisionsMixed() {
        int count = 8;
        float[] px = {0.0f, 15.0f, 2.0f, 30.0f, 4.0f, 50.0f, 6.0f, 70.0f};
        float[] py = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] cx = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] cy = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        float[] rSq = {100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f, 100.0f};
        
        int collisions = CollisionMath.countCollisions(px, py, cx, cy, rSq, count);
        
        // Points at 0, 2, 4, 6 are inside (distance <= 10)
        // Points at 15, 30, 50, 70 are outside
        assertEquals(4, collisions, "Expected 4 collisions");
    }
    
    @Test
    void testCountCollisionsBoundaryNonAligned() {
        // Test with size 13 (non-aligned to typical vector sizes)
        int count = 13;
        float[] px = new float[count];
        float[] py = new float[count];
        float[] cx = new float[count];
        float[] cy = new float[count];
        float[] rSq = new float[count];
        
        // Set up data: alternating hits and misses
        for (int i = 0; i < count; i++) {
            px[i] = i % 2 == 0 ? 5.0f : 15.0f; // Even indices inside, odd outside
            py[i] = 0.0f;
            cx[i] = 0.0f;
            cy[i] = 0.0f;
            rSq[i] = 100.0f; // radius = 10
        }
        
        int collisions = CollisionMath.countCollisions(px, py, cx, cy, rSq, count);
        
        // 7 even indices (0,2,4,6,8,10,12) should hit
        assertEquals(7, collisions, "Expected 7 collisions in non-aligned array");
    }
    
    @Test
    void testCountCollisionsSingleElement() {
        float[] px = {5.0f};
        float[] py = {0.0f};
        float[] cx = {0.0f};
        float[] cy = {0.0f};
        float[] rSq = {100.0f};
        
        int collisions = CollisionMath.countCollisions(px, py, cx, cy, rSq, 1);
        
        assertEquals(1, collisions, "Single element should collide");
    }
    
    @Test
    void testCountCollisionsLargeArray() {
        // Test with 1000 elements
        int count = 1000;
        float[] px = new float[count];
        float[] py = new float[count];
        float[] cx = new float[count];
        float[] cy = new float[count];
        float[] rSq = new float[count];
        
        int expectedCollisions = 0;
        for (int i = 0; i < count; i++) {
            px[i] = i * 0.5f;
            py[i] = 0.0f;
            cx[i] = 0.0f;
            cy[i] = 0.0f;
            rSq[i] = 10000.0f; // Large radius
            
            // Count expected collisions manually
            float dx = px[i] - cx[i];
            float dy = py[i] - cy[i];
            if (dx * dx + dy * dy <= rSq[i]) {
                expectedCollisions++;
            }
        }
        
        int collisions = CollisionMath.countCollisions(px, py, cx, cy, rSq, count);
        
        assertEquals(expectedCollisions, collisions, "Large array collision count mismatch");
    }
}
