package com.bulletstream.core;

import com.bulletstream.core.util.IntStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core ECS GameWorld using Data-Oriented Design with primitive arrays.
 * Zero-allocation update loop with entity lifecycle management.
 */
public final class GameWorld {
    private static final Logger log = LoggerFactory.getLogger(GameWorld.class);

    // Entity Flags
    public static final byte FLAG_ACTIVE = 1;
    public static final byte FLAG_PLAYER = 2;
    public static final byte FLAG_BULLET = 4;
    public static final byte FLAG_ENEMY = 8;

    // Entity Component Arrays (Structure of Arrays)
    private final int[] entityIds;      // Upper 16 bits = Generation, Lower 16 bits = Index
    private final byte[] flags;         // Bitmask: ACTIVE(1), PLAYER(2), BULLET(4), ENEMY(8)
    private final float[] positionsX;
    private final float[] positionsY;
    private final float[] velocitiesX;
    private final float[] velocitiesY;
    private final float[] radius;       // Collision radius (squared)
    private final int[] ownerId;        // Entity ID of who fired this bullet
    private final byte[] inputMask;     // Current tick input state (if player)
    
    // Entity Lifecycle Management
    private final IntStack freeIndices; // Recycling stack for destroyed entities
    private int entityCount;
    private final int maxEntities;

    public GameWorld(int maxEntities) {
        this.maxEntities = maxEntities;
        this.entityIds = new int[maxEntities];
        this.flags = new byte[maxEntities];
        this.positionsX = new float[maxEntities];
        this.positionsY = new float[maxEntities];
        this.velocitiesX = new float[maxEntities];
        this.velocitiesY = new float[maxEntities];
        this.radius = new float[maxEntities];
        this.ownerId = new int[maxEntities];
        this.inputMask = new byte[maxEntities];
        this.freeIndices = new IntStack(maxEntities);
        this.entityCount = 0;
        
        log.info("GameWorld initialized with capacity: {}", maxEntities);
    }

    /**
     * Spawn a new entity with recycling support.
     * @return entity ID (generation in upper 16 bits, index in lower 16 bits), or -1 if full
     */
    public int spawnEntity(float x, float y, float vx, float vy, byte entityFlags) {
        // Use free list or allocate new index
        int index;
        if (!freeIndices.isEmpty()) {
            index = freeIndices.pop();
            // Increment generation to invalidate stale references
            int generation = (entityIds[index] >>> 16) + 1;
            if (generation >= 0xFFFF) {
                generation = 1; // Wrap around (skip 0)
            }
            entityIds[index] = (generation << 16) | index;
        } else {
            if (entityCount >= maxEntities) {
                return -1;
            }
            index = entityCount++;
            // Initialize generation to 1 (0 is reserved for null/invalid)
            entityIds[index] = (1 << 16) | index;
        }
        
        // Reset all component arrays
        flags[index] = (byte) (entityFlags | FLAG_ACTIVE);
        positionsX[index] = x;
        positionsY[index] = y;
        velocitiesX[index] = vx;
        velocitiesY[index] = vy;
        radius[index] = 0.0f;
        ownerId[index] = 0;
        inputMask[index] = 0;
        
        return entityIds[index];
    }

    /**
     * Despawn an entity and recycle its index.
     * @param entityId the entity ID to despawn
     * @throws IllegalArgumentException if entity ID is invalid or stale
     */
    public void despawnEntity(int entityId) {
        int index = entityId & 0xFFFF;
        
        // Validate index range (against maxEntities to account for recycled indices)
        if (index < 0 || index >= maxEntities) {
            throw new IllegalArgumentException("Invalid entity index: " + index);
        }
        
        // Validate generation (prevent stale access)
        if (entityIds[index] != entityId) {
            throw new IllegalArgumentException("Stale entity ID: " + entityId + " (current: " + entityIds[index] + ")");
        }
        
        // Clear ACTIVE flag
        flags[index] = 0;
        
        // Add to free list for recycling
        freeIndices.push(index);
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use {@link #spawnEntity(float, float, float, float, byte)} instead
     */
    @Deprecated
    public int addEntity(float x, float y, float vx, float vy) {
        int entityId = spawnEntity(x, y, vx, vy, (byte) 0);
        return entityId == -1 ? -1 : (entityId & 0xFFFF); // Return index for compatibility
    }

    /**
     * Update all active entities (Zero-allocation loop).
     */
    public void update(float deltaTime) {
        for (int i = 0; i < entityCount; i++) {
            // Only update active entities
            if ((flags[i] & FLAG_ACTIVE) != 0) {
                positionsX[i] += velocitiesX[i] * deltaTime;
                positionsY[i] += velocitiesY[i] * deltaTime;
            }
        }
    }

    /**
     * Check if an entity is active.
     * @param entityId the entity ID to check
     * @return true if active, false otherwise
     */
    public boolean isActive(int entityId) {
        int index = entityId & 0xFFFF;
        if (index < 0 || index >= entityCount) {
            return false;
        }
        if (entityIds[index] != entityId) {
            return false; // Stale reference
        }
        return (flags[index] & FLAG_ACTIVE) != 0;
    }

    public int getEntityCount() {
        return entityCount;
    }

    public float getPositionX(int index) {
        return positionsX[index];
    }

    public float getPositionY(int index) {
        return positionsY[index];
    }

    public byte getFlags(int index) {
        return flags[index];
    }

    public int getEntityId(int index) {
        return entityIds[index];
    }
}
