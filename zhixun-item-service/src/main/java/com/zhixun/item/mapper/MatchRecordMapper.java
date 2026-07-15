package com.zhixun.item.mapper;

import com.zhixun.item.domain.MatchRecord;
import org.apache.ibatis.annotations.Param;

public interface MatchRecordMapper {
    int upsert(MatchRecord record);

    int markNotified(@Param("sourcePostId") Long sourcePostId,
                     @Param("targetPostId") Long targetPostId);
}
