package com.zhixun.item.dto;

import com.zhixun.common.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateItemPostRequest(
        @NotNull ItemType itemType,
        @NotBlank @Size(max = 120) String title,
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100) String itemName,
        @NotBlank @Size(max = 5000) String description,
        @Size(max = 2000) String publicFeatures,
        @Size(max = 2000) String privateFeatures,
        @PastOrPresent LocalDateTime occurredAt,
        @Size(max = 100) String campusArea,
        @Size(max = 200) String locationName,
        @Size(max = 50) String color,
        @Size(max = 100) String brand,
        @Size(max = 255) String contactHint,
        Boolean publishNow
) {
    public boolean shouldPublishNow() {
        return Boolean.TRUE.equals(publishNow);
    }
}
