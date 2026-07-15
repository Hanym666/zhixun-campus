package com.zhixun.item.mapper;

import com.zhixun.item.domain.ClaimMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClaimMessageMapper {
    int insert(ClaimMessage message);

    List<ClaimMessage> findByClaimId(@Param("claimId") Long claimId,
                                     @Param("beforeId") Long beforeId,
                                     @Param("limit") int limit);
}
