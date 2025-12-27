# Testing Standards & Definition of Done

## 1. Logic Verification (L1)
- **Path Coverage:** All `if/else` branches must be visited.
- **Bitwise Safety:** Tests must assert that setting `Flag A` does not corrupt `Flag B`.
- **Generation Safety:** Tests must assert that accessing a destroyed Entity ID throws an error or returns invalid.

## 2. Performance Baselines (L3)
- **Allocation Rate:** Must be 0 bytes/sec during steady-state gameplay.
- **Execution Time:** `MovementSystem` (10k entities) must stay under 1.0ms.

## 3. Architecture Enforcement (L2)
- **Layering:** `demo-core` must NEVER import `javafx` or `netty`.
- **Cyclomatic Complexity:** No method may have complexity > 10.

## Testing Architecture & Standards

We divide testing into four strictly isolated layers. Mixing layers (e.g., doing IO in a Unit Test) is a build failure.

| Layer | Scope | Framework | Strictness Rule |
|---|---|---|---|
| L1: Logic (Unit) | Math, Physics, Bitwise Flags | JUnit 5 + AssertJ | 100% Branch Coverage. No Mocking frameworks allowed (use real data). |
| L2: Structure (Static) | Code Quality, Forbidden APIs | ArchUnit, PMD, ErrorProne | Zero Violations. Fails compile on allocation-in-loop. |
| L3: Performance (Bench) | Hot Loops, Serialization | JMH (Java Microbenchmark) | Regression Check. New code must be within 5% of baseline. |
| L4: Integration (Sys) | Netty Boot, Client Connect | Testcontainers + JUnit | Leak Detection. Must assert clean shutdown of threads/channels. |

## Verification Checklist (The Gate)

Before any code is merged or marked as "Complete" by an agent/developer, it must pass this manual checklist.

- [ ] Static Analysis Clean: `mvn verify` returns BUILD SUCCESS.
- [ ] Zero-Allocation Proven: The JMH benchmark for the new component shows `·gc.alloc.rate.norm ≈ 0 B/op`.
- [ ] Mutation Check (Mental): If I change a `<` to `<=`, does a test fail?
- [ ] Stale Reference Check: Is there a test case that tries to use an ID after destroy()?
- [ ] Agent Compliance: Does the code strictly follow the `.cursorrules` (e.g., no Streams, no Lists)?
