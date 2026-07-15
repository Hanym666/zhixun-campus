package com.zhixun.common.exception;

import com.zhixun.common.api.CommonResultCode;
import com.zhixun.common.api.ResultCode;

public class BusinessException extends RuntimeException {

    private final int code;
    private final int httpStatus;

    public BusinessException(ResultCode resultCode) {
        this(resultCode, resultCode.message());
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.code();
        this.httpStatus = resultCode.httpStatus();
    }

    public BusinessException(String message) {
        this(CommonResultCode.BAD_REQUEST, message);
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
