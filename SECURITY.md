# APRA Security Implementation

This document describes the security controls implemented in the Aeronautic Product Release API (APRA) to comply with STIG (Security Technical Implementation Guides) and NIST 800-53 requirements for federal information systems.

## Overview

The security implementation provides defense-in-depth through multiple layers of protection:

1. Input Validation and Sanitization (STIG V-220631/V-220632)
2. Authentication and Session Management (STIG V-220629/V-220630)
3. Rate Limiting (STIG V-220636)
4. Security Headers (STIG V-220641)
5. Audit Logging (STIG V-220635)
6. Secure Dependencies (STIG V-220633)

## Security Components

### Input Validation (STIG V-220631)

The `InputValidator` class provides whitelist-based validation for all API parameters:

- Validates geoname, edition, and format parameters against allowed patterns
- Enforces maximum input lengths
- Detects and rejects dangerous characters

The `InputValidationFilter` automatically validates all incoming requests before they reach the API endpoints.

### Input Sanitization (STIG V-220632)

The `InputSanitizer` class removes dangerous characters to prevent injection attacks:

- Removes characters: `< > " ' ; | & $ ( ) \ \``
- Strips control characters
- Normalizes whitespace
- Provides HTML encoding for output

### Authentication (STIG V-220629)

The `AuthenticationFilter` implements API key authentication:

- API key validation via SHA-256 hash comparison
- Constant-time comparison to prevent timing attacks
- Account lockout after 5 failed attempts (configurable)
- 15-minute lockout duration (configurable)

Note: Authentication is disabled by default for backward compatibility. Enable via `security.auth.enabled=true`.

### Rate Limiting (STIG V-220636)

The `RateLimiter` and `RateLimitFilter` prevent abuse:

- Sliding window algorithm
- Default: 100 requests per 60 seconds per IP
- Configurable exempt IP list
- Returns 429 status with retry information

### Security Headers (STIG V-220641)

The `SecurityHeadersFilter` adds required security headers to all responses:

- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 1; mode=block`
- `Content-Security-Policy: default-src 'self'; frame-ancestors 'none'`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Cache-Control: no-store, no-cache, must-revalidate`

### Audit Logging (STIG V-220635)

The `AuditLogger` and `AuditFilter` provide comprehensive audit logging:

- JSON-formatted log entries
- SHA-256 integrity hashes for tamper detection
- Logs all API requests and responses
- Logs security events (rate limiting, validation failures, etc.)
- Separate audit and security log streams

## Configuration

Security settings are configured in `/opt/apra/conf/security.properties` (or `src/main/resources/security.properties` for development).

### Rate Limiting

```properties
security.ratelimit.enabled=true
security.ratelimit.max.requests=100
security.ratelimit.window.seconds=60
security.ratelimit.exempt.ips=
```

### Security Headers

```properties
security.headers.enabled=true
security.headers.csp=default-src 'self'; frame-ancestors 'none'; form-action 'self'
```

### Audit Logging

```properties
security.audit.enabled=true
security.audit.log.directory=/var/log/apra
security.audit.app.name=faa-apra
```

### Authentication

```properties
security.auth.enabled=false
security.auth.apikey.header=X-API-Key
security.auth.max.failed.attempts=5
security.auth.lockout.duration.minutes=15
```

## Enabling Authentication

To enable API key authentication:

1. Generate a secure API key (minimum 32 characters)
2. Hash the API key using SHA-256
3. Set the environment variable: `APRA_API_KEY_HASH=<sha256_hash>`
4. Set `security.auth.enabled=true` in security.properties
5. Clients must include the API key in the `X-API-Key` header

## STIG Compliance Matrix

| STIG ID | NIST Control | Implementation |
|---------|--------------|----------------|
| V-220629 | IA-2, IA-5, AC-7 | AuthenticationFilter, account lockout |
| V-220630 | AC-12 | Session timeout configuration |
| V-220631 | SI-10 | InputValidator, InputValidationFilter |
| V-220632 | SI-10 | InputSanitizer, parameterized queries |
| V-220633 | SC-28 | Updated Log4j dependencies |
| V-220635 | AU-2, AU-3 | AuditLogger, AuditFilter |
| V-220636 | - | RateLimiter, RateLimitFilter |
| V-220641 | SI-11 | SecurityHeadersFilter, generic errors |

## Dependency Updates

The following dependencies have been updated to address known vulnerabilities:

- Log4j: Updated to 2.21.1 (addresses CVE-2021-44228 and related vulnerabilities)
- Removed legacy log4j 1.x dependency, replaced with log4j-1.2-api bridge

## Error Handling

All error responses return generic messages to prevent information disclosure:

- `{"error":"Invalid input: parameter contains illegal characters"}`
- `{"error":"Too many requests. Please try again later."}`
- `{"error":"Authentication required"}`

Detailed error information is logged internally for debugging.

## Security Best Practices

1. Always deploy behind HTTPS (TLS 1.2+)
2. Configure a reverse proxy for additional protection
3. Monitor audit logs for security events
4. Regularly review and update security configuration
5. Keep dependencies updated for security patches
