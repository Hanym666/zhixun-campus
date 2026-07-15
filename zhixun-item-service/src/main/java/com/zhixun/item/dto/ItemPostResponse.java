package com.zhixun.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemPostResponse(
        Long id,
        String postNo,
        Long publisherId,
        ItemType itemType,
        ItemStatus status,
        String title,
        Long categoryId,
        String categoryName,
        String itemName,
        String description,
        String publicFeatures,
        String privateFeatures,
        LocalDateTime occurredAt,
        String campusArea,
        String locationName,
        String color,
        String brand,
        String contactHint,
        Long viewCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ItemImageResponse> images
) {
    public ItemPostResponse {
        images = images == null ? List.of() : List.copyOf(images);
    }
}
