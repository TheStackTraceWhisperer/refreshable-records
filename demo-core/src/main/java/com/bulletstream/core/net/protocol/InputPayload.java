package com.bulletstream.core.net.protocol;

import java.util.Objects;

/**
 * Client → Server: Player's intent for a specific tick.
 * 
 * Input bitmask values:
 * - 0x01 (1) = Up
 * - 0x02 (2) = Down
 * - 0x04 (4) = Left
 * - 0x08 (8) = Right
 * - 0x10 (16) = Shoot
 */
public final class InputPayload {
    
    /** Client-side prediction tick */
    private long tick;
    
    /** Bitmask for input states */
    private byte inputMask;
    
    /** Aiming angle in radians */
    private float angle;

    /**
     * Default constructor required for Fury serialization.
     */
    public InputPayload() {
    }

    /**
     * Creates a new InputPayload.
     */
    public InputPayload(long tick, byte inputMask, float angle) {
        this.tick = tick;
        this.inputMask = inputMask;
        this.angle = angle;
    }

    public long getTick() {
        return tick;
    }

    public void setTick(long tick) {
        this.tick = tick;
    }

    public byte getInputMask() {
        return inputMask;
    }

    public void setInputMask(byte inputMask) {
        this.inputMask = inputMask;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputPayload that = (InputPayload) o;
        return tick == that.tick 
            && inputMask == that.inputMask 
            && Float.compare(that.angle, angle) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tick, inputMask, angle);
    }

    @Override
    public String toString() {
        return "InputPayload{tick=" + tick + ", inputMask=" + inputMask + ", angle=" + angle + '}';
    }
}
