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
 * STIG V-220641 (NIST SI-11): Generic error handling.
 * Returns generic error messages to clients and logs detailed information
 * internally. Prevents information leakage through stack traces or internal details.
 * Preserves HTTP status codes for WebApplicationException (4xx errors) while
 * mapping unexpected exceptions to 500.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            int status = wae.getResponse().getStatus();
            logger.warn("HTTP error {} intercepted: {}", status, exception.getMessage());
            return Response.status(status)
                .entity("{\"status\":{\"code\":" + status + ",\"message\":\"" + genericMessage(status) + "\"}}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }

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

    private static String genericMessage(int status) {
        switch (status) {
            case 400: return "Bad request";
            case 401: return "Authentication required";
            case 403: return "Access denied";
            case 404: return "Resource not found";
            case 405: return "Method not allowed";
            case 415: return "Unsupported media type";
            default:
                if (status >= 400 && status < 500) {
                    return "Client error";
                }
                return "An internal error occurred. Please try again later.";
        }
    }
}
