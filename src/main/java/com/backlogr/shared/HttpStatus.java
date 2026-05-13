package com.backlogr.shared;

public final class HttpStatus {

    public static final String OK                    = "200";
    public static final String CREATED               = "201";
    public static final String NO_CONTENT            = "204";
    public static final String BAD_REQUEST           = "400";
    public static final String UNAUTHORIZED          = "401";
    public static final String FORBIDDEN             = "403";
    public static final String NOT_FOUND             = "404";
    public static final String CONFLICT              = "409";
    public static final String UNPROCESSABLE_ENTITY  = "422";
    public static final String INTERNAL_SERVER_ERROR = "500";

    private HttpStatus() {}

    public static final class Description {

        public static final String OK                    = "Request completed successfully";
        public static final String CREATED               = "Resource created successfully";
        public static final String NO_CONTENT            = "Request completed, no content to return";
        public static final String BAD_REQUEST           = "Malformed or structurally invalid request body";
        public static final String UNAUTHORIZED          = "Missing or invalid authentication token";
        public static final String FORBIDDEN             = "Authenticated but not authorised to perform this action";
        public static final String NOT_FOUND             = "Requested resource not found";
        public static final String CONFLICT              = "Resource already exists or state conflict";
        public static final String UNPROCESSABLE_ENTITY  = "Request body failed validation";
        public static final String INTERNAL_SERVER_ERROR = "Unexpected server error";

        private Description() {}
    }
}
