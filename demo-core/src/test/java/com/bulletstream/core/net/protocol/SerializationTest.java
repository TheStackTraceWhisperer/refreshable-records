package com.bulletstream.core.net.protocol;

import com.bulletstream.test.StrictUnitTest;
import io.fury.Fury;
import io.fury.config.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializationTest extends StrictUnitTest {

    private Fury fury;

    @BeforeEach
    void setUp() {
        fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(false)
                .build();
        
        // Register protocol classes
        fury.register(LanePacket.class);
        fury.register(InputPayload.class);
        fury.register(StatePayload.class);
        fury.register(AdminCommand.class);
    }

    @Test
    void testFuryRoundTrip() {
        // Create StatePayload
        float[] data = new float[]{1.0f, 100.0f, 200.0f, 2.0f, 150.0f, 250.0f};
        StatePayload original = new StatePayload(42L, 2, data);
        
        // Serialize
        byte[] bytes = fury.serialize(original);
        
        // Deserialize
        StatePayload deserialized = (StatePayload) fury.deserialize(bytes);
        
        // Verify
        assertEquals(original.getServerTick(), deserialized.getServerTick());
        assertEquals(original.getEntityCount(), deserialized.getEntityCount());
        assertArrayEquals(original.getPackedPositionData(), deserialized.getPackedPositionData());
    }

    @Test
    void testInputPayloadSerialization() {
        InputPayload original = new InputPayload(
            100L,
            (byte) (InputPayload.INPUT_UP | InputPayload.INPUT_SHOOT),
            1.57f
        );
        
        byte[] bytes = fury.serialize(original);
        InputPayload deserialized = (InputPayload) fury.deserialize(bytes);
        
        assertEquals(original.getTick(), deserialized.getTick());
        assertEquals(original.getInputMask(), deserialized.getInputMask());
        assertEquals(original.getAngle(), deserialized.getAngle(), 0.001f);
    }

    @Test
    void testLanePacketSerialization() {
        InputPayload payload = new InputPayload(50L, (byte) 5, 0.0f);
        LanePacket original = new LanePacket(LanePacket.LANE_RELIABLE, 123L, payload);
        
        byte[] bytes = fury.serialize(original);
        LanePacket deserialized = (LanePacket) fury.deserialize(bytes);
        
        assertEquals(original.getLaneId(), deserialized.getLaneId());
        assertEquals(original.getSequence(), deserialized.getSequence());
        assertNotNull(deserialized.getPayload());
        assertTrue(deserialized.getPayload() instanceof InputPayload);
    }

    @Test
    void testAdminCommandSerialization() {
        AdminCommand original = new AdminCommand(AdminCommand.TYPE_SET_TICK_RATE, 60.0f);
        
        byte[] bytes = fury.serialize(original);
        AdminCommand deserialized = (AdminCommand) fury.deserialize(bytes);
        
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getValue(), deserialized.getValue(), 0.001f);
    }

    @Test
    void testEmptyStatePayloadSerialization() {
        StatePayload original = new StatePayload(0L, 0, new float[0]);
        
        byte[] bytes = fury.serialize(original);
        StatePayload deserialized = (StatePayload) fury.deserialize(bytes);
        
        assertEquals(0L, deserialized.getServerTick());
        assertEquals(0, deserialized.getEntityCount());
        assertEquals(0, deserialized.getPackedPositionData().length);
    }

    @Test
    void testLargeStatePayloadSerialization() {
        // Create payload with 100 entities (300 floats)
        float[] data = new float[300];
        for (int i = 0; i < 100; i++) {
            data[i * 3] = i;           // id
            data[i * 3 + 1] = i * 10;  // x
            data[i * 3 + 2] = i * 20;  // y
        }
        
        StatePayload original = new StatePayload(1000L, 100, data);
        
        byte[] bytes = fury.serialize(original);
        StatePayload deserialized = (StatePayload) fury.deserialize(bytes);
        
        assertEquals(original.getServerTick(), deserialized.getServerTick());
        assertEquals(original.getEntityCount(), deserialized.getEntityCount());
        assertArrayEquals(original.getPackedPositionData(), deserialized.getPackedPositionData());
    }
}
