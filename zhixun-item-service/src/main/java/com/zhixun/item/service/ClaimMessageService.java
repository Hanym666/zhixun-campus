package com.zhixun.item.service;

import com.zhixun.common.enums.ClaimStatus;
import com.zhixun.common.enums.NotificationType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ClaimMessage;
import com.zhixun.item.domain.ClaimRequest;
import com.zhixun.item.dto.ClaimMessageResponse;
import com.zhixun.item.dto.SendClaimMessageRequest;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.mapper.ClaimMessageMapper;
import com.zhixun.item.mapper.ClaimRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClaimMessageService {

    private static final Set<ClaimStatus> WRITABLE_STATUSES = Set.of(
            ClaimStatus.PENDING, ClaimStatus.APPROVED, ClaimStatus.HANDING_OVER);

    private final ClaimMessageMapper messageMapper;
    private final ClaimRequestMapper claimMapper;
    private final NotificationService notificationService;

    @Transactional
    public ClaimMessageResponse send(Long claimId, Long senderId, SendClaimMessageRequest request) {
        ClaimRequest claim = requireParticipant(claimId, senderId);
        if (!WRITABLE_STATUSES.contains(claim.getStatus())) {
            throw new BusinessException(ItemResultCode.CLAIM_STATUS_INVALID);
        }
        ClaimMessage message = new ClaimMessage();
        message.setClaimId(claimId);
        message.setSenderId(senderId);
        message.setContent(request.content().trim());
        messageMapper.insert(message);

        Long recipientId = senderId.equals(claim.getClaimantId())
                ? claim.getPostPublisherId() : claim.getClaimantId();
        notificationService.send(recipientId, NotificationType.CLAIM_MESSAGE,
                "收到新的认领消息", "“" + claim.getPostTitle() + "”有一条新的沟通消息",
                "CLAIM", claimId);
        return toResponse(message);
    }

    @Transactional(readOnly = true)
    public List<ClaimMessageResponse> list(Long claimId, Long userId, Long beforeId, int limit) {
        requireParticipant(claimId, userId);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<ClaimMessageResponse> messages = new ArrayList<>(messageMapper
                .findByClaimId(claimId, beforeId, safeLimit).stream()
                .map(this::toResponse).toList());
        Collections.reverse(messages);
        return List.copyOf(messages);
    }

    private ClaimRequest requireParticipant(Long claimId, Long userId) {
        ClaimRequest claim = claimMapper.findById(claimId)
                .orElseThrow(() -> new BusinessException(ItemResultCode.CLAIM_NOT_FOUND));
        if (!userId.equals(claim.getClaimantId())
                && !userId.equals(claim.getPostPublisherId())) {
            throw new BusinessException(ItemResultCode.CLAIM_OPERATION_FORBIDDEN);
        }
        return claim;
    }

    private ClaimMessageResponse toResponse(ClaimMessage message) {
        return new ClaimMessageResponse(message.getId(), message.getClaimId(),
                message.getSenderId(), message.getContent(), message.getCreatedAt());
    }
}
