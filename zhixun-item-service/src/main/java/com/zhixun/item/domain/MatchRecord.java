package com.zhixun.item.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MatchRecord {
    private Long id;
    private Long sourcePostId;
    private Long targetPostId;
    private BigDecimal keywordScore;
    private BigDecimal vectorScore;
    private BigDecimal finalScore;
    private Boolean notified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
