# Testing Infrastructure Overview

This document provides an overview of the BulletStream testing infrastructure and how to use it.

## Quick Reference

### Running Tests

```bash
# All tests
mvn test

# Unit tests only (fast)
mvn test -Dgroups=unit

# With benchmarks
mvn verify

# Single test class
mvn test -Dtest=GameWorldTest
```

### Creating a New Test

```java
package com.bulletstream.core;

import com.bulletstream.test.StrictUnitTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MySystemTest extends StrictUnitTest {
    
    @Test
    void testEdgeCase() {
        // Your test here
        // Automatically enforces:
        // - Completes in < 500ms
        // - Tagged as @Tag("unit")
        // - Access to EPSILON and assertVectorEquals()
    }
}
```

### Creating a New Benchmark

```java
package com.bulletstream.benchmarks;

import com.bulletstream.bench.SystemBenchmarkTemplate;
import com.bulletstream.core.GameWorld;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
public class MySystemBenchmark extends SystemBenchmarkTemplate {

    private GameWorld world;

    @Setup(Level.Trial)
    public void setup() {
        world = new GameWorld(100_000);
        // Populate with test data
    }

    @Benchmark
    public void benchmarkMySystem() {
        world.update(0.016f);
    }
}
```

## Test Files Structure

```
demo-core/src/test/java/
├── com/bulletstream/
│   ├── core/
│   │   └── GameWorldTest.java        # Extends StrictUnitTest
│   └── test/
│       └── StrictUnitTest.java       # Base class for all unit tests

demo-benchmarks/src/main/java/
└── com/bulletstream/
    ├── benchmarks/
    │   └── GameWorldBenchmark.java   # Actual benchmarks
    └── bench/
        └── SystemBenchmarkTemplate.java  # Template for new benchmarks
```

## Testing Standards Files

- **`.cursorrules`** - AI agent behavior rules (root)
- **`docs/dev/testing_standards.md`** - Detailed testing requirements
- **`.github/copilot-instructions.md`** - GitHub Copilot specific instructions

## The Four Testing Layers

### L1: Unit Tests (Logic)
- **Scope**: Pure logic, math, physics, data structures
- **Framework**: JUnit 5 + StrictUnitTest
- **Rules**:
  - Must extend `StrictUnitTest`
  - Complete in < 500ms
  - 100% branch coverage
  - No mocking frameworks (use real data)
  - Test edge cases: overflow, negative, NaN

### L2: Static Analysis (Structure)
- **Scope**: Code quality, architecture
- **Tools**: PMD, SpotBugs, ForbiddenAPIs, ArchUnit
- **Rules**:
  - Zero violations
  - Cyclomatic complexity ≤ 10
  - No forbidden APIs (System.out, printStackTrace)
  - Module dependencies enforced

### L3: Performance Tests (Benchmarks)
- **Scope**: Hot loops, serialization, systems
- **Framework**: JMH
- **Rules**:
  - Allocation rate: 0 bytes/sec in steady state
  - Execution time within budget
  - Regression check: ±5% from baseline

### L4: Integration Tests (System)
- **Scope**: Netty server, client connections, end-to-end
- **Framework**: Testcontainers + JUnit
- **Rules**:
  - Leak detection enabled
  - Clean shutdown verification
  - Thread pool cleanup asserted

## Best Practices

### DO ✅
- Write test before implementation
- Use primitive arrays for data
- Use indexed loops: `for (int i=0; i<count; i++)`
- Extend `StrictUnitTest` for unit tests
- Create JMH benchmark for each system
- Test edge cases and boundaries
- Use SLF4J for logging

### DON'T ❌
- Use `System.out.println()`
- Create classes for entities
- Use `List<T>` or `Stream` in core
- Use `synchronized` blocks
- Allocate in hot paths
- Ignore test failures
- Skip benchmarks

## Verification Checklist

Before marking work complete:

- [ ] `mvn verify` returns BUILD SUCCESS
- [ ] JMH shows `gc.alloc.rate.norm ≈ 0 B/op`
- [ ] Unit tests cover all branches
- [ ] Tests complete in < 500ms
- [ ] No forbidden APIs used
- [ ] ArchUnit tests pass
- [ ] Cyclomatic complexity ≤ 10

## Examples

### Good Test Example

```java
class VectorMathTest extends StrictUnitTest {
    
    @Test
    void testNormalize_ZeroVector_ReturnsZero() {
        float[] result = VectorMath.normalize(0, 0);
        assertVectorEquals(0, 0, result[0], result[1]);
    }
    
    @Test
    void testNormalize_UnitVector_Unchanged() {
        float[] result = VectorMath.normalize(1, 0);
        assertVectorEquals(1, 0, result[0], result[1]);
    }
}
```

### Good Benchmark Example

```java
@State(Scope.Thread)
public class CollisionSystemBenchmark {
    
    private GameWorld world;
    
    @Setup(Level.Trial)
    public void setup() {
        world = new GameWorld(10_000);
        for (int i = 0; i < 10_000; i++) {
            world.addEntity(i * 10, i * 10, 1, 1);
        }
    }
    
    @Benchmark
    public void benchmarkCollisionDetection() {
        world.updateCollisions();
    }
}
```

## Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JMH Documentation](https://github.com/openjdk/jmh)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- BulletStream README.md
- BulletStream testing_standards.md
