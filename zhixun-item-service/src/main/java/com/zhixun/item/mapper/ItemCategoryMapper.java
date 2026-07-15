package com.zhixun.item.mapper;

import com.zhixun.item.domain.ItemCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemCategoryMapper {
    List<ItemCategory> findEnabled();

    boolean existsEnabledById(@Param("id") Long id);
}
