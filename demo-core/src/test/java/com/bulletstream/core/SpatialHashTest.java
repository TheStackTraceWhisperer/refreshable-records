package com.bulletstream.core;

import com.bulletstream.test.StrictUnitTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpatialHashTest extends StrictUnitTest {

    @Test
    void testInsertAndQuery() {
        SpatialHash hash = new SpatialHash(1000.0f, 1000.0f, 100);
        
        // Insert entities at different positions
        hash.insert(0, 10.0f, 10.0f);
        hash.insert(1, 15.0f, 15.0f);
        hash.insert(2, 500.0f, 500.0f);
        
        // Query same cell as entity 0
        List<Integer> results = new ArrayList<>();
        hash.query(10.0f, 10.0f, results::add);
        
        assertTrue(results.contains(0));
        assertTrue(results.contains(1)); // Same cell (64x64)
        assertFalse(results.contains(2)); // Different cell
    }

    @Test
    void testNeighborQuery() {
        SpatialHash hash = new SpatialHash(1000.0f, 1000.0f, 100);
        
        // Cell size is 64.0f
        // Insert entities in different cells
        hash.insert(0, 32.0f, 32.0f);    // Cell (0, 0)
        hash.insert(1, 96.0f, 32.0f);    // Cell (1, 0) - Adjacent
        hash.insert(2, 32.0f, 96.0f);    // Cell (0, 1) - Adjacent
        hash.insert(3, 200.0f, 200.0f);  // Cell (3, 3) - Far away
        
        // Query neighbors from center of cell (0, 0)
        List<Integer> results = new ArrayList<>();
        hash.queryNeighbors(32.0f, 32.0f, results::add);
        
        assertTrue(results.contains(0)); // Same cell
        assertTrue(results.contains(1)); // Adjacent cell
        assertTrue(results.contains(2)); // Adjacent cell
        assertFalse(results.contains(3)); // Too far away
    }

    @Test
    void testClear() {
        SpatialHash hash = new SpatialHash(1000.0f, 1000.0f, 100);
        
        // Insert entities
        hash.insert(0, 10.0f, 10.0f);
        hash.insert(1, 15.0f, 15.0f);
        
        // Clear
        hash.clear();
        
        // Query should return nothing
        List<Integer> results = new ArrayList<>();
        hash.query(10.0f, 10.0f, results::add);
        
        assertTrue(results.isEmpty());
    }

    @Test
    void testBoundsClamping() {
        SpatialHash hash = new SpatialHash(1000.0f, 1000.0f, 100);
        
        // Insert at negative coordinates (should clamp to cell 0,0)
        hash.insert(0, -10.0f, -10.0f);
        
        // Insert at coordinates beyond world (should clamp to max cell)
        hash.insert(1, 10000.0f, 10000.0f);
        
        // Query should succeed without exceptions
        List<Integer> results = new ArrayList<>();
        hash.query(0.0f, 0.0f, results::add);
        assertTrue(results.contains(0));
    }

    @Test
    void testMultipleEntitiesInSameCell() {
        SpatialHash hash = new SpatialHash(1000.0f, 1000.0f, 100);
        
        // Insert multiple entities in the same cell
        for (int i = 0; i < 10; i++) {
            hash.insert(i, 32.0f, 32.0f);
        }
        
        // Query should return all entities
        List<Integer> results = new ArrayList<>();
        hash.query(32.0f, 32.0f, results::add);
        
        assertEquals(10, results.size());
    }

    @Test
    void testGridDimensions() {
        SpatialHash hash = new SpatialHash(1000.0f, 1000.0f, 100);
        
        // Cell size is 64.0f
        // Expected grid: ceil(1000/64) = 16 x 16
        assertEquals(16, hash.getGridWidth());
        assertEquals(16, hash.getGridHeight());
        assertEquals(64.0f, hash.getCellSize(), 0.001f);
    }
}
