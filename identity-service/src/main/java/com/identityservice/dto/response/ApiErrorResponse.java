package com.identityservice.dto.response;

public record ApiErrorResponse(
        int status,
        String code,
        String message
) {
    public static ApiErrorResponse of(int status, String code, String message) {
        return new ApiErrorResponse(status, code, message);
    }
}
