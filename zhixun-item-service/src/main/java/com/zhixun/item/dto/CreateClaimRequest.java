package com.zhixun.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateClaimRequest(
        @NotNull Long postId,
        @NotBlank @Size(min = 5, max = 3000) String evidence
) {
}
