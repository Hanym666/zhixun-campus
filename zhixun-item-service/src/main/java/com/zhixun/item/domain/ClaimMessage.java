package com.zhixun.item.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClaimMessage {
    private Long id;
    private Long claimId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
