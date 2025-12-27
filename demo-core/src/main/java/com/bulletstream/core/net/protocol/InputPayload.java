package com.bulletstream.core.net.protocol;

/**
 * Client input payload sent to server.
 * Contains player input for a specific tick.
 */
public final class InputPayload {
    // Input bitmask flags
    public static final byte INPUT_UP = 1;
    public static final byte INPUT_DOWN = 2;
    public static final byte INPUT_LEFT = 4;
    public static final byte INPUT_RIGHT = 8;
    public static final byte INPUT_SHOOT = 16;

    private long tick;
    private byte inputMask;
    private float angle;

    public InputPayload() {
        // Default constructor for Fury serialization
    }

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
    public String toString() {
        return "InputPayload{" +
               "tick=" + tick +
               ", inputMask=" + inputMask +
               ", angle=" + angle +
               '}';
    }
}
