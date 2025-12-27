# Java 25 Library Compatibility Status

## Overview

BulletStream is built with Java 25 with preview features enabled. This document tracks the compatibility status of all dependencies with Java 25.

## Current Status

### ✅ Fully Compatible (Updated for Java 25)

**Core Libraries**
- **Guava 33.5.0-jre**: Latest version with improved Java 25 compatibility (upgraded from 32.1.2-jre)
- **ArchUnit 1.4.1**: Latest version with ASM updates for better bytecode compatibility (upgraded from 1.3.0)
- **JavaFX 24.0.2**: Latest stable release with Java 25 support (upgraded from 23.0.1)
- **JUnit 5.11.4**: Full support for Java 25
- **SLF4J 2.0.16**: Full support for Java 25
- **Netty 4.1.115.Final**: Full support (uses NIO, no bytecode manipulation)
- **JCTools 4.0.5**: Full support (lock-free data structures)
- **JMH 1.37**: Full support for benchmarking

### ⚠️ Working with Warnings

**Fury 0.4.1**
- **Status**: Working but uses deprecated `sun.misc.Unsafe` methods
- **Warning**: `Unsafe::arrayBaseOffset` is terminally deprecated and will be removed in future Java releases
- **Impact**: Functional but will require update when newer Fury releases become available
- **Note**: Fury 0.4.1 is the latest available version (as of Dec 2024)
- **Workaround**: Currently no alternative; serialization works correctly despite warnings
- **Future**: Monitor Fury project for updates that remove Unsafe usage

### ✅ Build Tools (Working)
- **Maven 3.9.11**: Full support for Java 25
- **Maven Compiler Plugin 3.13.0**: Full support with `--enable-preview`
- **Maven Surefire Plugin 3.5.2**: Full support for running tests with preview features

### ⚠️ Temporarily Disabled
The following tools have been temporarily disabled until they release Java 25 compatible versions:

#### PMD (maven-pmd-plugin 3.25.0)
- **Issue**: PMD 7.8.0 doesn't fully support Java 25 syntax
- **Impact**: Code complexity and style checks are not running
- **Workaround**: Disabled in root pom.xml (configured to Java 21 target but disabled)
- **Tracking**: Will re-enable when PMD releases Java 25 support

#### ForbiddenAPIs (forbiddenapis-maven-plugin 3.8)
- **Issue**: Uses ASM library which doesn't support class file format 69 (Java 25)
- **Impact**: Banned API checks (System.out, printStackTrace, etc.) are not enforced
- **Workaround**: Disabled in root pom.xml
- **Tracking**: Requires ASM 9.7+ with Java 25 support

#### SpotBugs (spotbugs-maven-plugin 4.8.6.4)
- **Issue**: Uses ASM library which doesn't support class file format 69
- **Impact**: Bug detection and security vulnerability scanning is not running
- **Workaround**: Disabled in root pom.xml and demo-benchmarks pom.xml
- **Tracking**: Requires ASM 9.7+ with Java 25 support

## Dependency Version Summary

| Library | Previous Version | Current Version | Status |
|---------|-----------------|-----------------|--------|
| Guava | 32.1.2-jre | 33.5.0-jre | ✅ Updated |
| ArchUnit | 1.3.0 | 1.4.1 | ✅ Updated |
| JavaFX | 23.0.1 | 24.0.2 | ✅ Updated |
| Fury | 0.4.1 | 0.4.1 | ⚠️ Warnings |
| Netty | 4.1.115.Final | 4.1.115.Final | ✅ Compatible |
| JCTools | 4.0.5 | 4.0.5 | ✅ Compatible |
| SLF4J | 2.0.16 | 2.0.16 | ✅ Compatible |
| JUnit | 5.11.4 | 5.11.4 | ✅ Compatible |
| JMH | 1.37 | 1.37 | ✅ Compatible |

## Known Warnings

### sun.misc.Unsafe Deprecation Warnings

```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::arrayBaseOffset has been called by io.fury.util.Platform
WARNING: Please consider reporting this to the maintainers of class io.fury.util.Platform
WARNING: sun.misc.Unsafe::arrayBaseOffset will be removed in a future release
```

**Source**: Fury 0.4.1  
**Impact**: None - serialization works correctly  
**Action**: These warnings are informational only. Fury will need to update their implementation before Java removes these methods.

## Manual Verification

Until static analysis tools are re-enabled, developers should manually verify:

1. **No Forbidden APIs**: Don't use `System.out`, `System.err`, `printStackTrace()`, legacy collections
2. **Code Complexity**: Keep methods under 10 cyclomatic complexity
3. **Code Quality**: Follow existing patterns and best practices
4. **Security**: Review code for common vulnerabilities

## Timeline

- **Q1 2025**: Expected ASM 9.7 release with Java 25 support
- **Q2 2025**: Expected PMD, ForbiddenAPIs, and SpotBugs updates
- **Ongoing**: Monitor Fury project for Unsafe API updates

## Testing Status

All runtime tests pass successfully:
- **20/20 unit tests** passing
- **Zero test failures**
- **Build**: SUCCESS
- **Functionality**: All features working correctly despite warnings

## Recommendations

1. **Continue monitoring** Fury project releases for Unsafe API fixes
2. **Update immediately** when PMD, ForbiddenAPIs, and SpotBugs support Java 25
3. **Manual code review** for patterns typically caught by disabled static analysis tools
4. **Regular dependency updates** to pick up Java 25 improvements

## Re-enabling Tools

When tools are updated, uncomment the following sections in `pom.xml`:

1. Root `pom.xml`: Uncomment PMD, ForbiddenAPIs, and SpotBugs plugins
2. `demo-benchmarks/pom.xml`: Uncomment PMD and SpotBugs plugins

```xml
<!-- In root pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
</plugin>
```

## Notes

- All **unit tests** (20 tests) pass successfully
- **Compilation** works correctly with preview features enabled
- **Runtime** behavior is verified through comprehensive testing
- Code follows **data-oriented design** principles and **zero-allocation** requirements
- **Guava, ArchUnit, and JavaFX** have been updated to their latest versions for better Java 25 compatibility
