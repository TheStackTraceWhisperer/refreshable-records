package com.bulletstream.core.math;

import com.bulletstream.test.StrictUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VectorOps SIMD math operations.
 * Tests both correctness and boundary conditions.
 */
class VectorOpsTest extends StrictUnitTest {
    
    @Test
    void testIntegrateCorrectness() {
        // Setup test data
        float[] pos = {0.0f, 10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f, 70.0f};
        float[] vel = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f};
        float dt = 0.5f;
        int count = pos.length;
        
        // Calculate expected results using scalar loop
        float[] expected = new float[count];
        for (int i = 0; i < count; i++) {
            expected[i] = pos[i] + vel[i] * dt;
        }
        
        // Execute SIMD version
        VectorOps.integrate(pos, vel, dt, count);
        
        // Verify results
        for (int i = 0; i < count; i++) {
            assertEquals(expected[i], pos[i], EPSILON, 
                "Mismatch at index " + i);
        }
    }
    
    @Test
    void testIntegrateBoundaryNonAligned() {
        // Test with size 13 (not aligned to typical vector sizes like 4, 8, 16)
        int count = 13;
        float[] pos = new float[count];
        float[] vel = new float[count];
        float dt = 1.0f;
        
        // Initialize with known values
        for (int i = 0; i < count; i++) {
            pos[i] = i * 10.0f;
            vel[i] = i * 2.0f;
        }
        
        // Calculate expected results
        float[] expected = new float[count];
        for (int i = 0; i < count; i++) {
            expected[i] = pos[i] + vel[i] * dt;
        }
        
        // Execute SIMD version
        VectorOps.integrate(pos, vel, dt, count);
        
        // Verify all elements including tail
        for (int i = 0; i < count; i++) {
            assertEquals(expected[i], pos[i], EPSILON,
                "Tail loop failed at index " + i);
        }
    }
    
    @Test
    void testIntegrateWithZeroDelta() {
        float[] pos = {1.0f, 2.0f, 3.0f, 4.0f};
        float[] vel = {10.0f, 20.0f, 30.0f, 40.0f};
        float[] original = pos.clone();
        
        VectorOps.integrate(pos, vel, 0.0f, pos.length);
        
        // With dt=0, positions should not change
        assertArrayEquals(original, pos);
    }
    
    @Test
    void testAddCorrectness() {
        float[] a = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f};
        float[] b = {10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f, 70.0f, 80.0f};
        float[] result = new float[a.length];
        
        VectorOps.add(a, b, result, a.length);
        
        // Verify results
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i] + b[i], result[i], EPSILON,
                "Addition failed at index " + i);
        }
    }
    
    @Test
    void testAddBoundaryNonAligned() {
        // Test with size 17 (non-aligned)
        int count = 17;
        float[] a = new float[count];
        float[] b = new float[count];
        float[] result = new float[count];
        
        for (int i = 0; i < count; i++) {
            a[i] = i * 1.5f;
            b[i] = i * 2.5f;
        }
        
        VectorOps.add(a, b, result, count);
        
        // Verify all elements
        for (int i = 0; i < count; i++) {
            assertEquals(a[i] + b[i], result[i], EPSILON,
                "Non-aligned addition failed at index " + i);
        }
    }
    
    @Test
    void testMulScalarCorrectness() {
        float[] a = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f};
        float[] result = new float[a.length];
        float scalar = 2.5f;
        
        VectorOps.mulScalar(a, scalar, result, a.length);
        
        // Verify results
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i] * scalar, result[i], EPSILON,
                "Scalar multiplication failed at index " + i);
        }
    }
    
    @Test
    void testMulScalarBoundaryNonAligned() {
        // Test with size 11 (non-aligned)
        int count = 11;
        float[] a = new float[count];
        float[] result = new float[count];
        float scalar = 3.0f;
        
        for (int i = 0; i < count; i++) {
            a[i] = i * 0.5f;
        }
        
        VectorOps.mulScalar(a, scalar, result, count);
        
        // Verify all elements
        for (int i = 0; i < count; i++) {
            assertEquals(a[i] * scalar, result[i], EPSILON,
                "Non-aligned scalar multiplication failed at index " + i);
        }
    }
    
    @Test
    void testMulScalarWithZero() {
        float[] a = {1.0f, 2.0f, 3.0f, 4.0f};
        float[] result = new float[a.length];
        
        VectorOps.mulScalar(a, 0.0f, result, a.length);
        
        // All results should be zero
        for (int i = 0; i < result.length; i++) {
            assertEquals(0.0f, result[i], EPSILON);
        }
    }
    
    @Test
    void testIntegrateSmallArray() {
        // Test with single element
        float[] pos = {100.0f};
        float[] vel = {5.0f};
        
        VectorOps.integrate(pos, vel, 2.0f, 1);
        
        assertEquals(110.0f, pos[0], EPSILON);
    }
    
    @Test
    void testAddSmallArray() {
        // Test with two elements
        float[] a = {1.0f, 2.0f};
        float[] b = {3.0f, 4.0f};
        float[] result = new float[2];
        
        VectorOps.add(a, b, result, 2);
        
        assertEquals(4.0f, result[0], EPSILON);
        assertEquals(6.0f, result[1], EPSILON);
    }
    
    @Test
    void testLargeArray() {
        // Test with larger array (1000 elements)
        int count = 1000;
        float[] pos = new float[count];
        float[] vel = new float[count];
        
        for (int i = 0; i < count; i++) {
            pos[i] = i;
            vel[i] = i * 0.1f;
        }
        
        float[] expected = new float[count];
        float dt = 0.016f; // ~60 FPS
        for (int i = 0; i < count; i++) {
            expected[i] = pos[i] + vel[i] * dt;
        }
        
        VectorOps.integrate(pos, vel, dt, count);
        
        for (int i = 0; i < count; i++) {
            assertEquals(expected[i], pos[i], EPSILON,
                "Large array test failed at index " + i);
        }
    }
}
