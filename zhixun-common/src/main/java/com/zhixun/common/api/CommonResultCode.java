package com.zhixun.common.api;

public enum CommonResultCode implements ResultCode {

    SUCCESS(0, "成功", 200),
    BAD_REQUEST(40000, "请求参数错误", 400),
    VALIDATION_ERROR(40001, "参数校验失败", 400),
    UNAUTHORIZED(40100, "请先登录", 401),
    FORBIDDEN(40300, "无权访问", 403),
    NOT_FOUND(40400, "资源不存在", 404),
    CONFLICT(40900, "资源状态冲突", 409),
    TOO_MANY_REQUESTS(42900, "请求过于频繁", 429),
    INTERNAL_ERROR(50000, "系统内部错误", 500),
    SERVICE_UNAVAILABLE(50300, "服务暂时不可用", 503);

    private final int code;
    private final String message;
    private final int httpStatus;

    CommonResultCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}
