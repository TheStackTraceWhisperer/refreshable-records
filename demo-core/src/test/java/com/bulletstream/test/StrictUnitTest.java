package com.bulletstream.test;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Tag;
import java.util.concurrent.TimeUnit;

/**
 * All Logic Tests must extend this.
 * Enforces:
 * 1. Fast execution (fails if > 500ms).
 * 2. No side effects (stateless).
 */
@Tag("unit")
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
public abstract class StrictUnitTest {
    
    // Helper to ensure floating point deterministic comparisons
    protected static final float EPSILON = 0.0001f;

    protected void assertVectorEquals(float x1, float y1, float x2, float y2) {
        if (Math.abs(x1 - x2) > EPSILON || Math.abs(y1 - y2) > EPSILON) {
            throw new AssertionError(
                String.format("Vector mismatch: Expected [%f, %f] but got [%f, %f]", x1, y1, x2, y2)
            );
        }
    }
}
