# Phase 1 Alignment Specification: The Core Engine

**Target**: demo-core & demo-server  
**Goal**: Upgrade the placeholder ECS to the "Industrial Grade" specification defined in the Master Plan.

---

## 1. Toolchain Correction (Step 0)

**Current Status**: pom.xml is downgraded to Java 17.  
**Required State**: Must use Java 25 (Preview) to enable Vector API readiness.

- [x] Root POM Update:
  - Set `<java.version>25</java.version>`
  - Update maven-compiler-plugin to 3.13.0 with `<release>25</release>`
  - Add compiler arg: `--enable-preview`
  - Constraint: If CI fails, use 25-ea specific builds, but code must target 25.

---

## 2. Core ECS Architecture (GameWorld.java)

**Current Status**: Basic Position/Velocity arrays only.  
**Required State**: Full Entity Lifecycle with Generation Safety and Flags.

### 2.1 Data Layout (Structure of Arrays)

Refactor GameWorld to include the following contiguous arrays (sized to maxEntities).

| Field | Type | Purpose |
|---|---|---|
| entityIds | int[] | Generation ID. Upper 16 bits = Generation, Lower 16 bits = Index. Prevents stale reference bugs. |
| flags | byte[] | Bitmask: ACTIVE(1), PLAYER(2), BULLET(4), ENEMY(8). |
| radius | float[] | Collision radius (squared) for fast checks. |
| ownerId | int[] | Entity ID of who fired this bullet (for scoring). |
| inputMask | byte[] | Current tick input state (if player). |

### 2.2 Lifecycle Management (The "Free List")

Do not simply increment entityCount. You must implement a recycling stack.

- [x] IntStack freeIndices: Stores indices of destroyed entities.
- [x] Spawn Logic:
```java
int index = freeIndices.isEmpty() ? entityCount++ : freeIndices.pop();
entityIds[index]++; // Increment generation
// Reset all component arrays at [index] to 0
```

- [x] Despawn Logic:
  - Push index to freeIndices.
  - Clear ACTIVE flag.

### 2.3 Spatial Partitioning (SpatialHash.java)

Implement a flattened 2D grid for O(1) collision lookups.

- Grid Structure: `int[] cellHead`, `int[] nextEntity`.
- Logic: Linked-list embedded in arrays (standard DOD approach).
- Cell Size: 64.0f (Game Units).

---

## 3. Network Protocol Definition (demo-core)

**Current Status**: Missing.  
**Required State**: Fury-registered POJOs defined for the Netty pipeline.

Create package `com.bulletstream.core.net.protocol`:

### 3.1 The Envelope

- **LanePacket**:
  - `byte laneId` (0=Reliable/TCP, 1=Unreliable/UDP)
  - `long sequence`
  - `Object payload` (Polymorphic)

### 3.2 Payloads (POJOs)

- **InputPayload**:
  - `long tick`
  - `byte inputMask` (Up/Down/Left/Right/Shoot)
  - `float angle`

- **StatePayload**:
  - `long serverTick`
  - `int entityCount`
  - `float[] packedPositionData` (Format: [id, x, y, id, x, y...])

- **AdminCommand**:
  - `int type` (1=SetTickRate, 2=SetTimeScale)
  - `float value`

---

## 4. Server Logic Alignment (demo-server)

**Current Status**: Basic while(running) loop.  
**Required State**: Rigid Fixed-Step Accumulator Loop.

### 4.1 The Loop (GameServer.java)

Refactor the loop to strictly separate Logic Time from Render/Wall Time.

```java
double t = 0.0;
double dt = 1.0 / 60.0;
double currentTime = System.nanoTime();
double accumulator = 0.0;

while (running) {
    double newTime = System.nanoTime();
    double frameTime = newTime - currentTime;
    currentTime = newTime;
    accumulator += frameTime;

    while (accumulator >= dt) {
        // 1. Drain Network Queue (JCTools) -> Apply Inputs
        // 2. Physics Step (GameWorld.update)
        // 3. Collision Step (SpatialHash)
        // 4. Pack & Broadcast State (if tick % sendRate == 0)
        t += dt;
        accumulator -= dt;
    }
}
```

---

## 5. Verification Requirements (Tests)

Update demo-core tests to enforce the new specs.

- [x] **GameWorldTest.java**:
  - Test: `testGenerationIncrementOnRecycle()` (Verify ID changes after destroy/create).
  - Test: `testStaleAccessThrowsException()` (Verify accessing old ID fails).

- [x] **SpatialHashTest.java**:
  - Test: `testNeighborQuery()` (Verify entities in adjacent cells are found).

- [x] **SerializationTest.java**:
  - Test: `testFuryRoundTrip()` (Verify StatePayload serializes identically).

---

## 6. Implementation Checklist

- [x] Update pom.xml to Java 25.
- [x] Implement GameWorld arrays (Flags, Owner, GenID).
- [x] Implement SpatialHash.
- [x] Create Packet classes in demo-core.
- [x] Refactor GameServer loop to use Accumulator.
- [x] Run `mvn verify` to ensure 0 allocations in hot paths.

---

## Notes

This specification follows the Master Engineering Plan's requirements for:
- Data-Oriented Design with Structure of Arrays
- Zero-Allocation hot paths
- Java 25 with preview features
- Test-Driven Development with StrictUnitTest
- 100% deterministic behavior
