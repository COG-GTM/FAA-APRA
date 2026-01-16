# FAA APRA Rust Re-architecture Implementation Plan

This document outlines the phased implementation plan for migrating the FAA Aeronautical Product Release API (APRA) from Java 1.8/Jersey to a modern Rust-based architecture.

## Overview

The implementation is organized into 5 phases with 19 stories, aligned to 2-week sprints. Priority key: P0 (must), P1 (high), P2 (normal). Story points use Fibonacci scale.

## Phase 1: Foundation Setup (Sprints 1-2)

**Epic UF-APRA-RUST-EPIC**: Establish Rust baseline, CI, common crates, and compatibility gateway.

| ID | Summary | Priority | Points | Sprint | Description |
|----|---------|----------|--------|--------|-------------|
| UF-1 | Create Cargo workspace & base crates (apra-model, apra-config, apra-security) | P0 | 5 | S1 | Define response types (ProductSet, Edition, Product) with Serde + quick-xml, centralize config and security headers. |
| UF-2 | Axum service template with tracing, metrics, error handling | P0 | 5 | S1 | Set up tracing, JSON logging, structured errors (thiserror), health/readiness endpoints. |
| UF-3 | Cycle Service MVP with Denodo adapter and in-memory cache | P0 | 8 | S1 | Implement 28/56-day calls, TTL cache, proxy support, timeouts, backoff. |
| UF-4 | CI/CD pipeline and security scanning | P0 | 3 | S1 | cargo fmt/clippy, cargo-audit, cargo-deny; container build with Trivy scan. |
| UF-5 | Compatibility Gateway router in Axum | P1 | 5 | S2 | Replicate legacy paths; forward to Cycle Service stubs to verify routing + headers. |

**Phase 1 Total**: 26 story points

## Phase 2: Core Services Migration (Sprints 2-3)

| ID | Summary | Priority | Points | Sprint | Description |
|----|---------|----------|--------|--------|-------------|
| UF-6 | IFR/Enroute Service (low/high/area, oceanic) URL builders | P0 | 8 | S2 | Implement edition+format validation, HEAD check toggle, parity tests vs Java outputs. |
| UF-7 | VFR Sectional/TAC/Helicopter Service | P0 | 8 | S3 | Repository interface for table lookups; Denodo adapter; caching geoname tables. |
| UF-8 | CIFP & NASR Service | P1 | 5 | S3 | 28-day cadence URL builders; integration with Cycle Service. |
| UF-9 | DDOF Service with daily changeset support | P1 | 5 | S3 | tod.faa.gov URL templates, HEAD checks, change-only logic. |

**Phase 2 Total**: 26 story points

## Phase 3: API Compatibility Layer (Sprint 4)

| ID | Summary | Priority | Points | Sprint | Description |
|----|---------|----------|--------|--------|-------------|
| UF-10 | Content negotiation parity (JSON/XML) | P0 | 5 | S4 | Accept/Content-Type handling; XML schema compatibility validation. |
| UF-11 | OpenAPI generation and Swagger UI (dev) | P1 | 3 | S4 | utoipa definitions; publish OpenAPI JSON; gated Swagger UI for non-prod. |
| UF-12 | Backward-compatible error codes/messages | P1 | 3 | S4 | Mirror BaseService error semantics (400/404/500), configurable messages. |

**Phase 3 Total**: 11 story points

## Phase 4: Production Readiness (Sprint 5)

| ID | Summary | Priority | Points | Sprint | Description |
|----|---------|----------|--------|--------|-------------|
| UF-13 | Security hardening & STIG controls | P0 | 5 | S5 | Headers, TLS, read-only FS, non-root, secrets mgmt, audit logs. |
| UF-14 | Scaling, HPA, timeouts, circuit breakers | P1 | 5 | S5 | tower layers for retries/backoff; gateway timeouts; load testing. |
| UF-15 | Canary + blue/green rollout | P1 | 3 | S5 | Progressive delivery with metrics guardrails; rollback procedures. |
| UF-16 | SRE runbook & dashboards | P1 | 3 | S5 | Alerting policies, log fields, dashboards (latency, err rate, HEAD failures). |

**Phase 4 Total**: 16 story points

## Phase 5: Migration and Cutover (Sprint 6)

| ID | Summary | Priority | Points | Sprint | Description |
|----|---------|----------|--------|--------|-------------|
| UF-17 | Shadow traffic + parity checks | P0 | 5 | S6 | Mirror production requests to Rust; validate response parity and latency. |
| UF-18 | Incremental route cutover via gateway | P0 | 3 | S6 | Gradually route chart families to Rust; monitor metrics. |
| UF-19 | Finalize deprecation of monolith paths | P1 | 3 | S6 | Announce dates, update docs, retain compatibility shim as needed. |

**Phase 5 Total**: 11 story points

## Summary

| Phase | Description | Story Points | Sprints |
|-------|-------------|--------------|---------|
| 1 | Foundation Setup | 26 | S1-S2 |
| 2 | Core Services Migration | 26 | S2-S3 |
| 3 | API Compatibility Layer | 11 | S4 |
| 4 | Production Readiness | 16 | S5 |
| 5 | Migration and Cutover | 11 | S6 |
| **Total** | | **90** | **6 sprints (12 weeks)** |

## Success Criteria

### Operational Targets

| Dimension | Baseline (Java/Jersey) | Target (Rust) | Measurement |
|-----------|------------------------|---------------|-------------|
| Latency (p50/p95) | p50: ~30-60ms; p95: ~120-200ms | p50: <30ms; p95: <100ms | Tracing spans; load tests |
| Memory (per pod) | 300-500MB | 100-200MB | Container metrics |
| Availability | 99.5%-99.9% | 99.9% | SLO w/ error budgets |
| Deploy time | Minutes | <30s rollout | CI/CD timestamps |
| Image size | >300MB | <30MB | Registry metadata |
| Cost | Reference | 30%-50% reduction | Compute and egress |

### Quality Gates

- All phases must pass contract tests against golden Java responses
- Security scans must show zero criticals
- CI must enforce clippy, fmt, audit, deny
- Shadow traffic error rate delta must be under 0.5% before cutover
- p95 latency must not regress against baseline
