package com.zhixun.item.dto;

import com.zhixun.common.enums.ClaimStatus;

import java.time.LocalDateTime;

public record ClaimResponse(
        Long id,
        String claimNo,
        Long postId,
        String postTitle,
        Long postPublisherId,
        Long claimantId,
        String evidence,
        ClaimStatus status,
        Long reviewerId,
        String reviewComment,
        LocalDateTime reviewedAt,
        String handoverLocation,
        LocalDateTime handoverAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
