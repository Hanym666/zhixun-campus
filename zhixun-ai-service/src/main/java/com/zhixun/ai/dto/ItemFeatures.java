package com.zhixun.ai.dto;

import java.util.List;

public record ItemFeatures(String normalizedText, List<String> keywords) {
    public ItemFeatures {
        keywords = keywords == null ? List.of() : List.copyOf(keywords);
    }
}
