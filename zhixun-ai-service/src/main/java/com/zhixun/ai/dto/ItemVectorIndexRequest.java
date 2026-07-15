package com.zhixun.ai.dto;

public record ItemVectorIndexRequest(
        Long postId,
        String itemType,
        Long categoryId,
        String content,
        String campusArea
) {
}
