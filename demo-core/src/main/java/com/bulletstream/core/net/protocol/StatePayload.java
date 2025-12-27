package com.bulletstream.core.net.protocol;

import java.util.Arrays;
import java.util.Objects;

/**
 * Server → Client: Authoritative world snapshot.
 * 
 * Packed data format: [ID, X, Y, TYPE, ID, X, Y, TYPE, ...]
 * Uses float[] to avoid object overhead (IDs are cast to float).
 */
public final class StatePayload {
    
    /** Server's current tick */
    private long serverTick;
    
    /** Number of active entities */
    private int entityCount;
    
    /** Flattened array: [ID, X, Y, TYPE, ...] */
    private float[] packedData;

    /**
     * Default constructor required for Fury serialization.
     */
    public StatePayload() {
    }

    /**
     * Creates a new StatePayload.
     */
    public StatePayload(long serverTick, int entityCount, float[] packedData) {
        this.serverTick = serverTick;
        this.entityCount = entityCount;
        this.packedData = packedData;
    }

    public long getServerTick() {
        return serverTick;
    }

    public void setServerTick(long serverTick) {
        this.serverTick = serverTick;
    }

    public int getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(int entityCount) {
        this.entityCount = entityCount;
    }

    public float[] getPackedData() {
        return packedData;
    }

    public void setPackedData(float[] packedData) {
        this.packedData = packedData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatePayload that = (StatePayload) o;
        return serverTick == that.serverTick 
            && entityCount == that.entityCount 
            && Arrays.equals(packedData, that.packedData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(serverTick, entityCount);
        result = 31 * result + Arrays.hashCode(packedData);
        return result;
    }

    @Override
    public String toString() {
        return "StatePayload{serverTick=" + serverTick 
            + ", entityCount=" + entityCount 
            + ", packedData=" + Arrays.toString(packedData) + '}';
    }
}
