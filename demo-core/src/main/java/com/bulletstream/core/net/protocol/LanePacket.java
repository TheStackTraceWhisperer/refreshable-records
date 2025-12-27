package com.bulletstream.core.net.protocol;

import java.util.Objects;

/**
 * Multiplexes different logical streams over a single UDP socket.
 * The envelope for all network messages.
 * 
 * Lane IDs:
 * - 0 = Reliable/TCP-like delivery
 * - 1 = Unreliable/UDP-like delivery
 */
public final class LanePacket {
    
    /** Lane identifier: 0=Reliable, 1=Unreliable */
    private byte laneId;
    
    /** Monotonic counter for packet loss detection */
    private long sequence;
    
    /** The polymorphic message payload */
    private Object payload;

    /**
     * Default constructor required for Fury serialization.
     */
    public LanePacket() {
    }

    /**
     * Creates a new LanePacket with the specified lane, sequence, and payload.
     */
    public LanePacket(byte laneId, long sequence, Object payload) {
        this.laneId = laneId;
        this.sequence = sequence;
        this.payload = payload;
    }

    public byte getLaneId() {
        return laneId;
    }

    public void setLaneId(byte laneId) {
        this.laneId = laneId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanePacket that = (LanePacket) o;
        return laneId == that.laneId 
            && sequence == that.sequence 
            && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(laneId, sequence, payload);
    }

    @Override
    public String toString() {
        return "LanePacket{laneId=" + laneId + ", sequence=" + sequence + ", payload=" + payload + '}';
    }
}
