# GitHub Copilot Instructions for BulletStream

You are working on BulletStream, a high-performance data-oriented ECS game server. Follow these instructions strictly.

## CRITICAL: Java Version Requirement

**Java 25 is RELEASED and REQUIRED for this project.**
- This project MUST use Java 25 (not Java 17, not Java 21, not any earlier version)
- Preview features are ENABLED with `--enable-preview`
- The JDK 25 is available and should be installed from: https://jdk.java.net/25/
- All code must compile and run with Java 25
- DO NOT suggest downgrading to earlier Java versions
- DO NOT disable preview features

## Core Principles

### 1. Data-Oriented Design
- **NEVER** create classes for game entities
- Use primitive arrays (`int[]`, `float[]`) in `GameWorld`
- Store components in structure-of-arrays (SoA) layout
- Example:
  ```java
  // CORRECT
  private final float[] positionsX;
  private final float[] positionsY;
  
  // WRONG
  class Entity { float x, y; }
  List<Entity> entities;
  ```

### 2. Zero-Allocation Hot Paths
- **NEVER** use `new` inside:
  - `update()` methods
  - `loop()` methods
  - Any System execution
- Use object pools or pre-allocated buffers
- Verify with JMH benchmarks: `gc.alloc.rate.norm ≈ 0 B/op`

### 3. Test-Driven Development
- Write the TEST before the IMPLEMENTATION
- All unit tests extend `StrictUnitTest`
- Tests must complete in < 500ms
- Cover edge cases: overflow, stale IDs, negative indices

### 4. No Boxing/Autoboxing
- Use primitives: `int`, `float`, `long`
- **NEVER** use: `Integer`, `Float`, `List<T>`, `Stream`
- Use primitive collections from JCTools if needed

## Forbidden Patterns

These patterns will FAIL the build:

```java
// ❌ FORBIDDEN
System.out.println("debug");           // Use SLF4J
list.stream().filter(...);             // Allocates iterators
synchronized (lock) { }                // Use JCTools queues
for (Entity e : entities) { }          // Use indexed loops
throw new Exception("error");          // Catch specific types
```

## Required Patterns

```java
// ✅ REQUIRED
log.debug("message");                  // SLF4J logging
for (int i=0; i<count; i++) { }       // Indexed loops
private final float[] data;            // Primitive arrays
try { } catch (IOException e) { }     // Specific exceptions
```

## Testing Layers

| Layer | Purpose | Framework | Rule |
|-------|---------|-----------|------|
| L1 | Unit (Logic) | JUnit 5 + StrictUnitTest | 100% branch coverage, < 500ms |
| L2 | Static Analysis | PMD, SpotBugs, ForbiddenAPIs | Zero violations |
| L3 | Performance | JMH Benchmarks | 0 allocation, < 5% regression |
| L4 | Integration | Testcontainers | No thread/channel leaks |

## When Writing a System

1. Create the test first (extends `StrictUnitTest`)
2. Create the implementation
3. Create a JMH benchmark (use `SystemBenchmarkTemplate`)
4. Verify allocation: `mvn clean verify`
5. Check GC: `gc.alloc.rate.norm ≈ 0 B/op`

## Module Layering Rules

- `demo-core`: **NEVER** import `javafx`, `netty`, or UI libs
- `demo-server`: Can import `netty`, `demo-core`
- `demo-fx-client`: Can import `javafx`, `demo-core`
- Violations detected by ArchUnit tests

## Code Quality Gates

All PRs must pass:
- `mvn verify`: Static analysis clean
- JMH benchmarks: Zero allocation in hot paths
- Unit tests: 100% branch coverage
- ArchUnit: No circular dependencies
- Complexity: Methods ≤ 10 cyclomatic complexity

## Error Handling Philosophy

- **Fail Fast**: Game logic errors crash the tick
- **Specific**: Catch specific exceptions, not `Exception`
- **Resource Cleanup**: Netty IO errors must close channels
- **No Silent Failures**: Log all errors at appropriate level

## Performance Standards

- `MovementSystem` (10k entities): < 1.0ms
- Allocation rate in steady state: 0 bytes/sec
- Network serialization: < 100μs per packet
- Frame time budget: 16.6ms (60 FPS)

## Questions?

Refer to:
- `.cursorrules` - AI agent behavior rules
- `docs/dev/testing_standards.md` - Testing requirements
- `README.md` - Project overview

Remember: **ZERO-ALLOCATION** and **100% DETERMINISM** are non-negotiable.
