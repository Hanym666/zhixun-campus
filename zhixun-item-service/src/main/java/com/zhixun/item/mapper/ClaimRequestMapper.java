package com.zhixun.item.mapper;

import com.zhixun.item.domain.ClaimRequest;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClaimRequestMapper {
    int insert(ClaimRequest claim);

    boolean existsActive(@Param("postId") Long postId, @Param("claimantId") Long claimantId);

    Optional<ClaimRequest> findById(@Param("id") Long id);

    List<ClaimRequest> findPage(@Param("claimantId") Long claimantId,
                                @Param("publisherId") Long publisherId,
                                @Param("offset") long offset,
                                @Param("size") int size);

    long countPage(@Param("claimantId") Long claimantId,
                   @Param("publisherId") Long publisherId);

    int approve(@Param("id") Long id, @Param("reviewerId") Long reviewerId,
                @Param("reviewComment") String reviewComment,
                @Param("handoverLocation") String handoverLocation,
                @Param("handoverAt") LocalDateTime handoverAt);

    int reject(@Param("id") Long id, @Param("reviewerId") Long reviewerId,
               @Param("reviewComment") String reviewComment);

    int rejectOtherPending(@Param("postId") Long postId, @Param("approvedClaimId") Long approvedClaimId,
                           @Param("reviewerId") Long reviewerId);

    int complete(@Param("id") Long id);

    int cancel(@Param("id") Long id, @Param("claimantId") Long claimantId);
}
