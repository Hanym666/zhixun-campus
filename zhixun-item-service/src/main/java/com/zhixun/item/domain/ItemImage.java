package com.zhixun.item.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemImage {
    private Long id;
    private Long postId;
    private String imageUrl;
    private Integer sortOrder;
    private Boolean coverImage;
    private LocalDateTime createdAt;
}
