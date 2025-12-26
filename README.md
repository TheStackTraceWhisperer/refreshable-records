# BulletStream

A high-performance, data-oriented Entity Component System (ECS) game server built with Java 25.

## Overview

BulletStream is an industrial-grade game server implementation featuring:
- **Data-Oriented Design**: ECS architecture using primitive arrays for zero-allocation updates
- **High-Performance Networking**: Netty 4.1 with NIO/Epoll and Fury serialization
- **Strict Quality Enforcement**: ErrorProne, PMD, SpotBugs, and ForbiddenAPIs
- **Modern Java**: Java 25 (Early Access) with preview features enabled

## Project Structure

```
bulletstream/
├── build-config/              # Shared enforcement configuration
│   ├── signatures.txt         # Banned API signatures
│   └── pmd-custom-rules.xml   # Complexity & performance rules
├── demo-core/                 # Shared math & data structures
├── demo-server/               # Authoritative game logic
├── demo-fx-client/            # JavaFX visual debugger
├── demo-bot-client/           # Headless stress testing
├── demo-benchmarks/           # JMH performance tests
└── pom.xml                    # Root build configuration
```

## Modules

| Module | Description | Key Dependencies |
|--------|-------------|------------------|
| **demo-core** | Physics engine, GameWorld (int[] arrays), and packet definitions | fury-core, jctools-core |
| **demo-server** | Netty bootstrapper and authoritative game loop | netty-all, demo-core |
| **demo-fx-client** | Visual debugger rendering game state to Canvas | javafx-controls, demo-core |
| **demo-bot-client** | Headless stress tester spawning virtual threads | demo-core |
| **demo-benchmarks** | JMH performance tests | jmh-core, demo-core |

## Requirements

- **JDK 25 (Early Access)**: Download from [OpenJDK](https://jdk.java.net/25/)
- **Maven 3.9+**: Required for multi-module build
- **IDE Support**: Configure for Java 25 preview features

## Build & Run

### Build the project
```bash
mvn clean install
```

### Run verification (includes static analysis)
```bash
mvn clean verify
```

### Run the server
```bash
java --enable-preview -jar demo-server/target/demo-server-1.0.0-SNAPSHOT.jar
```

### Run benchmarks
```bash
java -jar demo-benchmarks/target/benchmarks.jar
```

## Quality Enforcement

The build enforces strict quality standards:

### Forbidden APIs
Banned APIs include:
- `System.out` / `System.err` (use SLF4J)
- `printStackTrace()` (use logging)
- Legacy collections: `Hashtable`, `Vector`
- Legacy date/time: `Date`, `Calendar`

### PMD Rules
- **Cyclomatic Complexity**: Maximum 10 per method
- **Performance**: No object allocation in loops
- **Best Practices**: No unused variables, imports, or methods

### Error Prone
Catches common Java mistakes at compile time with enhanced static analysis.

### SpotBugs
Detects bugs, security vulnerabilities, and code smells.

## Design Principles

### Zero-Allocation Game Loop
The core game loop uses primitive arrays and avoids object allocation:
```java
// Data-Oriented: Arrays of primitives
float[] positionsX = new float[1000];
float[] positionsY = new float[1000];

// Zero-allocation update
for (int i = 0; i < entityCount; i++) {
    positionsX[i] += velocitiesX[i] * deltaTime;
}
```

### Single-Threaded Server
The authoritative server runs on a single thread to avoid synchronization overhead.

### Lock-Free Client Communication
Uses JCTools for lock-free queues in high-throughput scenarios.

## Testing

### Unit Tests
```bash
mvn test
```

### Architecture Tests
Uses ArchUnit to enforce architectural constraints:
- No circular dependencies
- Proper layer separation
- Dependency rules

### Performance Tests
JMH benchmarks measure:
- GameWorld update throughput
- Serialization performance
- Network packet processing

## CI/CD

GitHub Actions workflow automatically:
1. Builds with Java 25-EA
2. Runs all tests
3. Executes static analysis (PMD, SpotBugs, ForbiddenAPIs)
4. Uploads artifacts and reports

## Development

### Adding a New Module
1. Create module directory
2. Add `pom.xml` with parent reference
3. Add module to root `pom.xml` `<modules>` section
4. Implement with strict adherence to quality rules

### Troubleshooting Build Failures

**Forbidden API violation:**
```
[ForbiddenAPIs] ... System.out is forbidden
```
Solution: Replace with SLF4J logging

**PMD Complexity violation:**
```
PMD Violation: Cyclomatic Complexity
```
Solution: Refactor method to reduce complexity below 10

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
