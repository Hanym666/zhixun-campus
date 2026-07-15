package com.zhixun.item.domain;

import com.zhixun.common.enums.ClaimStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClaimRequest {
    private Long id;
    private String claimNo;
    private Long postId;
    private String postTitle;
    private Long postPublisherId;
    private Long claimantId;
    private String evidence;
    private ClaimStatus status;
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private String handoverLocation;
    private LocalDateTime handoverAt;
    private LocalDateTime completedAt;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
