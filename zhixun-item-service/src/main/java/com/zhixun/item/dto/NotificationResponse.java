package com.zhixun.item.dto;

import com.zhixun.common.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        String businessType,
        Long businessId,
        Boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
