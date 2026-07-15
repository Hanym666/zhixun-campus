package com.zhixun.item.dto;

public record ItemImageResponse(
        Long id,
        String imageUrl,
        Integer sortOrder,
        Boolean coverImage
) {
}
