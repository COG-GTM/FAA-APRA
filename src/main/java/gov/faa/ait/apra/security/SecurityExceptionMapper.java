/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STIG V-220641: Generic error handling to prevent information leakage.
 * Maps unhandled exceptions to generic error responses while logging
 * full details internally per NIST SI-11.
 * Preserves JAX-RS WebApplicationException responses (404, 400, etc.)
 * so that standard HTTP status codes are not swallowed as 500s.
 */
@Provider
public class SecurityExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        // Preserve JAX-RS WebApplicationException responses (404, 400, etc.)
        if (exception instanceof WebApplicationException) {
            Response original = ((WebApplicationException) exception).getResponse();
            int status = original.getStatus();
            logger.warn("JAX-RS exception: {} (status {})", exception.getMessage(), status);
            return Response.status(status)
                .entity("{\"status\":{\"code\":" + status + ",\"message\":\"" + original.getStatusInfo().getReasonPhrase() + "\"}}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }

        logger.error("Unhandled exception in request processing", exception);

        AuditLogger.log("unhandled_exception", "unknown", "unknown", "unknown",
            "failure", exception.getClass().getSimpleName());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("{\"status\":{\"code\":500,\"message\":\"An internal error occurred. Please try again later.\"}}")
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
