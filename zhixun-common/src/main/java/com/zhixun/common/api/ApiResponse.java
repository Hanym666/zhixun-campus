package com.zhixun.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int code,
        String message,
        T data,
        long timestamp
) {

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                CommonResultCode.SUCCESS.code(),
                CommonResultCode.SUCCESS.message(),
                data,
                System.currentTimeMillis()
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                CommonResultCode.SUCCESS.code(),
                message,
                data,
                System.currentTimeMillis()
        );
    }

    public static ApiResponse<Void> failure(ResultCode resultCode) {
        return failure(resultCode, resultCode.message());
    }

    public static ApiResponse<Void> failure(ResultCode resultCode, String message) {
        return failure(resultCode.code(), message);
    }

    public static ApiResponse<Void> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }
}
