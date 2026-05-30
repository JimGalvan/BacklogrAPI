package com.backlogr.common.exception;

import com.backlogr.common.ErrorResponse;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception e) {
        Throwable root = rootCause(e);

        if (root instanceof UnauthorizedException) {
            return Response.status(401)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(401, "Missing or invalid authentication token."))
                    .build();
        }

        if (root instanceof ForbiddenException) {
            return Response.status(403)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(403, "Authenticated but not authorised to perform this action."))
                    .build();
        }

        LOG.errorf(root, "Unhandled exception: %s", root.getMessage());
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(500, "Unexpected server error."))
                .build();
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
