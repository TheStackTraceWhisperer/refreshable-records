package com.bulletstream.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {

    @Test
    void testAddEntity() {
        GameWorld world = new GameWorld(10);
        
        int entityId = world.addEntity(100.0f, 200.0f, 5.0f, 10.0f);
        
        assertEquals(0, entityId);
        assertEquals(1, world.getEntityCount());
        assertEquals(100.0f, world.getPositionX(0), 0.001f);
        assertEquals(200.0f, world.getPositionY(0), 0.001f);
    }

    @Test
    void testUpdate() {
        GameWorld world = new GameWorld(10);
        world.addEntity(0.0f, 0.0f, 10.0f, 20.0f);
        
        world.update(1.0f);
        
        assertEquals(10.0f, world.getPositionX(0), 0.001f);
        assertEquals(20.0f, world.getPositionY(0), 0.001f);
    }

    @Test
    void testCapacityLimit() {
        GameWorld world = new GameWorld(2);
        
        assertEquals(0, world.addEntity(0, 0, 0, 0));
        assertEquals(1, world.addEntity(0, 0, 0, 0));
        assertEquals(-1, world.addEntity(0, 0, 0, 0)); // Should fail
    }
}
