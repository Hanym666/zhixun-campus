package com.zhixun.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper {

    int insert(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId
    );
}
