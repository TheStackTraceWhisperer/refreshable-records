package com.bulletstream.core.net.protocol;

import java.util.Objects;

/**
 * Dev → Server: Control server variables on the fly.
 * 
 * Command types:
 * - 1 = SET_TICK_RATE
 * - 2 = FORCE_RESYNC
 */
public final class AdminCommand {
    
    /** Command type identifier */
    private byte commandType;
    
    /** Command argument */
    private float value;

    /**
     * Default constructor required for Fury serialization.
     */
    public AdminCommand() {
    }

    /**
     * Creates a new AdminCommand.
     */
    public AdminCommand(byte commandType, float value) {
        this.commandType = commandType;
        this.value = value;
    }

    public byte getCommandType() {
        return commandType;
    }

    public void setCommandType(byte commandType) {
        this.commandType = commandType;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminCommand that = (AdminCommand) o;
        return commandType == that.commandType 
            && Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, value);
    }

    @Override
    public String toString() {
        return "AdminCommand{commandType=" + commandType + ", value=" + value + '}';
    }
}
