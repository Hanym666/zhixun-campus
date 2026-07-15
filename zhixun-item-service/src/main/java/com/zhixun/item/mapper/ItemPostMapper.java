package com.zhixun.item.mapper;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.item.domain.ItemPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface ItemPostMapper {
    int insert(ItemPost post);

    Optional<ItemPost> findById(@Param("id") Long id);

    List<ItemPost> findPage(@Param("itemType") ItemType itemType,
                            @Param("categoryId") Long categoryId,
                            @Param("keyword") String keyword,
                            @Param("status") ItemStatus status,
                            @Param("publisherId") Long publisherId,
                            @Param("offset") long offset,
                            @Param("size") int size);

    long countPage(@Param("itemType") ItemType itemType,
                   @Param("categoryId") Long categoryId,
                   @Param("keyword") String keyword,
                   @Param("status") ItemStatus status,
                   @Param("publisherId") Long publisherId);

    int updateOwned(@Param("post") ItemPost post, @Param("publisherId") Long publisherId);

    int publishOwned(@Param("id") Long id, @Param("publisherId") Long publisherId);

    int softDeleteOwned(@Param("id") Long id, @Param("publisherId") Long publisherId);

    int incrementViewCount(@Param("id") Long id);

    int startClaimVerification(@Param("id") Long id);

    int completeClaim(@Param("id") Long id, @Param("publisherId") Long publisherId);
}
