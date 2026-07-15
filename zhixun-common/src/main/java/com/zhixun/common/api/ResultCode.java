package com.zhixun.common.api;

/**
 * Contract implemented by both common and service-specific result codes.
 */
public interface ResultCode {

    int code();

    String message();

    int httpStatus();
}
