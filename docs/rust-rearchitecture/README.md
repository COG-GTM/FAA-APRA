# FAA APRA Rust Re-architecture Proposal

This directory contains the comprehensive proposal for re-architecting the FAA Aeronautical Product Release API (APRA) from Java 1.8/Jersey to a modern Rust-based architecture.

## Overview

The FAA APRA system is currently a monolithic Java 1.8 application using Jersey JAX-RS framework, deployed as a WAR file. This proposal outlines a migration to Rust using Axum/Tokio, offering improved performance, memory safety, and reduced operational costs.

## Contents

### Main Proposal

- **[proposal.html](proposal.html)** - Complete PRD with embedded diagrams, technology comparison, and implementation plan. Self-contained HTML file that works offline.

### Architecture Diagrams

Located in the `diagrams/` directory:

- **[high-level-architecture.svg](diagrams/high-level-architecture.svg)** - System architecture showing client layer, API gateway, microservices, and data layer
- **[service-decomposition.svg](diagrams/service-decomposition.svg)** - Service breakdown and dependencies
- **[deployment-architecture.svg](diagrams/deployment-architecture.svg)** - Kubernetes deployment topology
- **[data-flow.svg](diagrams/data-flow.svg)** - Request/response data flow

### Implementation Plans

Located in the `plans/` directory:

- **[implementation-plan.md](plans/implementation-plan.md)** - Detailed JIRA-style implementation plan with 5 phases and 19 stories

## Key Benefits of Rust Migration

| Benefit | Description |
|---------|-------------|
| **Memory Safety** | Eliminates entire classes of memory errors without garbage collection |
| **Performance** | 2-32x lower latency, 60-80% smaller memory footprint |
| **Security** | Minimal attack surface, modern dependencies, STIG-compliant |
| **Cost Reduction** | 30-50% infrastructure cost reduction |
| **Container Size** | <30MB images vs >300MB for Java |

## Proposed Technology Stack

| Category | Current | Proposed |
|----------|---------|----------|
| Language | Java 1.8 | Rust 1.75+ |
| Framework | Jersey 2.25.1 | Axum (Tokio) |
| Build | Maven | Cargo |
| Logging | Log4j 1.2.12 | tracing + JSON |
| API Docs | Swagger 1.5.0 | utoipa (OpenAPI 3) |
| Serialization | MOXy/JAXB | Serde + quick-xml |
| Deployment | WAR | Container (distroless) |

## Implementation Timeline

The migration is planned across 6 sprints (12 weeks):

1. **Phase 1** (S1-S2): Foundation setup - Cargo workspace, base crates, CI/CD
2. **Phase 2** (S2-S3): Core services migration - IFR, VFR, CIFP, DDOF
3. **Phase 3** (S4): API compatibility layer - Content negotiation, OpenAPI
4. **Phase 4** (S5): Production readiness - Security hardening, scaling
5. **Phase 5** (S6): Migration and cutover - Shadow traffic, route cutover

## Related Documents

- [Current APRA README](../../README.md)
- [SwaggerHub API Documentation](https://app.swaggerhub.com/apis/FAA/APRA)

## Contact

For questions about this proposal, please contact the development team.
