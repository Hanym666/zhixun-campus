package com.zhixun.item.dto;

public record CategoryResponse(Long id, String code, String name, Long parentId, Integer sortOrder) {
}
