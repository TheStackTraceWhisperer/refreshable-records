package com.bulletstream.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spatial Hash Grid for O(1) collision detection.
 * Uses embedded linked-list in arrays (Data-Oriented Design).
 */
public final class SpatialHash {
    private static final Logger log = LoggerFactory.getLogger(SpatialHash.class);

    private static final float CELL_SIZE = 64.0f;
    
    private final int gridWidth;
    private final int gridHeight;
    private final int[] cellHead;     // Head of linked list for each cell (entity index or -1)
    private final int[] nextEntity;   // Next entity in same cell (or -1 for end of list)
    private final int maxEntities;

    /**
     * Create a spatial hash grid.
     * @param worldWidth width of the world in game units
     * @param worldHeight height of the world in game units
     * @param maxEntities maximum number of entities
     */
    public SpatialHash(float worldWidth, float worldHeight, int maxEntities) {
        this.gridWidth = (int) Math.ceil(worldWidth / CELL_SIZE);
        this.gridHeight = (int) Math.ceil(worldHeight / CELL_SIZE);
        this.cellHead = new int[gridWidth * gridHeight];
        this.nextEntity = new int[maxEntities];
        this.maxEntities = maxEntities;
        
        clear();
        
        log.info("SpatialHash initialized: {}x{} cells ({} total), {} entities max", 
                 gridWidth, gridHeight, cellHead.length, maxEntities);
    }

    /**
     * Clear all cells (Zero-allocation).
     */
    public void clear() {
        // Initialize all cells to empty (-1)
        for (int i = 0; i < cellHead.length; i++) {
            cellHead[i] = -1;
        }
        // Initialize all next pointers to -1
        for (int i = 0; i < nextEntity.length; i++) {
            nextEntity[i] = -1;
        }
    }

    /**
     * Insert an entity into the spatial hash.
     * @param entityIndex the entity index
     * @param x entity X position
     * @param y entity Y position
     */
    public void insert(int entityIndex, float x, float y) {
        if (entityIndex < 0 || entityIndex >= maxEntities) {
            return; // Silently ignore invalid indices
        }
        
        int cellX = (int) (x / CELL_SIZE);
        int cellY = (int) (y / CELL_SIZE);
        
        // Clamp to grid bounds
        if (cellX < 0) cellX = 0;
        if (cellX >= gridWidth) cellX = gridWidth - 1;
        if (cellY < 0) cellY = 0;
        if (cellY >= gridHeight) cellY = gridHeight - 1;
        
        int cellIndex = cellY * gridWidth + cellX;
        
        // Insert at head of linked list
        nextEntity[entityIndex] = cellHead[cellIndex];
        cellHead[cellIndex] = entityIndex;
    }

    /**
     * Query entities in a cell.
     * @param x world X position
     * @param y world Y position
     * @param callback callback for each entity in the cell
     */
    public void query(float x, float y, QueryCallback callback) {
        int cellX = (int) (x / CELL_SIZE);
        int cellY = (int) (y / CELL_SIZE);
        
        // Check bounds
        if (cellX < 0 || cellX >= gridWidth || cellY < 0 || cellY >= gridHeight) {
            return;
        }
        
        int cellIndex = cellY * gridWidth + cellX;
        int entityIndex = cellHead[cellIndex];
        
        // Traverse linked list
        while (entityIndex != -1) {
            callback.onEntity(entityIndex);
            entityIndex = nextEntity[entityIndex];
        }
    }

    /**
     * Query entities in a cell and its 8 neighbors (3x3 grid).
     * @param x world X position
     * @param y world Y position
     * @param callback callback for each entity found
     */
    public void queryNeighbors(float x, float y, QueryCallback callback) {
        int centerCellX = (int) (x / CELL_SIZE);
        int centerCellY = (int) (y / CELL_SIZE);
        
        // Check 3x3 grid centered on the cell
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cellX = centerCellX + dx;
                int cellY = centerCellY + dy;
                
                // Skip out-of-bounds cells
                if (cellX < 0 || cellX >= gridWidth || cellY < 0 || cellY >= gridHeight) {
                    continue;
                }
                
                int cellIndex = cellY * gridWidth + cellX;
                int entityIndex = cellHead[cellIndex];
                
                // Traverse linked list
                while (entityIndex != -1) {
                    callback.onEntity(entityIndex);
                    entityIndex = nextEntity[entityIndex];
                }
            }
        }
    }

    /**
     * Callback interface for spatial queries.
     */
    @FunctionalInterface
    public interface QueryCallback {
        void onEntity(int entityIndex);
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public float getCellSize() {
        return CELL_SIZE;
    }
}
