package com.zhixun.user.exception;

import com.zhixun.common.api.ResultCode;

public enum UserResultCode implements ResultCode {

    USER_NOT_FOUND(40410, "用户不存在", 404),
    USERNAME_ALREADY_EXISTS(40910, "用户名已存在", 409),
    IDENTITY_NUMBER_ALREADY_EXISTS(40911, "学号或工号已存在", 409),
    EMAIL_ALREADY_EXISTS(40912, "邮箱已被使用", 409),
    INVALID_CREDENTIALS(40110, "用户名或密码错误", 401),
    REFRESH_TOKEN_INVALID(40111, "刷新令牌无效或已过期", 401),
    USER_DISABLED(40310, "账号已被禁用", 403),
    DEFAULT_ROLE_NOT_FOUND(50010, "系统默认角色不存在", 500);

    private final int code;
    private final String message;
    private final int httpStatus;

    UserResultCode(int code, String message, int httpStatus) {
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
