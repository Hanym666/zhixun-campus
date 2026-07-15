package com.zhixun.user.mapper;

import com.zhixun.user.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper {

    SysUser findById(@Param("id") Long id);

    SysUser findByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByStudentStaffNo(@Param("studentStaffNo") String studentStaffNo);

    boolean existsByEmail(@Param("email") String email);

    int insert(SysUser user);

    int updateLastLoginAt(
            @Param("id") Long id,
            @Param("lastLoginAt") LocalDateTime lastLoginAt
    );
}
