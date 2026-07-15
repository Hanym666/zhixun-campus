package com.zhixun.item.mapper;

import com.zhixun.item.domain.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotificationMapper {
    int insert(Notification notification);

    List<Notification> findPage(@Param("userId") Long userId,
                                @Param("unreadOnly") boolean unreadOnly,
                                @Param("offset") long offset,
                                @Param("size") int size);

    long countPage(@Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly);

    int markRead(@Param("id") Long id, @Param("userId") Long userId);
}
