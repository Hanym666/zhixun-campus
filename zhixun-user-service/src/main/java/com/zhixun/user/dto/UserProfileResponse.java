package com.zhixun.user.dto;

import com.zhixun.common.enums.UserIdentityType;
import com.zhixun.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String username,
        String realName,
        UserIdentityType identityType,
        String studentStaffNo,
        String phone,
        String email,
        String avatarUrl,
        UserStatus status,
        List<String> roles,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}
