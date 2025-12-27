package com.bulletstream.core.util;

/**
 * Fixed-capacity primitive int stack for entity index recycling.
 * Zero-allocation push/pop operations.
 */
public final class IntStack {
    private final int[] data;
    private int size;

    public IntStack(int capacity) {
        this.data = new int[capacity];
        this.size = 0;
    }

    /**
     * Push an integer onto the stack.
     * @param value the value to push
     * @throws IllegalStateException if stack is full
     */
    public void push(int value) {
        if (size >= data.length) {
            throw new IllegalStateException("IntStack is full");
        }
        data[size++] = value;
    }

    /**
     * Pop an integer from the stack.
     * @return the popped value
     * @throws IllegalStateException if stack is empty
     */
    public int pop() {
        if (size == 0) {
            throw new IllegalStateException("IntStack is empty");
        }
        return data[--size];
    }

    /**
     * Check if the stack is empty.
     * @return true if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get the current size of the stack.
     * @return the number of elements
     */
    public int size() {
        return size;
    }

    /**
     * Clear all elements from the stack.
     */
    public void clear() {
        size = 0;
    }
}
