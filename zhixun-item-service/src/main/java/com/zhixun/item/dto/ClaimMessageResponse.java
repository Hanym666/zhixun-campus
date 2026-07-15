package com.zhixun.item.dto;

import java.time.LocalDateTime;

public record ClaimMessageResponse(
        Long id,
        Long claimId,
        Long senderId,
        String content,
        LocalDateTime createdAt
) {
}
