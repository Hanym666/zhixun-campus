package com.zhixun.item.dto;

import com.zhixun.common.enums.ItemType;

public record ItemMatchResponse(
        Long postId,
        String postNo,
        ItemType itemType,
        String title,
        String itemName,
        String categoryName,
        String campusArea,
        String locationName,
        double keywordScore,
        double vectorScore,
        double finalScore
) {
}
