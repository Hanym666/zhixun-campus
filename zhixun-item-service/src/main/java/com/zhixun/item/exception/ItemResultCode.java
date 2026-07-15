package com.zhixun.item.exception;

import com.zhixun.common.api.ResultCode;

public enum ItemResultCode implements ResultCode {
    ITEM_NOT_FOUND(40420, "物品信息不存在", 404),
    CATEGORY_NOT_FOUND(40421, "物品分类不存在或已停用", 404),
    ITEM_OPERATION_FORBIDDEN(40320, "无权操作该物品信息", 403),
    ITEM_STATUS_INVALID(40920, "当前状态不允许执行该操作", 409),
    CLAIM_NOT_FOUND(40422, "认领申请不存在", 404),
    CLAIM_DUPLICATED(40921, "已经提交过有效的认领申请", 409),
    CLAIM_OWN_POST_FORBIDDEN(40922, "不能认领自己发布的物品", 409),
    CLAIM_OPERATION_FORBIDDEN(40321, "无权操作该认领申请", 403),
    CLAIM_STATUS_INVALID(40923, "认领申请当前状态不允许执行该操作", 409),
    IMAGE_NOT_FOUND(40423, "物品图片不存在", 404),
    IMAGE_FORMAT_INVALID(40020, "只支持有效的 JPG 或 PNG 图片", 400),
    IMAGE_LIMIT_EXCEEDED(40924, "每条物品信息最多上传 6 张图片", 409);

    private final int code;
    private final String message;
    private final int httpStatus;

    ItemResultCode(int code, String message, int httpStatus) {
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
