package com.backlogr.exception;

import com.backlogr.shared.ErrorResponse;
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
        if (e instanceof UnauthorizedException) {
            return Response.status(401)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(401, "Missing or invalid authentication token."))
                    .build();
        }

        if (e instanceof ForbiddenException) {
            return Response.status(403)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(403, "Authenticated but not authorised to perform this action."))
                    .build();
        }

        LOG.errorf(e, "Unhandled exception: %s", e.getMessage());
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(500, "Unexpected server error."))
                .build();
    }
}
