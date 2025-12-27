package com.bulletstream.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core ECS GameWorld using Data-Oriented Design with primitive arrays.
 * Zero-allocation update loop.
 */
public final class GameWorld {
    private static final Logger log = LoggerFactory.getLogger(GameWorld.class);

    // Entity Component Arrays
    private final float[] positionsX;
    private final float[] positionsY;
    private final float[] velocitiesX;
    private final float[] velocitiesY;
    
    private int entityCount;
    private final int maxEntities;

    public GameWorld(int maxEntities) {
        this.maxEntities = maxEntities;
        this.positionsX = new float[maxEntities];
        this.positionsY = new float[maxEntities];
        this.velocitiesX = new float[maxEntities];
        this.velocitiesY = new float[maxEntities];
        this.entityCount = 0;
        
        log.info("GameWorld initialized with capacity: {}", maxEntities);
    }

    /**
     * Add a new entity to the world.
     * @return entity index, or -1 if full
     */
    public int addEntity(float x, float y, float vx, float vy) {
        if (entityCount >= maxEntities) {
            return -1;
        }
        
        int index = entityCount++;
        positionsX[index] = x;
        positionsY[index] = y;
        velocitiesX[index] = vx;
        velocitiesY[index] = vy;
        
        return index;
    }

    /**
     * Update all entities (Zero-allocation loop).
     */
    public void update(float deltaTime) {
        for (int i = 0; i < entityCount; i++) {
            positionsX[i] += velocitiesX[i] * deltaTime;
            positionsY[i] += velocitiesY[i] * deltaTime;
        }
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
}
