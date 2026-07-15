package com.zhixun.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    Long findIdByCode(@Param("code") String code);

    List<String> findCodesByUserId(@Param("userId") Long userId);
}
