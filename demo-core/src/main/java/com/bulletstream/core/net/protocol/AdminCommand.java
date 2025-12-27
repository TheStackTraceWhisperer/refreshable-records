package com.bulletstream.core.net.protocol;

/**
 * Admin command payload for server configuration.
 */
public final class AdminCommand {
    public static final int TYPE_SET_TICK_RATE = 1;
    public static final int TYPE_SET_TIME_SCALE = 2;

    private int type;
    private float value;

    public AdminCommand() {
        // Default constructor for Fury serialization
    }

    public AdminCommand(int type, float value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AdminCommand{" +
               "type=" + type +
               ", value=" + value +
               '}';
    }
}
