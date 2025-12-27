package com.bulletstream.core;

import com.bulletstream.test.StrictUnitTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest extends StrictUnitTest {

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

    @Test
    void testGenerationIncrementOnRecycle() {
        GameWorld world = new GameWorld(10);
        
        // Spawn entity (generation 1, index 0)
        int firstEntityId = world.spawnEntity(10.0f, 10.0f, 0.0f, 0.0f, (byte) 0);
        int firstIndex = firstEntityId & 0xFFFF;
        int firstGeneration = firstEntityId >>> 16;
        
        assertEquals(0, firstIndex);
        assertEquals(1, firstGeneration);
        assertTrue(world.isActive(firstEntityId));
        
        // Despawn entity
        world.despawnEntity(firstEntityId);
        assertFalse(world.isActive(firstEntityId));
        
        // Spawn new entity (should reuse index 0 but increment generation to 2)
        int secondEntityId = world.spawnEntity(20.0f, 20.0f, 0.0f, 0.0f, (byte) 0);
        int secondIndex = secondEntityId & 0xFFFF;
        int secondGeneration = secondEntityId >>> 16;
        
        assertEquals(0, secondIndex); // Same index
        assertEquals(2, secondGeneration); // Incremented generation
        assertNotEquals(firstEntityId, secondEntityId); // Different IDs
        assertTrue(world.isActive(secondEntityId));
    }

    @Test
    void testStaleAccessThrowsException() {
        GameWorld world = new GameWorld(10);
        
        // Spawn and despawn entity
        int entityId = world.spawnEntity(10.0f, 10.0f, 0.0f, 0.0f, (byte) 0);
        world.despawnEntity(entityId);
        
        // Spawn new entity at same index (different generation)
        world.spawnEntity(20.0f, 20.0f, 0.0f, 0.0f, (byte) 0);
        
        // Try to despawn old entity ID (should throw)
        assertThrows(IllegalArgumentException.class, () -> {
            world.despawnEntity(entityId);
        });
    }

    @Test
    void testEntityFlags() {
        GameWorld world = new GameWorld(10);
        
        // Spawn player entity
        int playerId = world.spawnEntity(0.0f, 0.0f, 0.0f, 0.0f, GameWorld.FLAG_PLAYER);
        int playerIndex = playerId & 0xFFFF;
        
        byte playerFlags = world.getFlags(playerIndex);
        assertTrue((playerFlags & GameWorld.FLAG_ACTIVE) != 0);
        assertTrue((playerFlags & GameWorld.FLAG_PLAYER) != 0);
        assertFalse((playerFlags & GameWorld.FLAG_BULLET) != 0);
        
        // Spawn bullet entity
        int bulletId = world.spawnEntity(10.0f, 10.0f, 5.0f, 0.0f, GameWorld.FLAG_BULLET);
        int bulletIndex = bulletId & 0xFFFF;
        
        byte bulletFlags = world.getFlags(bulletIndex);
        assertTrue((bulletFlags & GameWorld.FLAG_ACTIVE) != 0);
        assertFalse((bulletFlags & GameWorld.FLAG_PLAYER) != 0);
        assertTrue((bulletFlags & GameWorld.FLAG_BULLET) != 0);
    }

    @Test
    void testUpdateOnlyActiveEntities() {
        GameWorld world = new GameWorld(10);
        
        // Spawn two entities
        int entity1 = world.spawnEntity(0.0f, 0.0f, 10.0f, 0.0f, (byte) 0);
        int entity2 = world.spawnEntity(0.0f, 0.0f, 10.0f, 0.0f, (byte) 0);
        
        int index1 = entity1 & 0xFFFF;
        int index2 = entity2 & 0xFFFF;
        
        // Despawn first entity
        world.despawnEntity(entity1);
        
        // Update
        world.update(1.0f);
        
        // First entity should not have moved (inactive)
        assertEquals(0.0f, world.getPositionX(index1), 0.001f);
        
        // Second entity should have moved
        assertEquals(10.0f, world.getPositionX(index2), 0.001f);
    }
}
