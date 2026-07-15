package com.zhixun.item.domain;

import com.zhixun.common.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    private String businessType;
    private Long businessId;
    private Boolean readFlag;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
