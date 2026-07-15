package com.zhixun.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendClaimMessageRequest(
        @NotBlank @Size(max = 1000) String content
) {
}
