package com.backlogr.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            request.abortWith(Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        String origin = request.getHeaderString("Origin");
        if (origin != null) {
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type,Authorization");
            response.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
            response.getHeaders().putSingle("Access-Control-Max-Age", "86400");
        }
    }
}
