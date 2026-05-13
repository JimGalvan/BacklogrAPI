package com.backlogr.shared;

public final class Result<T> {

    private final T value;
    private final int status;
    private final String message;

    private Result(T value, int status, String message) {
        this.value = value;
        this.status = status;
        this.message = message;
    }

    // --- success factories ---

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, 200, null);
    }

    public static <T> Result<T> created(T value) {
        return new Result<>(value, 201, null);
    }

    // --- error factories ---

    public static <T> Result<T> badRequest(String message) {
        return new Result<>(null, 400, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(null, 401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return new Result<>(null, 403, message);
    }

    public static <T> Result<T> notFound(String message) {
        return new Result<>(null, 404, message);
    }

    public static <T> Result<T> conflict(String message) {
        return new Result<>(null, 409, message);
    }

    public static <T> Result<T> unprocessableEntity(String message) {
        return new Result<>(null, 422, message);
    }

    public static <T> Result<T> internalError(String message) {
        return new Result<>(null, 500, message);
    }

    // --- accessors ---

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public T getValue() {
        return value;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
