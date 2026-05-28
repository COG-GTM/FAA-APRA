/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STIG V-220641 (NIST SI-11): Generic error handling.
 * Returns generic error messages to clients and logs detailed information
 * internally. Prevents information leakage through stack traces or internal details.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("Unhandled exception intercepted by global handler", exception);

        AuditLogger.getInstance().log(
            "security_violation", "anonymous", "unknown",
            "unhandled_exception", "failure",
            exception.getClass().getSimpleName());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("{\"status\":{\"code\":500,\"message\":\"An internal error occurred. Please try again later.\"}}")
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
