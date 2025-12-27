package com.bulletstream.core.net.protocol;

/**
 * Envelope for all network packets.
 * Supports dual-lane architecture (TCP/UDP).
 */
public final class LanePacket {
    public static final byte LANE_RELIABLE = 0;   // TCP
    public static final byte LANE_UNRELIABLE = 1; // UDP

    private byte laneId;
    private long sequence;
    private Object payload;

    public LanePacket() {
        // Default constructor for Fury serialization
    }

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
    public String toString() {
        return "LanePacket{" +
               "laneId=" + laneId +
               ", sequence=" + sequence +
               ", payload=" + payload +
               '}';
    }
}
