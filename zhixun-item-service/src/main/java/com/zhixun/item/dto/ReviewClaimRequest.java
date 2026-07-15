package com.zhixun.item.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReviewClaimRequest(
        @NotNull Boolean approved,
        @Size(max = 500) String reviewComment,
        @Size(max = 255) String handoverLocation,
        @FutureOrPresent LocalDateTime handoverAt
) {
}
