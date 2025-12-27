package com.bulletstream.core.net;

import com.bulletstream.core.net.protocol.*;
import com.bulletstream.test.StrictUnitTest;
import io.fury.Fury;
import io.fury.config.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests network protocol classes for Fury serialization compatibility and efficiency.
 */
class ProtocolTest extends StrictUnitTest {

    private Fury fury;

    @BeforeEach
    void setUp() {
        // Initialize Fury with thread-safe configuration
        fury = Fury.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .build();
        
        // Register protocol classes for optimal performance
        fury.register(LanePacket.class);
        fury.register(InputPayload.class);
        fury.register(StatePayload.class);
        fury.register(AdminCommand.class);
    }

    @Test
    void testLanePacketFuryCompatibility() {
        // Create test packet
        InputPayload payload = new InputPayload(100L, (byte) 0x1F, 1.5708f);
        LanePacket original = new LanePacket((byte) 1, 42L, payload);
        
        // Serialize
        byte[] bytes = fury.serialize(original);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        
        // Deserialize
        LanePacket deserialized = (LanePacket) fury.deserialize(bytes);
        
        // Verify equality
        assertEquals(original.getLaneId(), deserialized.getLaneId());
        assertEquals(original.getSequence(), deserialized.getSequence());
        
        // Verify nested payload
        assertNotNull(deserialized.getPayload());
        assertTrue(deserialized.getPayload() instanceof InputPayload);
        InputPayload deserializedPayload = (InputPayload) deserialized.getPayload();
        assertEquals(payload.getTick(), deserializedPayload.getTick());
        assertEquals(payload.getInputMask(), deserializedPayload.getInputMask());
        assertEquals(payload.getAngle(), deserializedPayload.getAngle(), EPSILON);
    }

    @Test
    void testInputPayloadFuryCompatibility() {
        // Create test input
        InputPayload original = new InputPayload(12345L, (byte) 0x15, 3.14159f);
        
        // Serialize
        byte[] bytes = fury.serialize(original);
        assertNotNull(bytes);
        
        // Deserialize
        InputPayload deserialized = (InputPayload) fury.deserialize(bytes);
        
        // Verify equality
        assertEquals(original, deserialized);
        assertEquals(original.getTick(), deserialized.getTick());
        assertEquals(original.getInputMask(), deserialized.getInputMask());
        assertEquals(original.getAngle(), deserialized.getAngle(), EPSILON);
    }

    @Test
    void testStatePayloadFuryCompatibility() {
        // Create test state with entity data
        float[] packedData = new float[]{
            1.0f, 100.0f, 200.0f, 0.0f,  // Entity 1
            2.0f, 150.0f, 250.0f, 1.0f   // Entity 2
        };
        StatePayload original = new StatePayload(9999L, 2, packedData);
        
        // Serialize
        byte[] bytes = fury.serialize(original);
        assertNotNull(bytes);
        
        // Deserialize
        StatePayload deserialized = (StatePayload) fury.deserialize(bytes);
        
        // Verify equality
        assertEquals(original, deserialized);
        assertEquals(original.getServerTick(), deserialized.getServerTick());
        assertEquals(original.getEntityCount(), deserialized.getEntityCount());
        assertArrayEquals(original.getPackedData(), deserialized.getPackedData());
    }

    @Test
    void testAdminCommandFuryCompatibility() {
        // Create test command
        AdminCommand original = new AdminCommand((byte) 1, 60.0f);
        
        // Serialize
        byte[] bytes = fury.serialize(original);
        assertNotNull(bytes);
        
        // Deserialize
        AdminCommand deserialized = (AdminCommand) fury.deserialize(bytes);
        
        // Verify equality
        assertEquals(original, deserialized);
        assertEquals(original.getCommandType(), deserialized.getCommandType());
        assertEquals(original.getValue(), deserialized.getValue(), EPSILON);
    }

    @Test
    void testStatePayloadSizeEfficiency() {
        // Create StatePayload with 0 entities (minimal state)
        StatePayload emptyState = new StatePayload(1L, 0, new float[0]);
        
        // Serialize
        byte[] bytes = fury.serialize(emptyState);
        
        // Verify size is under 50 bytes (efficiency check)
        assertTrue(bytes.length < 50, 
            "StatePayload with 0 entities should be < 50 bytes, but was " + bytes.length + " bytes");
    }

    @Test
    void testInputMaskBitOperations() {
        // Test bitmask combinations
        byte up = 0x01;
        byte down = 0x02;
        byte left = 0x04;
        byte right = 0x08;
        byte shoot = 0x10;
        
        // Test combined input (up + right + shoot)
        byte combined = (byte) (up | right | shoot);
        InputPayload input = new InputPayload(1L, combined, 0.0f);
        
        // Verify bits are set correctly
        assertEquals(0x19, input.getInputMask()); // 0x01 | 0x08 | 0x10 = 0x19
        
        // Test serialization preserves bitmask
        byte[] bytes = fury.serialize(input);
        InputPayload deserialized = (InputPayload) fury.deserialize(bytes);
        assertEquals(combined, deserialized.getInputMask());
    }

    @Test
    void testStatePayloadPackedDataFormat() {
        // Test packed data format: [ID, X, Y, TYPE, ID, X, Y, TYPE]
        float[] packedData = new float[]{
            123.0f, 500.0f, 300.0f, 2.0f,  // Entity ID=123, pos=(500,300), type=2
            456.0f, 100.0f, 50.0f, 1.0f    // Entity ID=456, pos=(100,50), type=1
        };
        
        StatePayload state = new StatePayload(100L, 2, packedData);
        
        // Serialize and deserialize
        byte[] bytes = fury.serialize(state);
        StatePayload deserialized = (StatePayload) fury.deserialize(bytes);
        
        // Verify packed data structure is preserved
        float[] deserializedData = deserialized.getPackedData();
        assertEquals(8, deserializedData.length);
        assertEquals(123.0f, deserializedData[0], EPSILON);  // First entity ID
        assertEquals(500.0f, deserializedData[1], EPSILON);  // First entity X
        assertEquals(300.0f, deserializedData[2], EPSILON);  // First entity Y
        assertEquals(2.0f, deserializedData[3], EPSILON);    // First entity TYPE
        assertEquals(456.0f, deserializedData[4], EPSILON);  // Second entity ID
    }

    @Test
    void testAdminCommandTypes() {
        // Test SET_TICK_RATE command
        AdminCommand setTickRate = new AdminCommand((byte) 1, 120.0f);
        byte[] bytes1 = fury.serialize(setTickRate);
        AdminCommand deserialized1 = (AdminCommand) fury.deserialize(bytes1);
        assertEquals((byte) 1, deserialized1.getCommandType());
        assertEquals(120.0f, deserialized1.getValue(), EPSILON);
        
        // Test FORCE_RESYNC command
        AdminCommand forceResync = new AdminCommand((byte) 2, 0.0f);
        byte[] bytes2 = fury.serialize(forceResync);
        AdminCommand deserialized2 = (AdminCommand) fury.deserialize(bytes2);
        assertEquals((byte) 2, deserialized2.getCommandType());
        assertEquals(0.0f, deserialized2.getValue(), EPSILON);
    }

    @Test
    void testLanePacketWithDifferentPayloadTypes() {
        // Test with InputPayload
        InputPayload input = new InputPayload(1L, (byte) 0x01, 0.0f);
        LanePacket packet1 = new LanePacket((byte) 0, 1L, input);
        byte[] bytes1 = fury.serialize(packet1);
        LanePacket deserialized1 = (LanePacket) fury.deserialize(bytes1);
        assertTrue(deserialized1.getPayload() instanceof InputPayload);
        
        // Test with StatePayload
        StatePayload state = new StatePayload(10L, 0, new float[0]);
        LanePacket packet2 = new LanePacket((byte) 1, 2L, state);
        byte[] bytes2 = fury.serialize(packet2);
        LanePacket deserialized2 = (LanePacket) fury.deserialize(bytes2);
        assertTrue(deserialized2.getPayload() instanceof StatePayload);
        
        // Test with AdminCommand
        AdminCommand admin = new AdminCommand((byte) 1, 60.0f);
        LanePacket packet3 = new LanePacket((byte) 0, 3L, admin);
        byte[] bytes3 = fury.serialize(packet3);
        LanePacket deserialized3 = (LanePacket) fury.deserialize(bytes3);
        assertTrue(deserialized3.getPayload() instanceof AdminCommand);
    }

    @Test
    void testSequenceNumberMonotonic() {
        // Test sequence number progression
        LanePacket packet1 = new LanePacket((byte) 1, 100L, null);
        LanePacket packet2 = new LanePacket((byte) 1, 101L, null);
        LanePacket packet3 = new LanePacket((byte) 1, 102L, null);
        
        assertTrue(packet2.getSequence() > packet1.getSequence());
        assertTrue(packet3.getSequence() > packet2.getSequence());
    }
}
