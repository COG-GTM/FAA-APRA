# FAA-APRA Dependency Upgrade Plan

## Executive Summary

This document outlines the comprehensive plan to modernize dependencies in the FAA Aeronautical Product Release API (APRA) repository. The upgrades address critical security vulnerabilities, resolve conflicting dependencies, and bring the codebase to current supported versions.

## Current State Analysis

### Dependencies Requiring Upgrade

| Dependency | Current Version | Target Version | Priority | Risk Level |
|------------|-----------------|----------------|----------|------------|
| Log4j 1.x | 1.2.12 | REMOVE | Critical | High (CVE-2019-17571, CVE-2021-4104) |
| Log4j 2.x | 2.4.1/2.17.1 | 2.23.1 | Critical | Medium |
| Jackson | 2.15.2 | 2.17.2 | High | Low |
| Jersey | 2.25.1 | 2.43 | Medium | Medium |
| Swagger | 1.5.0 | OpenAPI 3.x (swagger-core 2.2.x) | Medium | High |
| JUnit | 4.12 | 5.10.2 | Low | Medium |
| Maven Compiler Plugin | 2.5.1 | 3.12.1 | Low | Low |
| Java Target | 1.8 | 17 | Medium | Medium |
| javax.ws.rs-api | 2.0.1 | 2.1.1 | Low | Low |

### Repository Security Issues

The following Maven repositories use insecure HTTP:
- http://artifactory.faa.gov:8081/artifactory/libs-release
- http://artifactory.faa.gov:8081/artifactory/libs-snapshot
- http://artifactory.faa.gov:8081/artifactory/faa-apra

## Upgrade Order and Rationale

The upgrades will be performed in the following order to minimize risk and ensure stability:

### Phase 1: Security-Critical Updates (Immediate)

**1.1 Remove Log4j 1.x and Consolidate Log4j 2.x**

The Log4j 1.x library (version 1.2.12) has multiple critical CVEs including remote code execution vulnerabilities. This must be removed immediately.

Current state:
- Log4j 1.x (1.2.12) - MUST REMOVE
- Log4j 2.x core (2.17.1)
- Log4j 2.x slf4j-impl (2.4.1) - version mismatch
- Log4j 2.x web (2.4.1) - version mismatch

Target state:
- Log4j 2.x core (2.23.1)
- Log4j 2.x slf4j2-impl (2.23.1) - note: slf4j2-impl for SLF4J 2.x compatibility
- Log4j 2.x web (2.23.1)
- Log4j 1.x bridge (2.23.1) - for any transitive dependencies

Migration steps:
1. Remove the log4j:log4j:1.2.12 dependency
2. Update all Log4j 2.x dependencies to version 2.23.1
3. Change log4j-slf4j-impl to log4j-slf4j2-impl for SLF4J 2.x
4. Add log4j-1.2-api bridge for any transitive Log4j 1.x dependencies

Code impact: The codebase uses SLF4J for logging (org.slf4j.Logger), so no source code changes are required. The only Log4j 2.x direct usage is ThreadContext in ProductApiListener.java, which remains compatible.

**1.2 Update Jackson to 2.17.2**

Jackson 2.15.2 has known security patches in later versions.

Migration steps:
1. Update jackson-core to 2.17.2
2. Update jackson-databind to 2.17.2

Code impact: None - API is backward compatible.

### Phase 2: Framework Updates (High Priority)

**2.1 Update Jersey to 2.43**

Jersey 2.25.1 is from 2017 and lacks modern features and security patches.

Migration steps:
1. Update jersey.version property to 2.43
2. Update jersey-container-servlet explicit version to 2.43
3. Update jersey-client explicit version to 2.43
4. Verify jersey-media-moxy compatibility

Code impact: Jersey 2.43 maintains backward compatibility with 2.25.1 APIs. No source code changes expected.

**2.2 Update Swagger to OpenAPI 3.x**

Swagger 1.5.0 uses the older Swagger 2.0 specification. Modern APIs should use OpenAPI 3.x.

Migration steps:
1. Replace swagger-jersey2-jaxrs (1.5.0) with swagger-jaxrs2-jakarta (2.2.21)
2. Update web.xml Swagger servlet configuration
3. Update any @Api annotations to @Tag annotations if present

Code impact: 
- web.xml needs Swagger servlet configuration update
- Source files using @Api, @ApiOperation annotations need updates to @Tag, @Operation

### Phase 3: Build and Test Infrastructure (Medium Priority)

**3.1 Update Maven Compiler Plugin and Java Version**

Migration steps:
1. Update maven-compiler-plugin to 3.12.1
2. Change source and target from 1.8 to 17
3. Add release configuration for Java 17

Code impact: Java 8 to 17 migration may require:
- Review of deprecated APIs
- No major code changes expected for this codebase

**3.2 Update JUnit to JUnit 5**

JUnit 4.12 should be upgraded to JUnit 5 (Jupiter) for modern testing capabilities.

Migration steps:
1. Replace junit:junit:4.12 with org.junit.jupiter:junit-jupiter:5.10.2
2. Add junit-vintage-engine for backward compatibility during transition
3. Update test source files

Code changes required in test files:
- Change `import org.junit.Test` to `import org.junit.jupiter.api.Test`
- Change `import org.junit.Before` to `import org.junit.jupiter.api.BeforeEach`
- Change `import org.junit.After` to `import org.junit.jupiter.api.AfterEach`
- Change `import org.junit.Ignore` to `import org.junit.jupiter.api.Disabled`
- Change `import static org.junit.Assert.*` to `import static org.junit.jupiter.api.Assertions.*`
- Change `@RunWith(Parameterized.class)` to `@ParameterizedTest` with `@MethodSource`
- Update assertion method signatures (parameter order changes)

### Phase 4: Security Hardening (Low Priority)

**4.1 Update Repository URLs to HTTPS**

Migration steps:
1. Change all http:// URLs to https:// in repository configurations
2. Verify FAA Artifactory supports HTTPS

**4.2 Update Servlet Specification**

The web.xml uses Servlet 2.5 specification. This should be updated to Servlet 4.0+ for Java 17 compatibility.

Migration steps:
1. Update web.xml schema to Servlet 4.0
2. Update namespace declarations

## Breaking Changes and Mitigations

### Log4j Migration
- **Breaking Change**: Log4j 1.x API removal
- **Mitigation**: Add log4j-1.2-api bridge for transitive dependencies

### Swagger to OpenAPI 3.x
- **Breaking Change**: Annotation changes (@Api -> @Tag, @ApiOperation -> @Operation)
- **Mitigation**: Update annotations in source files

### JUnit 4 to JUnit 5
- **Breaking Change**: Different package structure and annotations
- **Mitigation**: Add junit-vintage-engine for gradual migration, update test files

### Java 8 to Java 17
- **Breaking Change**: Some deprecated APIs removed
- **Mitigation**: Review and update any deprecated API usage

## Rollback Plan

If issues are encountered during deployment:

1. Revert to the previous pom.xml version
2. Rebuild with original dependencies
3. Redeploy the previous WAR file

## Testing Strategy

1. **Unit Tests**: Run all existing JUnit tests after migration
2. **Integration Tests**: Verify API endpoints return expected responses
3. **Smoke Tests**: Deploy to staging and verify basic functionality
4. **Security Scan**: Run dependency vulnerability scan to confirm CVE resolution

## Timeline Estimate

- Phase 1 (Security-Critical): Immediate
- Phase 2 (Framework Updates): 1-2 days
- Phase 3 (Build Infrastructure): 1 day
- Phase 4 (Security Hardening): 1 day

Total estimated effort: 3-5 days

## Appendix: Detailed Dependency Changes

### pom.xml Changes Summary

```xml
<!-- REMOVE -->
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.12</version>
</dependency>

<!-- UPDATE Log4j 2.x -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.23.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j2-impl</artifactId>
    <version>2.23.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-web</artifactId>
    <version>2.23.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-1.2-api</artifactId>
    <version>2.23.1</version>
</dependency>

<!-- UPDATE Jackson -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.17.2</version>
</dependency>

<!-- UPDATE Jersey -->
<properties>
    <jersey.version>2.43</jersey.version>
</properties>

<!-- UPDATE JUnit -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>

<!-- UPDATE Swagger to OpenAPI 3.x -->
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-jaxrs2</artifactId>
    <version>2.2.21</version>
</dependency>

<!-- UPDATE Maven Compiler Plugin -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.12.1</version>
    <configuration>
        <release>17</release>
    </configuration>
</plugin>
```

### Test File Migration Pattern

For each test file, apply these transformations:

```java
// OLD (JUnit 4)
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

// NEW (JUnit 5)
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
```
