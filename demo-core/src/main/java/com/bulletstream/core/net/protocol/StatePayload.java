package com.bulletstream.core.net.protocol;

import java.util.Arrays;

/**
 * Server state payload broadcast to clients.
 * Contains snapshot of game state for a specific tick.
 */
public final class StatePayload {
    private long serverTick;
    private int entityCount;
    private float[] packedPositionData; // Format: [id, x, y, id, x, y, ...]

    public StatePayload() {
        // Default constructor for Fury serialization
    }

    public StatePayload(long serverTick, int entityCount, float[] packedPositionData) {
        this.serverTick = serverTick;
        this.entityCount = entityCount;
        this.packedPositionData = packedPositionData;
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

    public float[] getPackedPositionData() {
        return packedPositionData;
    }

    public void setPackedPositionData(float[] packedPositionData) {
        this.packedPositionData = packedPositionData;
    }

    @Override
    public String toString() {
        return "StatePayload{" +
               "serverTick=" + serverTick +
               ", entityCount=" + entityCount +
               ", packedPositionData=" + Arrays.toString(packedPositionData) +
               '}';
    }
}
