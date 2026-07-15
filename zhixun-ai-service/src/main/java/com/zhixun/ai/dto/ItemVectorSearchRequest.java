package com.zhixun.ai.dto;

public record ItemVectorSearchRequest(
        Long postId,
        String targetItemType,
        String content,
        Integer topK
) {
    public int resolvedTopK() {
        return topK == null ? 10 : Math.min(Math.max(topK, 1), 50);
    }
}
