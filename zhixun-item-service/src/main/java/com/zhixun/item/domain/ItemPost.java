package com.zhixun.item.domain;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.model.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ItemPost extends BaseEntity {
    private String postNo;
    private Long publisherId;
    private ItemType itemType;
    private ItemStatus status;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String itemName;
    private String description;
    private String publicFeatures;
    private String privateFeatures;
    private LocalDateTime occurredAt;
    private String campusArea;
    private String locationName;
    private String color;
    private String brand;
    private String contactHint;
    private Long viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime completedAt;
}
