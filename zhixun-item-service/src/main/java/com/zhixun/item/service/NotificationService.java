package com.zhixun.item.service;

import com.zhixun.common.api.PageResult;
import com.zhixun.common.enums.NotificationType;
import com.zhixun.item.domain.Notification;
import com.zhixun.item.dto.NotificationResponse;
import com.zhixun.item.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    @Transactional
    public void send(Long userId, NotificationType type, String title, String content,
                     String businessType, Long businessId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notificationMapper.insert(notification);
    }

    @Transactional(readOnly = true)
    public PageResult<NotificationResponse> list(Long userId, boolean unreadOnly,
                                                 int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        long total = notificationMapper.countPage(userId, unreadOnly);
        var records = notificationMapper.findPage(userId, unreadOnly,
                        (long) (safePage - 1) * safeSize, safeSize)
                .stream().map(this::toResponse).toList();
        return PageResult.of(records, total, safePage, safeSize);
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        notificationMapper.markRead(id, userId);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(),
                notification.getTitle(), notification.getContent(), notification.getBusinessType(),
                notification.getBusinessId(), notification.getReadFlag(), notification.getReadAt(),
                notification.getCreatedAt());
    }
}
