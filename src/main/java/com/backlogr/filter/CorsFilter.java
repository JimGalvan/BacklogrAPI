package com.backlogr.filter;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter {

    @Inject
    RoutingContext routingContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String origin = routingContext.request().getHeader("Origin");

        if (origin != null) {
            // Set headers at the Vert.x layer so they are present for all response
            // types — including SSE streams where the JAX-RS response filter fires
            // too late (after headers are already flushed).
            routingContext.response().putHeader("Access-Control-Allow-Origin",      origin);
            routingContext.response().putHeader("Access-Control-Allow-Methods",     "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            routingContext.response().putHeader("Access-Control-Allow-Headers",     "Content-Type,Authorization");
            routingContext.response().putHeader("Access-Control-Allow-Credentials", "true");
            routingContext.response().putHeader("Access-Control-Max-Age",           "86400");
        }

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(Response.ok().build());
        }
    }
}
