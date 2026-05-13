package com.backlogr.controller;

import com.backlogr.shared.ErrorResponse;
import com.backlogr.shared.Result;
import jakarta.ws.rs.core.Response;

public abstract class BaseController {

    protected <T> Response toResponse(Result<T> result) {
        if (result.isSuccess()) {
            return Response.status(result.getStatus()).entity(result.getValue()).build();
        }
        return Response.status(result.getStatus())
                .entity(new ErrorResponse(result.getStatus(), result.getMessage()))
                .build();
    }
}
