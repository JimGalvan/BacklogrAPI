package com.backlogr.exception;

import com.backlogr.shared.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        String message = switch (status) {
            case 400 -> "Bad request.";
            case 401 -> "Missing or invalid authentication token.";
            case 403 -> "Authenticated but not authorised to perform this action.";
            case 404 -> "Requested resource not found.";
            case 405 -> "Method not allowed.";
            default  -> e.getMessage() != null ? e.getMessage() : "Request error.";
        };

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(status, message))
                .build();
    }
}
