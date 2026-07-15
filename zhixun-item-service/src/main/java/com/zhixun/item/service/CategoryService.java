package com.zhixun.item.service;

import com.zhixun.item.dto.CategoryResponse;
import com.zhixun.item.mapper.ItemCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ItemCategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listEnabled() {
        return categoryMapper.findEnabled().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getCode(),
                        category.getName(), category.getParentId(), category.getSortOrder()))
                .toList();
    }
}
