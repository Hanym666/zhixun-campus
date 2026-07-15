package com.zhixun.item.mapper;

import com.zhixun.item.domain.ItemImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface ItemImageMapper {
    int insert(ItemImage image);

    List<ItemImage> findByPostId(@Param("postId") Long postId);

    Optional<ItemImage> findById(@Param("id") Long id);

    int countByPostId(@Param("postId") Long postId);

    int deleteById(@Param("id") Long id);

    int promoteFirstToCover(@Param("postId") Long postId);
}
